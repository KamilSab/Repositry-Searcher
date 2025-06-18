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
import com.volsib.repositorysearcher.databinding.FragmentRegisterBinding
import com.volsib.repositorysearcher.util.Resource
import com.volsib.repositorysearcher.viewmodel.AuthState
import com.volsib.repositorysearcher.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {
    
    private var _binding: FragmentRegisterBinding? = null
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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeAuthState()
    }
    
    private fun setupUI() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            
            if (validateInput(username, email, password, confirmPassword)) {
                viewModel.register(username, email, password, confirmPassword)
            }
        }
        
        binding.btnBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
    
    private fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Заполните все поля")
            return false
        }
        
        if (password != confirmPassword) {
            showError("Пароли не совпадают")
            return false
        }
        
        if (password.length < 6) {
            showError("Пароль должен содержать минимум 6 символов")
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Введите корректный email")
            return false
        }
        
        return true
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
                            Toast.makeText(context, "Добро пожаловать!", Toast.LENGTH_SHORT).show()
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
        binding.btnRegister.isEnabled = !show
        binding.btnBackToLogin.isEnabled = !show
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
    
    private fun navigateToMain() {
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 