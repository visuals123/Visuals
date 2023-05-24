package com.uc.ccs.visuals.screens.admin

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentAdminDashboardBinding
import com.uc.ccs.visuals.screens.admin.tabs.signs.SignFragment
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.admin.tabs.users.UsersFragment
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.screens.settings.CsvData
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import com.uc.ccs.visuals.utils.sharedpreference.SharedPreferenceManager

class AdminDashboardFragment : Fragment() {

    private lateinit var viewModel: AdminDashboardViewModel
    private lateinit var mapViewModel: MapViewModel

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuHost: MenuHost

    private val onSuccess: (List<UserItem>) -> Unit = {
        viewModel.setUsers(it)
    }

    private val onFailure: (e: Exception) -> Unit = {
        Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(requireActivity()).get(AdminDashboardViewModel::class.java)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

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
                    else -> true
                }
            }

        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            mapViewModel.markers.observe(viewLifecycleOwner) {
                totalCount2.text = it.count().toString()
            }
        }
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

}

enum class TabNames(val value: String) {
    SIGN("Signs"),
    USERS("Users")
}