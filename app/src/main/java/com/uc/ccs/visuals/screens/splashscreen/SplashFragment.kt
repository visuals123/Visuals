package com.uc.ccs.visuals.screens.splashscreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSplashScreenBinding
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.screens.login.LoginViewModel
import com.uc.ccs.visuals.screens.main.CsvDataState
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.screens.settings.CsvData
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager

data class Person(var name: String, var address: String, var age: Int)

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginViewModel: LoginViewModel

    private val SPLASH_TIME_OUT = 3000L // 3 seconds delay

    private lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var mapViewModel: MapViewModel

    private val onSuccess: (List<CsvData>) -> Unit = { csvData ->
        mapViewModel.setCsvDataState(CsvDataState.onSuccess(csvData))
    }

    private val onFailure: (e: Exception) -> Unit = {exception ->
        mapViewModel.setCsvDataState(CsvDataState.onFailure(exception))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)

        firestoreViewModel = ViewModelProvider(requireActivity()).get(FirestoreViewModel::class.java)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(mapViewModel) {
            setCsvDataState(CsvDataState.onLoad)
            firestoreViewModel.retrieveData(onSuccess, onFailure)
        }

    }

    override fun onStart() {
        super.onStart()

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