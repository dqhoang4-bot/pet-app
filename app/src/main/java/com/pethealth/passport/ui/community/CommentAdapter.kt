package com.pethealth.passport.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.data.local.entity.Comment
import com.pethealth.passport.databinding.ItemCommentBinding
import com.pethealth.passport.ui.common.relativeTime

class CommentAdapter : ListAdapter<Comment, CommentAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val comment = getItem(position)
        val context = holder.binding.root.context
        with(holder.binding) {
            val indent = if (comment.parentId != null) {
                context.resources.getDimensionPixelSize(com.pethealth.passport.R.dimen.space_xl)
            } else {
                0
            }
            root.setPadding(indent, root.paddingTop, 0, root.paddingBottom)
            txtAuthor.text = comment.authorName
            txtMeta.text = relativeTime(context, comment.createdAt)
            txtContent.text = comment.content
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(a: Comment, b: Comment) = a.id == b.id
            override fun areContentsTheSame(a: Comment, b: Comment) = a == b
        }
    }
}
