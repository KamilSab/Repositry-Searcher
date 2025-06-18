package com.volsib.repositorysearcher.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.volsib.repositorysearcher.AppViewModelProvider
import com.volsib.repositorysearcher.R
import com.volsib.repositorysearcher.RSApplication
import com.volsib.repositorysearcher.databinding.FragmentFavoritesBinding
import com.volsib.repositorysearcher.util.Resource

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        val app = requireActivity().application as RSApplication
        AppViewModelProvider(app.container).Factory
    }

    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeFavorites()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter(
            onOpenInBrowser = { url ->
                // Открытие репозитория в браузере
                viewModel.openInBrowser(url)
            },
            onRemoveFromFavorites = { repo ->
                // Удаление из избранного
                viewModel.toggleFavorite(repo)
            }
        )

        binding.recyclerView.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeFavorites() {
        viewModel.favoriteRepos.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showLoading(false)
                    val favoritesList = resource.data
                    if (favoritesList.isNullOrEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                        favoritesAdapter.submitList(favoritesList)
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: "Ошибка загрузки избранного")
                }
                is Resource.Loading -> {
                    showLoading(true)
                    hideEmptyState()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        binding.tvEmpty.text = getString(R.string.no_favorites)
        binding.tvEmpty.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.tvEmpty.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 