package com.uc.ccs.visuals.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentLoginBinding
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var viewModel: LoginViewModel
    private lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var mapViewModel: MapViewModel

    private val onSuccess: (UserItem?) -> Unit = {user ->
        user?.let {
            mapViewModel.setCurrentUser(user)

            if(SharedPreferenceManager.getCurrentUser(requireContext()) == null) {
                SharedPreferenceManager.setCurrentUser(requireContext(), user)
            }

            findNavController().navigate(R.id.action_loginFragment_to_splashScreenFragment)
        }
    }

    private val onFailure: (e: Exception) -> Unit = {
        Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        firestoreViewModel = ViewModelProvider(requireActivity()).get(FirestoreViewModel::class.java)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

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
                    firestoreViewModel.getUser(state.email, onSuccess, onFailure)
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

enum class UserRoles(val value: Int) {
    ADMIN(1),
    USER(2)
}

const val TAG = "LoginFragment"