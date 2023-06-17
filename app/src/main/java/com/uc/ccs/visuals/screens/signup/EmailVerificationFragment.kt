package com.uc.ccs.visuals.screens.signup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.uc.ccs.visuals.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager


class EmailVerificationFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email

        view.findViewById<TextView>(R.id.emailTextView).text = email


        val backtoLogin = view.findViewById<ImageView>(R.id.btn_back_to_login)
        backtoLogin.setOnClickListener {
                SharedPreferenceManager.clearCurrentUser(requireContext())
                FirebaseAuthManager.signOut()
                findNavController().navigate(R.id.action_emailVerificationFragment_to_loginFragment)
        }

        val proceed = view.findViewById<MaterialButton>(R.id.proceed)
        proceed.setOnClickListener {
            handleEmailVerification()
        }
    }

    private fun handleEmailVerification() {
        val currentUser = FirebaseAuthManager.getCurrentUser()

        currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val isEmailVerified = currentUser.isEmailVerified

                if (isEmailVerified) {
                    findNavController().navigate(R.id.action_emailVerificationFragment_to_splashScreenFragment)
                }
                else
                {
                    Toast.makeText(requireContext(), "Email is not verified.", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(requireContext(), "Failed to reload user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_email_verification, container, false)
    }

}