package com.uc.ccs.visuals.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSignListDialogListDialogBinding
import com.uc.ccs.visuals.screens.main.models.MarkerInfo

class SignListDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSignListDialogListDialogBinding? = null
    private val binding get() = _binding!!

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
        with(binding) {
            with(viewModel) {
                list.apply {

                    btnStartRide.setOnClickListener {
                        setStartARide(true)
                        dismiss()
                    }

                    markers.observe(viewLifecycleOwner) { markers ->
                        if (markers.isNotEmpty()) {
                            showList(true)

                            val marketAdapter = MarkerInfoAdapter().apply {
                                submitList(markers)
                            }

                            layoutManager = LinearLayoutManager(context)
                            adapter = marketAdapter

                        } else {
                            showList(false)
                        }
                    }

                    layoutManager = LinearLayoutManager(context)
                    adapter = MarkerInfoAdapter()
                    addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                }
            }
        }
    }

    private fun showList(bool: Boolean) {
        binding.apply {
            clNotEmptyNotification.isVisible = bool
            clEmptyNotification.isVisible = bool.not()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class MarkerInfoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val markerInfoList = mutableListOf<Any>()

        fun submitList(list: List<MarkerInfo>) {
            markerInfoList.clear()

            val groupedMarkers = list.groupBy { markerInfo ->
                if (markerInfo.isWithinRadius) {
                    HeaderTitle.NEAREST_SIGNS.value
                } else {
                    HeaderTitle.INCOMING_SIGNS.value
                }
            }

            val sortedMarkers = mutableListOf<Any>()
            groupedMarkers.entries.sortedBy { entry ->
                if (entry.key == HeaderTitle.NEAREST_SIGNS.value) {
                    -1
                } else {
                    1
                }
            }.forEach { (groupTitle, markers) ->
                sortedMarkers.add(groupTitle)
                sortedMarkers.addAll(markers)
            }

            markerInfoList.addAll(sortedMarkers)

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
            private val ivImage: ImageView = itemView.findViewById(R.id.iv_image)
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val distanceTextView: TextView = itemView.findViewById(R.id.distanceTextView)

            fun bind(markerInfo: MarkerInfo) {
                titleTextView.text = markerInfo.title
                distanceTextView.text = markerInfo.distance.toString()

                Glide.with(itemView)
                    .load(markerInfo.iconImageUrl) // Replace with your image URL or resource
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivImage)
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