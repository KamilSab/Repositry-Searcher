package com.volsib.repositorysearcher.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.volsib.repositorysearcher.AppViewModelProvider
import com.volsib.repositorysearcher.R
import com.volsib.repositorysearcher.adapters.ReposAdapter
import com.volsib.repositorysearcher.databinding.FragmentReposBinding
import com.volsib.repositorysearcher.ui.ReposViewModel
import com.volsib.repositorysearcher.util.Constants.Companion.QUERY_PAGE_SIZE
import com.volsib.repositorysearcher.util.Resource
import com.volsib.repositorysearcher.RSApplication


class ReposFragment : Fragment() {
    private var _binding: FragmentReposBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReposViewModel by viewModels {
        val app = requireActivity().application as RSApplication
        AppViewModelProvider(app.container).Factory
    }
    private lateinit var reposAdapter: ReposAdapter

    private var isError = false
    private var isLoading = false
    private var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReposBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()

        reposAdapter.apply {
            setOnNameClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
                startActivity(intent)
            }
            setOnDownloadClickListener {
                viewModel.downloadRepo(it)
            }
            setOnFavoriteClickListener {
                viewModel.toggleFavorite(it)
            }
        }

        viewModel.repos.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { reposResponse ->
                        reposAdapter.differ.submitList(reposResponse)
                        // Обновляем адаптер после получения данных
                        reposAdapter.notifyDataSetChanged()
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, message, Toast.LENGTH_LONG)
                            .show()
                    }
                    reposAdapter.differ.submitList(emptyList())
                    // Обновляем адаптер после ошибки
                    reposAdapter.notifyDataSetChanged()
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {}
            }
        }

        viewModel.downloads.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { repoName ->
                        Snackbar.make(binding.root, "$repoName.zip успешно загружен в папку Download", Snackbar.LENGTH_SHORT).show()
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {}
            }
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
        toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.repos_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.refresh_option -> {
                        val existingUsername = binding.etSearch.text.toString()
                        viewModel.getRepos(existingUsername)
                        binding.rvRepos.scrollToPosition(0)
                    }
                    R.id.downloads_option -> {
                        // Переход на экран профиля
                        findNavController().navigate(R.id.navigation_profile)
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.etSearch.apply {
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val username = v.text.toString()
                    viewModel.getRepos(username)
                    // Скрытие клавиатуры и удаление фокуса
                    val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                    v.clearFocus()
                    return@setOnEditorActionListener true
                }
                false
            }

            val removeFilter = InputFilter { s, _, _, _, _, _ ->
                s.toString().removeSpace()
            }
            filters = filters.plus(removeFilter)
        }
    }

    private fun String.removeSpace() = trim().replace("\\s+".toRegex(), replacement = "")

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !viewModel.isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getRepos(binding.etSearch.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter()
        binding.rvRepos.apply {
            adapter = reposAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@ReposFragment.scrollListener)
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