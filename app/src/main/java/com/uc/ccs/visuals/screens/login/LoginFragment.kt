package com.uc.ccs.visuals.screens.login

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentLoginBinding
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.apply {

            btnLogin.setOnClickListener {
                val email = binding.etUsername.text.toString()
                val pass = binding.etPassword.text.toString()

                viewModel.login(email, pass)
            }

            tvToSignup.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
            }

            observeAuthenticationState()

        }
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.AuthenticationState.Authenticated -> {
                    findNavController().navigate(R.id.action_loginFragment_to_splashScreenFragment)
                }

                is LoginViewModel.AuthenticationState.InvalidAuthentication -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.login_error_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuthManager.getCurrentUser()
        if (currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_splashScreenFragment)
        }
    }

}

const val TAG = "LoginFragment"