package com.pethealth.passport.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pethealth.passport.data.local.entity.Comment
import com.pethealth.passport.data.local.entity.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun observePosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun observePost(id: Long): Flow<Post?>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPost(id: Long): Post?

    @Query("SELECT * FROM posts WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getPostByRemoteId(remoteId: String): Post?

    @Insert
    suspend fun insertPost(post: Post): Long

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("UPDATE posts SET likedByMe = :liked, likeCount = :count WHERE id = :id")
    suspend fun setLike(id: Long, liked: Boolean, count: Int)

    @Query("UPDATE posts SET commentCount = commentCount + :delta WHERE id = :id")
    suspend fun bumpCommentCount(id: Long, delta: Int)

    @Query("UPDATE posts SET remoteId = :remoteId, pendingSync = 0 WHERE id = :id")
    suspend fun setRemoteId(id: Long, remoteId: String)

    @Query("UPDATE posts SET pendingSync = 1 WHERE id = :id")
    suspend fun markPending(id: Long)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeComments(postId: Long): Flow<List<Comment>>

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    suspend fun getComments(postId: Long): List<Comment>

    @Insert
    suspend fun insertComment(comment: Comment): Long

    @Delete
    suspend fun deleteComment(comment: Comment)

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()
}
