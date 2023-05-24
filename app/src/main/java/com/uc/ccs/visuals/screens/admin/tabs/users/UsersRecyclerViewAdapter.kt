package com.uc.ccs.visuals.screens.admin.tabs.users

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uc.ccs.visuals.databinding.FragmentUsersBinding

data class UserItem(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val roles: Int = 2
)

class UsersRecyclerViewAdapter(
    private val values: List<UserItem>
) : RecyclerView.Adapter<UsersRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentUsersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = position.plus(1).toString()
        holder.contentView.text = "${item.firstName} ${item.lastName}"
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentUsersBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}