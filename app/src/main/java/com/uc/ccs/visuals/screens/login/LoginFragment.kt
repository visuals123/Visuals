package com.uc.ccs.visuals.screens.login

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.window.SplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.jakewharton.rxbinding2.widget.RxTextView
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentLoginBinding
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.screens.splashscreen.SplashFragment
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager
import io.reactivex.Observable

@SuppressLint("CheckResult")
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
   // private lateinit var googleSignInClient : GoogleSignInClient

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

        mapViewModel.setUpGoogleClient(requireContext())

        return binding.root
    }

    private fun signInGoogle() {
        mapViewModel.googleSignInHelper.googleSignInClient?.let {
            launcher.launch(it.signInIntent) }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }else{
            Toast.makeText(requireContext(), task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                findNavController().navigate(R.id.action_loginFragment_to_splashScreenFragment)
            }else{
                Toast.makeText(requireContext(), it.exception.toString() , Toast.LENGTH_SHORT).show()

            }
        }
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
                showBottomDialog()
            }

            btnLoginWithGoogle.setOnClickListener {
                signInGoogle()
            }

            forgotPass.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Forgot Password")
                val view = layoutInflater.inflate(R.layout.dialog_forgot_password,null)
                val username = view.findViewById<EditText>(R.id.et_username)
                builder.setView(view)
                builder.setPositiveButton("Reset", DialogInterface.OnClickListener { _, _ ->
                    forgotPassword(username)
                })
                builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ ->  })
                builder.show()
            }

            observeAuthenticationState()

        }
    }

    private fun forgotPassword(username : EditText){
        if (username.text.toString().isEmpty()) {
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()) {
            return
        }

        firebaseAuth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(),"Email sent.",Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if(text == "Email")
            binding.emailEt.error = if(isNotValid) "Enter your valid email" else null
    }

    private fun showBottomDialog(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheet)

        val signup_email = dialog.findViewById<MaterialButton>(R.id.btn_signup_email)
        signup_email?.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
            dialog.dismiss()
        }


        val btnSignupGoogle = dialog.findViewById<MaterialButton>(R.id.btn_signup_google)
        btnSignupGoogle?.setOnClickListener {
            signInGoogle()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations  = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

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