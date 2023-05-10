package com.uc.ccs.visuals.screens.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentIntroBinding
import com.uc.ccs.visuals.databinding.FragmentSplashScreenBinding

class IntroFragment : Fragment() {

    private var _binding: FragmentIntroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentIntroBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setupViews()
    }

    private fun FragmentIntroBinding.setupViews() {
        tvLetsGo.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_mapFragment)
        }
    }


}
