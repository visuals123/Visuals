package com.uc.ccs.visuals.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSignListDialogListDialogBinding
import com.uc.ccs.visuals.screens.main.models.MarkerInfo

class SignListDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSignListDialogListDialogBinding? = null
    private val binding get() = _binding!!

    private val markerInfos = mutableListOf<MarkerInfo>()

    private lateinit var viewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignListDialogListDialogBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MarkerInfoAdapter()
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class MarkerInfoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val markerInfoList = mutableListOf<Any>()

        init {
            viewModel.markers.observe(viewLifecycleOwner) { markers ->
                submitList(markers)
            }
        }

        private fun submitList(list: List<MarkerInfo>) {
            markerInfoList.clear()

            val groupedMarkers = list.groupBy { markerInfo ->
                if (markerInfo.isWithinRadius) {
                    HeaderTitle.NEAREST_SIGNS.value
                } else {
                    HeaderTitle.INCOMING_SIGNS.value
                }
            }

            groupedMarkers.forEach { (groupTitle, markers) ->
                markerInfoList.add(groupTitle)
                markerInfoList.addAll(markers)
            }

            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_HEADER -> {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_header, parent, false)
                    HeaderViewHolder(itemView)
                }
                else -> {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_marker_info, parent, false)
                    MarkerInfoViewHolder(itemView)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is HeaderViewHolder -> {
                    holder.bind(markerInfoList[position] as String)
                }
                is MarkerInfoViewHolder -> {
                    val markerInfo = markerInfoList[position] as MarkerInfo
                    holder.bind(markerInfo)
                }
            }
        }

        override fun getItemCount(): Int {
            return markerInfoList.size
        }

        override fun getItemViewType(position: Int): Int {
            return if (markerInfoList[position] is String) {
                VIEW_TYPE_HEADER
            } else {
                VIEW_TYPE_MARKER_INFO
            }
        }

        inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)

            fun bind(header: String) {
                headerTextView.text = header
            }
        }

        inner class MarkerInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val distanceTextView: TextView = itemView.findViewById(R.id.distanceTextView)

            fun bind(markerInfo: MarkerInfo) {
                titleTextView.text = markerInfo.title
                distanceTextView.text = markerInfo.distance.toString()

                // Access other properties of the markerInfo object as needed
            }
        }
    }

    enum class HeaderTitle(val value: String) {
        NEAREST_SIGNS("Nearest signs"),
        INCOMING_SIGNS("Incoming signs")
    }

    companion object {
        // Function to create a new instance of the fragment

        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_MARKER_INFO = 1

        fun newInstance(): SignListDialogFragment {
            return SignListDialogFragment()
        }
    }
}