package com.pethealth.passport.ui.community

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Post
import com.pethealth.passport.data.model.Species
import com.pethealth.passport.databinding.ItemPostBinding
import com.pethealth.passport.ui.common.relativeTime
import com.pethealth.passport.ui.common.visible
import java.io.File

class PostAdapter(
    private val ownerName: String,
    private val onOpen: (Post) -> Unit,
    private val onLike: (Post) -> Unit,
    private val onComment: (Post) -> Unit,
    private val onDelete: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val post = getItem(position)
        val context = holder.binding.root.context
        with(holder.binding) {
            txtAvatar.text = Species.emoji(post.speciesTag)
            txtAuthor.text = post.authorName
            txtMeta.text = listOf(
                post.petName,
                context.getString(Species.option(post.speciesTag).labelRes),
                relativeTime(context, post.createdAt)
            ).filter { it.isNotBlank() }.joinToString(" • ")
            txtContent.text = post.content
            txtPending.visible(post.pendingSync)

            val hasImage = !post.imageUri.isNullOrBlank()
            imgPost.visible(hasImage)
            if (hasImage) {
                imgPost.load(File(post.imageUri!!.removePrefix("file://"))) {
                    crossfade(true)
                    placeholder(R.drawable.bg_image_placeholder)
                    error(R.drawable.bg_image_placeholder)
                }
            }

            btnLike.text = context.getString(R.string.community_likes, post.likeCount)
            btnComment.text = context.getString(R.string.community_comments, post.commentCount)
            btnLike.setIconResource(
                if (post.likedByMe) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            btnLike.iconTint = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (post.likedByMe) R.color.brand_coral else R.color.text_secondary
                )
            )

            btnDelete.visible(post.authorName == ownerName)
            root.setOnClickListener { onOpen(post) }
            btnLike.setOnClickListener { onLike(post) }
            btnComment.setOnClickListener { onComment(post) }
            btnDelete.setOnClickListener { onDelete(post) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(a: Post, b: Post) = a.id == b.id
            override fun areContentsTheSame(a: Post, b: Post) = a == b
        }
    }
}
