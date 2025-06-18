package com.volsib.repositorysearcher.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.volsib.repositorysearcher.AppViewModelProvider
import com.volsib.repositorysearcher.R
import com.volsib.repositorysearcher.RSApplication
import com.volsib.repositorysearcher.databinding.FragmentLoginBinding
import com.volsib.repositorysearcher.util.Resource
import com.volsib.repositorysearcher.viewmodel.AuthState
import com.volsib.repositorysearcher.viewmodel.AuthViewModel

class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as RSApplication
        AppViewModelProvider(app.container).Factory
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeAuthState()
    }
    
    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(username, password)
            } else {
                showError("Заполните все поля")
            }
        }
        
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
    
    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    hideError()
                }
                is Resource.Success -> {
                    showLoading(false)
                    when (resource.data) {
                        is AuthState.LoggedIn -> {
                            Toast.makeText(context, "Добро пожаловать, ${resource.data.user.username}!", Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        }
                        is AuthState.Registered -> {
                            Toast.makeText(context, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        }
                        is AuthState.LoggedOut -> {
                            // Пользователь вышел из системы
                        }

                        null -> TODO()
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: "Произошла ошибка")
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.btnRegister.isEnabled = !show
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
    
    private fun navigateToMain() {
        findNavController().navigate(R.id.action_loginFragment_to_searchFragment)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 