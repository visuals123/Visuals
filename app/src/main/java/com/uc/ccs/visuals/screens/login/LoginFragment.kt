package com.uc.ccs.visuals.screens.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentLoginBinding
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager
import io.reactivex.Observable

@SuppressLint("CheckResult")
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

        //Email Validation
        val emailStream = RxTextView.textChanges(binding.emailEt)
            .skipInitialValue()
            .map { email ->
                email.isEmpty()
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showTextMinimalAlert(it, "Email")
        }

        //Password Validation
        val passwordStream = RxTextView.textChanges(binding.passwordEt)
            .skipInitialValue()
            .map { password ->
                password.isEmpty()
                password.length < 8
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it, "Password")
        }

        //Button Enable
        val invalidFieldStream = Observable.combineLatest(
            emailStream,
            passwordStream,
            { emailInvalid: Boolean, passwordInvalid: Boolean ->
                !emailInvalid && !passwordInvalid
            })
        invalidFieldStream.subscribe{isValid ->
            if (isValid) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
            }
            else {
                binding.btnLogin.isEnabled = false
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.md_blue_grey_600)
            }
        }

        binding.apply {

            btnLogin.setOnClickListener {
                val email = binding.emailEt.text.toString()
                val pass = binding.passwordEt.text.toString()

                viewModel.login(email, pass)
            }

            tvToSignup.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
            }

            observeAuthenticationState()

        }
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if(text == "Password")
            binding.passwordTIL.error = if(isNotValid) "Please enter your password" else null
        else if(text == "Email")
            binding.emailTIL.error = if(isNotValid) "Please enter your email" else null
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