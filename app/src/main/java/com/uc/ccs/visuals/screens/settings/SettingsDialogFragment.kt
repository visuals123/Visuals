package com.uc.ccs.visuals.screens.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSettingsDialogBinding
import com.uc.ccs.visuals.utils.auth.FirebaseAuthManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

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

            findPreference<Preference>(getString(R.string.text_import))?.setOnPreferenceClickListener {
                // Handle CSV import click
                Log.d("qweqwe", "onCreatePreferences: 1")
                if (isStoragePermissionGranted()) {
                    Log.d("qweqwe", "onCreatePreferences: 2")
                    openFilePicker()
                } else {
                    Log.d("qweqwe", "onCreatePreferences: 3")
                    openFilePicker()
                }
                true
            }
        }

        private fun isStoragePermissionGranted(): Boolean {
            return if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
                false
            }
        }

        private fun openFilePicker() {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "text/csv"
            startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                val selectedFileUri: Uri? = data?.data
                Log.d("qweqwe", selectedFileUri.toString())
                selectedFileUri?.let {
                    val csvDataList = readCsvFile(selectedFileUri)
                    csvDataList.forEach { csvData ->
                        Log.d("qweqwe", csvData.toString())
                    }
                }
            }
        }

        private fun readCsvFile(fileUri: Uri): List<CsvData> {
            val csvDataList = mutableListOf<CsvData>()

            try {
                requireContext().contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        var isFirstLine = true
                        while (reader.readLine().also { line = it } != null) {
                            if (isFirstLine) {
                                isFirstLine = false
                                continue // Skip header row
                            }
                            val csvFields = line?.split(",") ?: continue
                            if (csvFields.size >= 4) { // Check if the row has at least 4 fields
                                val title = csvFields[1].trim()
                                val position = csvFields[2].trim()
                                val distance = csvFields[3].trim()
                                val iconImageUrl = csvFields[4].trim()
                                val csvData = CsvData(title, position, distance, iconImageUrl)
                                csvDataList.add(csvData)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return csvDataList
        }


        companion object {
            private const val STORAGE_PERMISSION_REQUEST_CODE = 1
            private const val FILE_PICKER_REQUEST_CODE = 2
        }
    }
}

data class CsvData(
    val title: String,
    val position: String? = null,
    val distance: String,
    val iconImageUrl: String? = null
)
