package com.pethealth.passport.ui.community

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Post
import com.pethealth.passport.data.model.Species
import com.pethealth.passport.databinding.FragmentPostDetailBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.relativeTime
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible
import kotlinx.coroutines.launch
import java.io.File

class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    private val postId: Long by lazy { requireArguments().getLong("postId") }

    private val viewModel: PostDetailViewModel by viewModels {
        viewModelFactory { PostDetailViewModel(postId, container.communityRepository) }
    }

    private val adapter = CommentAdapter()
    private var currentPost: Post? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerComments.adapter = adapter

        binding.btnSend.setOnClickListener { sendComment() }
        binding.btnComment.setOnClickListener {
            binding.editComment.requestFocus()
        }
        binding.btnLike.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { viewModel.toggleLike() }
        }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        collectFlow(viewModel.state) { state ->
            val post = state.post
            if (post == null) return@collectFlow
            currentPost = post
            bindPost(post)
            adapter.submitList(state.comments)
            binding.txtNoComments.visible(state.comments.isEmpty())
            binding.recyclerComments.visible(state.comments.isNotEmpty())
        }
    }

    private fun bindPost(post: Post) {
        val context = requireContext()
        binding.txtAvatar.text = Species.emoji(post.speciesTag)
        binding.txtAuthor.text = post.authorName
        binding.txtMeta.text = listOf(
            post.petName,
            context.getString(Species.option(post.speciesTag).labelRes),
            relativeTime(context, post.createdAt)
        ).filter { it.isNotBlank() }.joinToString(" • ")
        binding.txtContent.text = post.content

        val hasImage = !post.imageUri.isNullOrBlank()
        binding.imgPhoto.visible(hasImage)
        if (hasImage) {
            binding.imgPhoto.load(File(post.imageUri!!.removePrefix("file://"))) {
                crossfade(true)
                placeholder(R.drawable.bg_image_placeholder)
                error(R.drawable.bg_image_placeholder)
            }
        }

        binding.btnLike.text = context.getString(R.string.community_likes, post.likeCount)
        binding.btnComment.text = context.getString(R.string.community_comments, post.commentCount)
        binding.btnLike.setIconResource(
            if (post.likedByMe) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
        binding.btnLike.iconTint = ColorStateList.valueOf(
            ContextCompat.getColor(
                context,
                if (post.likedByMe) R.color.brand_coral else R.color.text_secondary
            )
        )
        binding.btnDelete.visible(post.authorName == container.settings.ownerName)
    }

    private fun sendComment() {
        val content = binding.editComment.text?.toString()?.trim().orEmpty()
        if (content.isBlank()) return
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addComment(content)
            binding.editComment.setText("")
        }
    }

    private fun confirmDelete() {
        val post = currentPost ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.community_title)
            .setMessage(R.string.community_delete_confirm)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deletePost(post)
                    toast(getString(R.string.common_delete))
                    findNavController().navigateUp()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
