package com.uc.ccs.visuals.screens.admin

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.data.CsvDataRepository
import com.uc.ccs.visuals.databinding.FragmentAdminDashboardBinding
import com.uc.ccs.visuals.factories.AdminDashboardViewModelFactory
import com.uc.ccs.visuals.screens.admin.tabs.signs.SignFragment
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.admin.tabs.users.UsersFragment
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.screens.settings.CsvData
import com.uc.ccs.visuals.utils.extensions.toMarkerInfoList
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class AdminDashboardFragment : Fragment() {

    private lateinit var viewModel: AdminDashboardViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var firestoreViewModel: FirestoreViewModel

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuHost: MenuHost
    private lateinit var dialog: Dialog

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.csvClient.onActivityResult(requireContext(), result)
    }

    private val onSuccess: (List<UserItem>) -> Unit = {
        viewModel.setUsers(it)
    }

    private val onFailure: (e: Exception) -> Unit = {
        Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        val repository = CsvDataRepository(requireContext())
        val viewModelFactory = AdminDashboardViewModelFactory(repository)

        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(AdminDashboardViewModel::class.java)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        firestoreViewModel = ViewModelProvider(requireActivity()).get(FirestoreViewModel::class.java)

        with(binding) {
            tvTitleName.text = getString(R.string.admin_header, SharedPreferenceManager
                .getCurrentUser(requireContext())?.firstName ?: getString(R.string.admin))
        }

        viewModel.setContext(requireContext())

        return binding.root
    }

    private fun setupMenuBar() {
        menuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_nav, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_logout -> {
                        SharedPreferenceManager.clearCurrentUser(requireContext())
                        FirebaseAuthManager.signOut()
                        findNavController().navigate(R.id.action_adminDashboardFragment_to_loginFragment)
                        true
                    }
                    R.id.menu_import -> {
                        if (isStoragePermissionGranted()) {
                            openFilePicker()
                        } else {
                            openFilePicker()
                        }
                        true
                    }
                    R.id.menu_export -> {
                        showExportOptionsDialog()
                        true
                    }
                    else -> true
                }
            }

        })
    }

    private fun showExportOptionsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        viewModel.csvClient.launchBaseDirectoryPicker(launcher)
        builder.setTitle("Choose an option to export")
            .setItems(arrayOf("Export Users", "Export Signs")) { dialog, which ->
                when (which) {
                    0 -> {
                        viewModel.restoreSavedUri(requireContext())
                        viewModel.exportUsersToCSV()
                    }
                    1 -> {
                        viewModel.exportMarkersToCSV()
                    }
                }
            }

        // Show the dialog
        builder.create().show()
    }

    private fun showLoadingDialog() {
        dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.progress_dialog)
            setCancelable(false)
            show()
        }
    }

    private fun showLoadingDialogImport() {
        dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.progress_dialog_import)
            setCancelable(false)
            show()
        }
    }

    private fun dismissDialog() {
        dialog.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenuBar()
        setupObservers()
        setupViews()
    }

    private fun setupViews() {
        val adapter = TabPagerAdapter(requireActivity().supportFragmentManager)
        adapter.addFragment(UsersFragment(), TabNames.USERS.value)
        adapter.addFragment(SignFragment(), TabNames.SIGN.value)

        with(binding) {
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)
        }
    }

    private fun setupObservers() {
        with(binding) {
            viewModel.users.observe(viewLifecycleOwner) {
                totalCount1.text = it.count().toString()
            }

            mapViewModel.allMarkers.observe(viewLifecycleOwner) {
                totalCount2.text = it.count().toString()
            }

            with(viewModel) {
                csvExportDataState.observe(viewLifecycleOwner) { state ->
                    when(state) {
                        CsvExportDataState.onLoad -> {
                            showLoadingDialog()
                        }

                        is CsvExportDataState.onFailure -> {
                            dialog.dismiss()
                            Toast.makeText(requireContext(), state.e, Toast.LENGTH_SHORT).show()
                        }

                        CsvExportDataState.onSuccess -> {
                            dialog.dismiss()
                            Toast.makeText(requireContext(), getString(R.string.successfully_exported), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            with(firestoreViewModel) {
                operationState.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        is FirestoreViewModel.OperationState.Success -> {
                            viewModel.insertCsvDataToLocalDb(state.csvData)
                            Toast.makeText(requireContext(), getString(R.string.successfully_uploaded), Toast.LENGTH_SHORT).show()
                            dismissDialog()
                        }
                        FirestoreViewModel.OperationState.Loading -> {
                            showLoadingDialogImport()
                        }
                        is FirestoreViewModel.OperationState.Error -> {
                            val exception = state.exception
                            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                            dismissDialog()
                        }
                    }
                }

                viewModel.insertCsvDataToLocalDbState.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        is LocalDBState.Success -> {
                            mapViewModel.setAllMarkers(state.csvData.toMarkerInfoList())
                        }

                        is LocalDBState.Error -> {
                            Toast.makeText(requireContext(), state.exception.localizedMessage, Toast.LENGTH_SHORT).show()
                        }

                        LocalDBState.Loading -> {}
                    }
                }
            }
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
        intent.type = "*/*"
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedFileUri: Uri? = data?.data

            if (selectedFileUri != null) {
                val csvDataList = readCsvFile(selectedFileUri)
                firestoreViewModel.saveCsvData(data = csvDataList)
            } else {
                Toast.makeText(requireContext(), "Invalid file selected", Toast.LENGTH_SHORT).show()
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
                        if (csvFields.size >= 8) { // Check if the row has at least 8 fields
                            val id = csvFields[0].trim()
                            val code = csvFields[1].trim()
                            val title = csvFields[2].trim()
                            val description = csvFields[3].trim()
                            val lat = csvFields[4].trim().removePrefix("\"")
                            val long = csvFields[5].trim().removeSuffix("\"")
                            val iconImageUrl = csvFields[6].trim()
                            val vehicleType = csvFields[7].trim()

                            // Validate required fields
                            if (id.isNotEmpty() && code.isNotEmpty() && title.isNotEmpty() &&
                                description.isNotEmpty() && lat.isNotEmpty() && long.isNotEmpty() &&
                                vehicleType.isNotEmpty()
                            ) {
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
                            } else {
                                // Log or handle invalid row data
                            }
                        } else {
                            // Log or handle rows with insufficient fields
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return csvDataList
    }


    class TabPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList = mutableListOf<Fragment>()
        private val fragmentTitleList = mutableListOf<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1
        private const val FILE_PICKER_REQUEST_CODE = 2
    }

}

enum class TabNames(val value: String) {
    SIGN("Signs"),
    USERS("Users")
}