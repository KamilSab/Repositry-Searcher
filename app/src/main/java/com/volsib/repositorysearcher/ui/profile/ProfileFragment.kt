package com.volsib.repositorysearcher.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.volsib.repositorysearcher.AppViewModelProvider
import com.volsib.repositorysearcher.R
import com.volsib.repositorysearcher.RSApplication
import com.volsib.repositorysearcher.databinding.FragmentProfileBinding
import com.volsib.repositorysearcher.util.Resource

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        val app = requireActivity().application as RSApplication
        AppViewModelProvider(app.container).Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupUserInfo()
        setupLogoutButton()
    }

    private fun setupViewPager() {
        val pagerAdapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.search_history)
                1 -> getString(R.string.favorites)
                else -> null
            }
        }.attach()
    }

    private fun setupUserInfo() {
        viewModel.user.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { user ->
                        binding.tvUsername.text = getString(R.string.username_format, user.username)
                        binding.tvEmail.text = getString(R.string.email_format, user.email)
                        binding.tvRegistrationDate.text = getString(
                            R.string.registration_date_format,
                            viewModel.formatDate(user.createdAt)
                        )
                    }
                }
                is Resource.Error -> {
                    // Если пользователь не авторизован, перенаправляем на экран входа
                    findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                }
                is Resource.Loading -> {
                    // Можно показать индикатор загрузки
                }
            }
        }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 