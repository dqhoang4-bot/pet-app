package com.pethealth.passport.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.Comment
import com.pethealth.passport.data.local.entity.Post
import com.pethealth.passport.data.repository.CommunityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PostDetailUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList()
)

class PostDetailViewModel(
    private val postId: Long,
    private val communityRepository: CommunityRepository
) : ViewModel() {

    val state: StateFlow<PostDetailUiState> = combine(
        communityRepository.post(postId),
        communityRepository.comments(postId)
    ) { post, comments ->
        PostDetailUiState(post = post, comments = comments)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PostDetailUiState()
    )

    suspend fun toggleLike() = communityRepository.toggleLike(postId)

    suspend fun addComment(content: String) = communityRepository.addComment(postId, content)

    suspend fun deletePost(post: Post) = communityRepository.deletePost(post)
}
