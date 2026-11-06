package com.pethealth.passport.data.repository

import android.content.Context
import com.pethealth.passport.data.local.dao.CommunityDao
import com.pethealth.passport.data.local.entity.Comment
import com.pethealth.passport.data.local.entity.Post
import com.pethealth.passport.data.local.model.UpcomingVaccination
import com.pethealth.passport.util.ImageStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommunityRepository(
    private val dao: CommunityDao,
    private val context: Context,
    private val settings: SettingsRepository,
    private val imageStore: ImageStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _cloudEnabled = MutableStateFlow(false)
    val cloudEnabled: StateFlow<Boolean> = _cloudEnabled

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    val posts: Flow<List<Post>> = dao.observePosts()

    fun post(id: Long): Flow<Post?> = dao.observePost(id)

    fun comments(postId: Long): Flow<List<Comment>> = dao.observeComments(postId)

    suspend fun createPost(
        content: String,
        speciesTag: String,
        petName: String,
        imageUri: String?
    ): Long {
        val storedImage = imageUri?.let { imageStore.copyToInternal(it) }
        val post = Post(
            authorName = settings.ownerName,
            petName = petName,
            speciesTag = speciesTag,
            content = content.trim(),
            imageUri = storedImage,
            pendingSync = true
        )
        val id = dao.insertPost(post)
        pushRemote(id)
        return id
    }

    suspend fun toggleLike(postId: Long) {
        val post = dao.getPost(postId) ?: return
        val liked = !post.likedByMe
        val count = (post.likeCount + if (liked) 1 else -1).coerceAtLeast(0)
        dao.setLike(postId, liked, count)
    }

    suspend fun addComment(postId: Long, content: String, parentId: Long? = null) {
        if (content.isBlank()) return
        dao.insertComment(
            Comment(
                postId = postId,
                parentId = parentId,
                authorName = settings.ownerName,
                content = content.trim()
            )
        )
        dao.bumpCommentCount(postId, 1)
    }

    suspend fun deletePost(post: Post) {
        dao.deletePost(post)
        val remoteId = post.remoteId ?: return
        runCatching {
            if (firebaseReady()) {
                FirebaseBridge.firestore(context).collection("posts").document(remoteId).delete().await()
            }
        }
    }

    fun refresh() {
        scope.launch { refreshRemote() }
    }

    suspend fun refreshRemote() {
        if (!firebaseReady()) {
            _cloudEnabled.value = false
            return
        }
        _cloudEnabled.value = true
        _refreshing.value = true
        runCatching {
            val snapshot = FirebaseBridge.firestore(context)
                .collection("posts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val remoteId = doc.id
                val author = doc.getString("authorName") ?: "Pet lover"
                val petName = doc.getString("petName") ?: ""
                val tag = doc.getString("speciesTag") ?: "other"
                val body = doc.getString("content") ?: return@forEach
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                val likes = doc.getLong("likeCount")?.toInt() ?: 0
                val comments = doc.getLong("commentCount")?.toInt() ?: 0
                val existing = dao.getPostByRemoteId(remoteId)
                if (existing == null) {
                    dao.insertPost(
                        Post(
                            remoteId = remoteId,
                            authorName = author,
                            petName = petName,
                            speciesTag = tag,
                            content = body,
                            createdAt = createdAt,
                            likeCount = likes,
                            commentCount = comments,
                            pendingSync = false
                        )
                    )
                } else {
                    dao.updatePost(
                        existing.copy(
                            authorName = author,
                            petName = petName,
                            speciesTag = tag,
                            content = body,
                            likeCount = likes,
                            commentCount = comments,
                            pendingSync = false
                        )
                    )
                }
            }
        }.onFailure {
            _cloudEnabled.value = false
        }
        _refreshing.value = false
    }

    private fun pushRemote(postId: Long) {
        scope.launch {
            if (!firebaseReady()) return@launch
            val post = dao.getPost(postId) ?: return@launch
            _cloudEnabled.value = true
            runCatching {
                val data = hashMapOf(
                    "authorName" to post.authorName,
                    "petName" to post.petName,
                    "speciesTag" to post.speciesTag,
                    "content" to post.content,
                    "createdAt" to post.createdAt,
                    "likeCount" to post.likeCount,
                    "commentCount" to post.commentCount
                )
                val ref = FirebaseBridge.firestore(context).collection("posts").add(data).await()
                dao.setRemoteId(postId, ref.id)
            }.onFailure {
                dao.markPending(postId)
                _cloudEnabled.value = false
            }
        }
    }

    private fun firebaseReady(): Boolean = FirebaseBridge.isAvailable(context)
}

/**
 * Isolates the optional Firebase dependency so the rest of the app stays
 * fully functional in offline mode.
 */
private object FirebaseBridge {
    fun isAvailable(context: Context): Boolean = runCatching {
        com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()
    }.getOrDefault(false)

    fun firestore(context: Context): com.google.firebase.firestore.FirebaseFirestore =
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
}
