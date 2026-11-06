package com.pethealth.passport.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Post
import com.pethealth.passport.data.model.Species
import com.pethealth.passport.databinding.FragmentCommunityBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels {
        viewModelFactory { CommunityViewModel(container.communityRepository) }
    }

    private val adapter by lazy {
        PostAdapter(
            ownerName = container.settings.ownerName,
            onOpen = { post ->
                findNavController().navigate(
                    R.id.postDetailFragment,
                    bundleOf("postId" to post.id)
                )
            },
            onLike = { post ->
                viewLifecycleOwner.lifecycleScope.launch { viewModel.toggleLike(post.id) }
            },
            onComment = { post ->
                findNavController().navigate(
                    R.id.postDetailFragment,
                    bundleOf("postId" to post.id)
                )
            },
            onDelete = { post -> confirmDelete(post) }
        )
    }

    private var allPosts: List<Post> = emptyList()
    private var speciesFilter: String? = null
    private var chipsReady = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerPosts.adapter = adapter
        binding.fabCreate.setOnClickListener {
            findNavController().navigate(R.id.createPostFragment)
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        buildFilterChips()

        collectFlow(viewModel.state) { state ->
            allPosts = state.posts
            renderPosts()
            binding.swipeRefresh.isRefreshing = state.refreshing
            binding.txtStatus.text = getString(
                if (state.cloudEnabled) R.string.community_cloud else R.string.community_offline
            )
            binding.txtStatus.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(
                requireContext(),
                if (state.cloudEnabled) R.color.brand_teal_light else R.color.surface_variant
            )
            binding.txtStatus.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                    requireContext(),
                    if (state.cloudEnabled) R.color.brand_teal_dark else R.color.text_secondary
                )
            )
        }
    }

    private fun buildFilterChips() {
        val all = Chip(requireContext()).apply {
            text = getString(R.string.community_all)
            isCheckable = true
            isChecked = true
            tag = null
        }
        binding.chipSpecies.addView(all)
        Species.all.forEach { option ->
            val chip = Chip(requireContext()).apply {
                text = getString(option.labelRes)
                isCheckable = true
                tag = option.key
            }
            binding.chipSpecies.addView(chip)
        }
        chipsReady = true
        binding.chipSpecies.setOnCheckedStateChangeListener { group, checkedIds ->
            if (!chipsReady || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedIds.first()) ?: return@setOnCheckedStateChangeListener
            speciesFilter = chip.tag as? String
            renderPosts()
        }
    }

    private fun renderPosts() {
        val filtered = allPosts.filter { speciesFilter == null || it.speciesTag == speciesFilter }
        adapter.submitList(filtered)
        binding.txtEmpty.visible(filtered.isEmpty())
        binding.recyclerPosts.visible(filtered.isNotEmpty())
    }

    private fun confirmDelete(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.community_title)
            .setMessage(R.string.community_delete_confirm)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deletePost(post)
                    toast(getString(R.string.common_delete))
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
