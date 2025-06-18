package com.volsib.repositorysearcher.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.volsib.repositorysearcher.AppViewModelProvider
import com.volsib.repositorysearcher.RSApplication
import com.volsib.repositorysearcher.adapters.SearchHistoryAdapter
import com.volsib.repositorysearcher.databinding.FragmentSearchHistoryBinding
import com.volsib.repositorysearcher.ui.ReposViewModel
import com.volsib.repositorysearcher.util.Resource

class SearchHistoryFragment : Fragment() {
    
    private var _binding: FragmentSearchHistoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ReposViewModel by viewModels {
        val app = requireActivity().application as RSApplication
        AppViewModelProvider(app.container).Factory
    }
    
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupUI()
        observeSearchHistory()
        
        // Загрузка истории поиска
        viewModel.getSearchHistory()
    }
    
    private fun setupRecyclerView() {
        searchHistoryAdapter = SearchHistoryAdapter()
        binding.recyclerView.apply {
            adapter = searchHistoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupUI() {
        binding.btnClearHistory.setOnClickListener {
            viewModel.clearSearchHistory()
        }
    }
    
    private fun observeSearchHistory() {
        viewModel.searchHistory.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    hideEmptyState()
                }
                is Resource.Success -> {
                    showLoading(false)
                    val historyList = resource.data
                    if (historyList?.isEmpty() == true) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                        searchHistoryAdapter.submitList(historyList)
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: "Ошибка загрузки истории")
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showEmptyState() {
        binding.tvEmpty.text = "История поиска пуста"
        binding.tvEmpty.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        binding.tvEmpty.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 