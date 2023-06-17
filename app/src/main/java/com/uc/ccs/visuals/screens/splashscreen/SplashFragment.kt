package com.uc.ccs.visuals.screens.splashscreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.data.CsvDataRepository
import com.uc.ccs.visuals.databinding.FragmentSplashScreenBinding
import com.uc.ccs.visuals.factories.AdminDashboardViewModelFactory
import com.uc.ccs.visuals.screens.admin.AdminDashboardViewModel
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.login.UserRoles
import com.uc.ccs.visuals.screens.main.CsvDataState
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.screens.settings.CsvData
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager

data class Person(var name: String, var address: String, var age: Int)

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    private val SPLASH_TIME_OUT = 3000L // 3 seconds delay

    private lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mapViewModel: MapViewModel
    private lateinit var adminViewModel: AdminDashboardViewModel

    private val onSuccessGetUsers: (List<UserItem>) -> Unit = {
        adminViewModel.setUsers(it)
    }

    private val onSuccess: (List<CsvData>) -> Unit = { csvData ->
        /**
         * [05/29/2023] Temporary Disabled: Currently using local db to show markers
         *
         * mapViewModel.setCsvDataState(CsvDataState.onSuccess(csvData))
         *
         * */

        adminViewModel.setCsvData(csvData)
        adminViewModel.insertCsvDataToLocalDb(csvData)

        if(SharedPreferenceManager.getRoles(requireContext()) == UserRoles.ADMIN.value)
            firestoreViewModel.getUsers(onSuccessGetUsers, onFailure)
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

        val repository = CsvDataRepository(requireContext())
        val viewModelFactory = AdminDashboardViewModelFactory(repository)

        firestoreViewModel = ViewModelProvider(requireActivity()).get(FirestoreViewModel::class.java)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        adminViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(AdminDashboardViewModel::class.java)

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

            if (SharedPreferenceManager.getRoles(requireContext()) == UserRoles.ADMIN.value) {
                findNavController().navigate(R.id.action_splashScreenFragment_to_adminDashboardFragment)
                return@postDelayed
            }

            val verified = FirebaseAuthManager.getCurrentUser()?.isEmailVerified

            if (currentUser != null && isFirstLogin && verified == true)
            {
                SharedPreferenceManager.setFirstLogin(requireContext(), false)
                findNavController().navigate(R.id.action_splashScreenFragment_to_introFragment)
            }
            else if(currentUser != null && verified == true)
            {
                findNavController().navigate(R.id.action_splashScreenFragment_to_mapFragment)
            }
            else
            {
                findNavController().navigate(R.id.action_splashScreenFragment_to_emailVerificationFragment)
            }

        }, SPLASH_TIME_OUT)
    }

}