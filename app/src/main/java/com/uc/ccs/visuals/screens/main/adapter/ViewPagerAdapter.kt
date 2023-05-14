package com.uc.ccs.visuals.screens.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.ViewPagerItemBinding
import com.uc.ccs.visuals.screens.main.models.MarkerInfo

class ViewPagerAdapter(private val context: Context, private val items: List<MarkerInfo>,
                       val onClick: () -> Unit
) : PagerAdapter() {

    private var _binding: ViewPagerItemBinding? = null
    private val binding get() = _binding!!

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        _binding = ViewPagerItemBinding.inflate(inflater, container, false)

        val item = items[position]

        with(binding) {
            iconImageView.setImageResource(R.drawable.ic_road_sign_1)
            tvDistance.text = formatDistance(item.distance)
            tvTitle.text = item.title
            tvDescription.text = item.description

            ivClose.setOnClickListener {
                onClick.invoke()
            }

            container.addView(root)
        }

        return binding.root
    }

    private fun formatDistance(distance: Double): String {
        if (distance > 1)
            return kotlin.math.ceil(distance).toInt().toString().plus(" Meters")
        return kotlin.math.ceil(distance).toInt().toString().plus(" Meter")
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }
}