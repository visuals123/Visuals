package com.uc.ccs.visuals.screens.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        val imageResource = getRoadSignImageResource(item.code)

        with(binding) {
            // Use the image resource in your code, for example, with Glide:
            Glide.with(context)
                .load(imageResource)
                .into(iconImageView)
            tvDistance.text = formatDistance(item.distance ?: 0.0)
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

    enum class RoadSignCode(val code: String, val imageResource: Int) {
        R1_1("R1-1", R.drawable.sign_stop_sign),
        R1_2("R1-2", R.drawable.sign_yield_sign),
        R2_1("R2-1", R.drawable.sign_one_way_sign),
        R3_1("R3-1", R.drawable.sign_no_entry_sign),
        R3_15("R3-15", R.drawable.sign_no_u_turn_sign),
        R3_14("R3-14", R.drawable.sign_no_left_turn_sign),
        R3_13("R3-13", R.drawable.sign_no_right_turn_sign),
        R3_16("R3-16", R.drawable.sign_no_overtake_sign),
        R4_1("R4-1", R.drawable.sign_speed_limit_sign),
        R4_3("R4-3", R.drawable.sign_minimum_speed_limit_sign),
        R5_1S("R5-1S", R.drawable.sign_no_parking_sign)
    }

    fun getRoadSignImageResource(code: String): Int {
        val roadSign = RoadSignCode.values().find { it.code == code }
        return roadSign?.imageResource ?: R.drawable.sign_loading
    }
}