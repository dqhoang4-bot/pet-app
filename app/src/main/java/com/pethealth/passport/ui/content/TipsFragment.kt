package com.pethealth.passport.ui.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pethealth.passport.R
import com.pethealth.passport.data.repository.Article
import com.pethealth.passport.data.repository.ArticleCategory
import com.pethealth.passport.databinding.FragmentTipsBinding
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.label
import com.pethealth.passport.ui.common.visible

class TipsFragment : Fragment() {

    private var _binding: FragmentTipsBinding? = null
    private val binding get() = _binding!!

    private val english: Boolean by lazy { container.contentRepository.isEnglish() }
    private val adapter by lazy { ArticleAdapter(english) { showArticle(it) } }

    private var allArticles: List<Article> = emptyList()
    private var category: ArticleCategory? = null
    private var query: String = ""
    private var chipsReady = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allArticles = container.contentRepository.articles
        binding.recyclerArticles.adapter = adapter
        buildCategoryChips()
        binding.editSearch.doAfterTextChanged {
            query = it?.toString()?.trim().orEmpty()
            render()
        }
        render()
    }

    private fun buildCategoryChips() {
        val all = Chip(requireContext()).apply {
            text = getString(R.string.tips_all)
            isCheckable = true
            isChecked = true
            tag = null
        }
        binding.chipCategories.addView(all)

        ArticleCategory.entries.forEach { option ->
            val chip = Chip(requireContext()).apply {
                text = option.label(requireContext())
                isCheckable = true
                tag = option
            }
            binding.chipCategories.addView(chip)
        }

        chipsReady = true
        binding.chipCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (!chipsReady || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedIds.first())
                ?: return@setOnCheckedStateChangeListener
            category = chip.tag as? ArticleCategory
            render()
        }
    }

    private fun render() {
        val filtered = allArticles.filter { article ->
            val matchesCategory = category == null || article.category == category
            val matchesQuery = query.isBlank() ||
                article.title(english).contains(query, ignoreCase = true) ||
                article.body(english).contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
        adapter.submitList(filtered)
        binding.txtEmpty.visible(filtered.isEmpty())
        binding.recyclerArticles.visible(filtered.isNotEmpty())
    }

    private fun showArticle(article: Article) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(article.title(english))
            .setMessage(article.body(english))
            .setPositiveButton(R.string.common_close, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
