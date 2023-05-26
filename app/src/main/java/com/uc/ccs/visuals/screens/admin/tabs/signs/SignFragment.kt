package com.uc.ccs.visuals.screens.admin.tabs.signs

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSignListBinding
import com.uc.ccs.visuals.screens.admin.AdminDashboardViewModel
import com.uc.ccs.visuals.screens.admin.tabs.signs.placeholder.PlaceholderContent
import com.uc.ccs.visuals.screens.main.MapViewModel

/**
 * A fragment representing a list of Items.
 */
class SignFragment : Fragment() {

    private var columnCount = 1

    private var _binding: FragmentSignListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminDashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignListBinding.inflate(inflater, container, false)

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
        viewModel.signs.observe(viewLifecycleOwner) {
            binding.list.adapter = SignRecyclerViewAdapter(it)
        }
    }

}