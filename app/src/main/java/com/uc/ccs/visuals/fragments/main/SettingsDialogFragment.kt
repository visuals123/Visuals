package com.uc.ccs.visuals.fragments.main

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSignListDialogListDialogBinding
import com.uc.ccs.visuals.databinding.FragmentSignListDialogListDialogItemBinding

class SettingsDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSignListDialogListDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSignListDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding?.list?.let {
            it.apply {
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        view?.post {
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                it.measuredHeight.let { height ->
                    behavior.setPeekHeight(height)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}