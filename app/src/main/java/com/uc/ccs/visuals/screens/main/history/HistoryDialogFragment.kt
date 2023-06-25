package com.uc.ccs.visuals.screens.main.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.data.CsvDataRepository
import com.uc.ccs.visuals.data.LocalHistory
import com.uc.ccs.visuals.databinding.FragmentHistoryDialogListDialogBinding
import com.uc.ccs.visuals.databinding.FragmentHistoryDialogListDialogItemBinding
import com.uc.ccs.visuals.factories.AdminDashboardViewModelFactory
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel
import kotlin.math.min


class HistoryDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHistoryDialogListDialogBinding? = null

    private val binding get() = _binding!!

    private lateinit var firestoreViewModel: FirestoreViewModel

    private lateinit var viewmodel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val repository = CsvDataRepository(requireContext())
        val factory = AdminDashboardViewModelFactory(repository)

        _binding = FragmentHistoryDialogListDialogBinding.inflate(inflater, container, false)
        firestoreViewModel = ViewModelProvider(requireActivity()).get(FirestoreViewModel::class.java)
        viewmodel = ViewModelProvider(requireActivity(), factory).get(HistoryViewModel::class.java)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            with(viewmodel) {

                viewmodel.getLocalHistory()
                firestoreViewModel.getTravelRideHistory({
                    saveHistoryListToLocal(it.map { it.toLocalHistory() })
                    getLocalHistory()
                }, {

                })

                setObservers()
            }
        }
    }

    private fun setObservers() {
        with(binding) {
            with(viewmodel) {
                localAllHistory.observe(viewLifecycleOwner) {
                    if(it.isNotEmpty()) {
                        list.layoutManager = LinearLayoutManager(context)
                        list.adapter = HistoryItemAdapter(it,
                            onItemClick = { item ->
                                setSelectedHistory(item)
                                setIsFromHistory(true)
                                dismiss()
                            },
                            onItemDelete = {item, view, pos ->
                                showPopupMenu(item, view, pos)
                            }
                        )
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.no_history), Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
        }
    }

    private fun showPopupMenu(localHistory: LocalHistory, view: View, position: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.menu_history_item, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    firestoreViewModel.deleteTravelRideHistory(localHistory.id, {
                        viewmodel.deleteHistoryItem(localHistory)
                        binding.list.adapter?.notifyItemRemoved(position)
                        dismiss()
                    },{
                        Toast.makeText(requireContext(), getString(R.string.unable_to_delete_history_item), Toast.LENGTH_SHORT).show()
                        dismiss()
                    })
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private inner class ViewHolder constructor(binding: FragmentHistoryDialogListDialogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val container: ConstraintLayout = binding.clContainer
        val currentLocation: TextView = binding.currentLocationTextView
        val destination: TextView = binding.destinationTextView
        val timeStamp: TextView = binding.tvTimestamp
        val more: AppCompatImageView = binding.ivMore
    }

    private inner class HistoryItemAdapter constructor(
        private val list: List<LocalHistory>,
        val onItemClick: (LocalHistory) -> Unit,
        val onItemDelete: (LocalHistory,View, Int) -> Unit
    ) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                FragmentHistoryDialogListDialogItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.currentLocation.text = item.startDestinationName
            holder.destination.text = item.endDestinationName
            holder.timeStamp.text = item.timestamp

            holder.container.setOnClickListener {
                onItemClick(item)
            }
            holder.more.setOnClickListener {
                onItemDelete.invoke(item, it, position)
            }
        }

        override fun getItemCount(): Int {
            return min(10, list.size)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}