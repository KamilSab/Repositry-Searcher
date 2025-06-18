package com.volsib.repositorysearcher.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.volsib.repositorysearcher.AppViewModelProvider
import com.volsib.repositorysearcher.R
import com.volsib.repositorysearcher.adapters.DownloadsAdapter
import com.volsib.repositorysearcher.databinding.FragmentDownloadsBinding
import com.volsib.repositorysearcher.ui.ReposViewModel
import com.volsib.repositorysearcher.RSApplication

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReposViewModel by viewModels {
        val app = requireActivity().application as RSApplication
        AppViewModelProvider(app.container).Factory
    }
    private lateinit var downloadsAdapter: DownloadsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        viewModel.getDownloadedRepos().observe(viewLifecycleOwner) {downloads ->
            downloadsAdapter.differ.submitList(downloads)
        }
    }

    private fun setupToolbar() {
        // Добавление отступа сверху для статус бара
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<AppBarLayout.LayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }

        val toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.navigation_search)
        }
    }

    private fun setupRecyclerView() {
        downloadsAdapter = DownloadsAdapter()
        binding.rvDownloads.apply {
            adapter = downloadsAdapter
            layoutManager = LinearLayoutManager(activity)
            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            divider.setDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.divider_vertical_22, null)!!
            )
            addItemDecoration(divider)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}