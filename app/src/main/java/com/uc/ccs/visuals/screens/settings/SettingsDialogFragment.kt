package com.uc.ccs.visuals.screens.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSettingsDialogBinding
import com.uc.ccs.visuals.screens.main.VehicleType
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class SettingsDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSettingsDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestoreViewModel: FirestoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsDialogBinding.inflate(inflater, container, false)
        firestoreViewModel = ViewModelProvider(requireActivity()).get(FirestoreViewModel::class.java)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_settings, SettingsFragment(firestoreViewModel))
            .commit()

        with(firestoreViewModel) {
            operationState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is FirestoreViewModel.OperationState.Success -> {
                        Toast.makeText(requireContext(), getString(R.string.successfully_uploaded), Toast.LENGTH_SHORT).show()
                    }
                    FirestoreViewModel.OperationState.Loading -> {
                        //show loading dialog
                    }
                    is FirestoreViewModel.OperationState.Error -> {
                        val exception = state.exception
                        Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
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

     class SettingsFragment(private val firestoreViewModel: FirestoreViewModel) : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            // Listen for preference clicks
            findPreference<Preference>("logout_preference")?.setOnPreferenceClickListener {
                // Handle logout click
                SharedPreferenceManager.clearCurrentUser(requireContext())
                FirebaseAuthManager.signOut()
                findNavController().navigate(R.id.action_settingsDialogFragment_to_loginFragment)
                true
            }

            /**
             * [05/29/2023] Temporary disabled
             * */
            findPreference<Preference>(getString(R.string.text_import))?.setOnPreferenceClickListener {
                // Handle CSV import click
                if (isStoragePermissionGranted()) {
                    openFilePicker()
                } else {
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
                selectedFileUri?.let {
                    val csvDataList = readCsvFile(selectedFileUri)
                    firestoreViewModel.saveCsvData(data = csvDataList)
                    csvDataList.forEach { csvData ->

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
                        var lineCount = 0
                        while (reader.readLine().also { line = it } != null) {
                            if (lineCount < 2) {
                                lineCount++
                                continue // Skip rows
                            }

                            val csvFields = line?.split(",") ?: continue
                            if (csvFields.size >= 6) { // Check if the row has at least 6 fields
                                val id = csvFields[0].trim()
                                val code = csvFields[1].trim()
                                val title = csvFields[2].trim()
                                val description = csvFields[3].trim()
                                val lat = csvFields[4].trim().removePrefix("\"")
                                val long = csvFields[5].trim().removeSuffix("\"")
                                val iconImageUrl = csvFields[6].trim()
                                val vehicleType = csvFields[7].trim()

                                val csvData = CsvData(
                                    id = id,
                                    code = code,
                                    title = title,
                                    description = description,
                                    position = "$lat-$long",
                                    iconImageUrl = iconImageUrl,
                                    vehicleType = vehicleType
                                )
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
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1
        private const val FILE_PICKER_REQUEST_CODE = 2
    }
}

data class CsvData(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val position: String,
    val iconImageUrl: String? = null,
    val vehicleType: String
)
