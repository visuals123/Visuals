package com.uc.ccs.visuals.screens.admin.tabs.signs

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.uc.ccs.visuals.R

import com.uc.ccs.visuals.screens.admin.tabs.signs.placeholder.PlaceholderContent.PlaceholderItem
import com.uc.ccs.visuals.databinding.FragmentSignBinding
import com.uc.ccs.visuals.screens.settings.CsvData

class SignRecyclerViewAdapter(
    private val values: List<CsvData>
) : RecyclerView.Adapter<SignRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentSignBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = position.plus(1).toString()
        holder.contentView.text = item.title
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentSignBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}