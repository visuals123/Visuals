package com.uc.ccs.visuals.screens.splashscreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSplashScreenBinding
import com.uc.ccs.visuals.utils.auth.FirebaseAuthManager
import com.uc.ccs.visuals.screens.login.LoginViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager

data class Person(var name: String, var address: String, var age: Int)

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginViewModel: LoginViewModel

    private val SPLASH_TIME_OUT = 3000L // 3 seconds delay

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isFirstLogin = SharedPreferenceManager.isFirstLogin(requireContext())

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuthManager.getCurrentUser()
            if (currentUser != null && isFirstLogin) {
                SharedPreferenceManager.setFirstLogin(requireContext(), false)
                findNavController().navigate(R.id.action_splashScreenFragment_to_introFragment)
            }
            else { findNavController().navigate(R.id.action_splashScreenFragment_to_mapFragment)}
        }, SPLASH_TIME_OUT)

    }

}