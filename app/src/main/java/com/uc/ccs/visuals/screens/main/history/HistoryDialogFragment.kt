package com.uc.ccs.visuals.screens.main.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uc.ccs.visuals.data.CsvDataRepository
import com.uc.ccs.visuals.data.LocalHistory
import com.uc.ccs.visuals.databinding.FragmentHistoryDialogListDialogBinding
import com.uc.ccs.visuals.databinding.FragmentHistoryDialogListDialogItemBinding
import com.uc.ccs.visuals.factories.AdminDashboardViewModelFactory
import com.uc.ccs.visuals.utils.firebase.FirestoreViewModel


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
                    list.layoutManager = LinearLayoutManager(context)
                    list.adapter = HistoryItemAdapter(it) {
                        setSelectedHistory(it)
                        setIsFromHistory(true)
                        dismiss()
                    }
                }
            }
        }
    }

    private inner class ViewHolder constructor(binding: FragmentHistoryDialogListDialogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val text: TextView = binding.text
    }

    private inner class HistoryItemAdapter constructor(
        private val list: List<LocalHistory>,
        val onItemClick: (LocalHistory) -> Unit
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
            holder.text.text = item.id
            holder.text.setOnClickListener {
                onItemClick.invoke(item)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}