package com.pethealth.passport.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.Post
import com.pethealth.passport.data.repository.CommunityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CommunityUiState(
    val posts: List<Post> = emptyList(),
    val cloudEnabled: Boolean = false,
    val refreshing: Boolean = false
)

class CommunityViewModel(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    val state: StateFlow<CommunityUiState> = combine(
        communityRepository.posts,
        communityRepository.cloudEnabled,
        communityRepository.refreshing
    ) { posts, cloud, refreshing ->
        CommunityUiState(posts = posts, cloudEnabled = cloud, refreshing = refreshing)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CommunityUiState()
    )

    fun refresh() = communityRepository.refresh()

    suspend fun toggleLike(postId: Long) = communityRepository.toggleLike(postId)

    suspend fun deletePost(post: Post) = communityRepository.deletePost(post)
}
