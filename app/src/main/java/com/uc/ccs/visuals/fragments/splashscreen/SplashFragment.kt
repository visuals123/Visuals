package com.uc.ccs.visuals.fragments.splashscreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSplashScreenBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

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

        Handler(Looper.getMainLooper()).postDelayed({
                  findNavController().navigate(R.id.action_splashScreenFragment_to_mapFragment)
        }, SPLASH_TIME_OUT)

    }

}