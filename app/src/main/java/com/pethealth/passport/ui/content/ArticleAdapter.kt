package com.pethealth.passport.ui.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.R
import com.pethealth.passport.data.repository.Article
import com.pethealth.passport.data.repository.ArticleCategory
import com.pethealth.passport.databinding.ItemArticleBinding
import com.pethealth.passport.ui.common.label

class ArticleAdapter(
    private val english: Boolean,
    private val onClick: (Article) -> Unit
) : ListAdapter<Article, ArticleAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val article = getItem(position)
        val context = holder.binding.root.context
        with(holder.binding) {
            txtEmoji.text = article.emoji
            txtTitle.text = article.title(english)
            txtCategory.text = article.category.label(context)
            txtSnippet.text = article.body(english).replace('\n', ' ')

            val (background, foreground) = when (article.category) {
                ArticleCategory.NUTRITION -> R.color.brand_teal_light to R.color.brand_teal_dark
                ArticleCategory.DISEASE -> R.color.status_overdue_bg to R.color.status_overdue
                ArticleCategory.CARE -> R.color.status_due_soon_bg to R.color.status_due_soon
            }
            txtCategory.backgroundTintList =
                ContextCompat.getColorStateList(context, background)
            txtCategory.setTextColor(ContextCompat.getColor(context, foreground))

            root.setOnClickListener { onClick(article) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Article, newItem: Article) =
                oldItem == newItem
        }
    }
}
