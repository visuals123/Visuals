package com.uc.ccs.visuals.screens.settings

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSettingsDialogBinding
import com.uc.ccs.visuals.databinding.FragmentSignListDialogListDialogBinding
import com.uc.ccs.visuals.screens.auth.FirebaseAuthManager

class SettingsDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSettingsDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_settings, SettingsFragment())
            .commit()
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

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            // Listen for preference clicks
            findPreference<Preference>("logout_preference")?.setOnPreferenceClickListener {
                // Handle logout click
                FirebaseAuthManager.signOut()
                findNavController().navigate(R.id.action_settingsDialogFragment_to_loginFragment)
                true
            }
        }
    }
}