package com.uc.ccs.visuals.screens.admin.tabs.users

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.uc.ccs.visuals.databinding.FragmentUsersListBinding
import com.uc.ccs.visuals.screens.admin.AdminDashboardViewModel

/**
 * A fragment representing a list of Items.
 */
class UsersFragment : Fragment() {

    private var columnCount = 1

    private var _binding: FragmentUsersListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminDashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersListBinding.inflate(inflater, container, false)

        binding.list.apply {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
        }

        viewModel = ViewModelProvider(requireActivity()).get(AdminDashboardViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) {
            binding.list.adapter = UsersRecyclerViewAdapter(it)
        }
    }

}