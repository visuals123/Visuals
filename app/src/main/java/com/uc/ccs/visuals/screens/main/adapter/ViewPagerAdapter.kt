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
            iconImageView.setImageResource(imageResource)

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
        R1_2B("R1-2B", R.drawable.sign_yield_sign),
        R1_2P1("R1-2P1", R.drawable.sign_yield_sign),
        R1_2P2("R1-2P2", R.drawable.sign_yield_sign),
        R2_1("R2-1", R.drawable.sign_one_way_sign),
        R2_3("R2-3", R.drawable.sign_direction_sign),
        R2_6("R2-6", R.drawable.sign_merging_traffic_sign),
        R2_6P("R2-6P", R.drawable.sign_merging_traffic_sign),
        R3_1("R3-1", R.drawable.sign_no_entry_sign),
        R3_15("R3-15", R.drawable.sign_no_u_turn_sign),
        R3_14("R3-14", R.drawable.sign_no_left_turn_sign),
        R3_13("R3-13", R.drawable.sign_no_right_turn_sign),
        R3_16("R3-16", R.drawable.sign_no_overtake_sign),
        R3_1PA("R3-1PA", R.drawable.sign_no_entry_sign),
        R4_1("R4-1", R.drawable.sign_speed_limit_sign),
        R4_1_60("R4-1-60", R.drawable.sign_speed_limit_sign_60),
        R4_1_30("R4-1-30", R.drawable.sign_speed_limit_sign_30),
        R4_1_80("R4-1-80", R.drawable.sign_speed_limit_sign_80),
        R4_2PM_40("R4-2PM-40", R.drawable.sign_no_overtaking_sign_40),
        R4_2PM_60("R4-2PM-60", R.drawable.sign_no_overtaking_sign_60),
        R4_3("R4-3", R.drawable.sign_minimum_speed_limit_sign),
        R5_10("R5-10", R.drawable.sign_do_not_block_intersection_sign),
        R5_1S("R5-1S", R.drawable.sign_no_parking_sign),
        R5_2P("R5-2P", R. drawable.sign_emergency_parking_sign),
        R5_2PA("R5-2PA", R.drawable.sign_emergency_parking_sign),
        R6_11("R6-11", R.drawable.sign_wheel_chair_crossing_sign),
        R6_8("R6-8", R.drawable.sign_pedestrians_crossing_sign),
        S1_3("S1-3", R.drawable.sign_trucks_on_low_gear_sign),
        S1_4("S1-4", R.drawable.sign_check_brakes_sign),
        SM_1("SM-1", R.drawable.sign_below_400cc_single_line_only_sign),
        SM_2("SM-2", R.drawable.sign_below_400cc_lane_ends_here_sign),
        W1_3("W1-3", R.drawable.sign_curve_sign),
        W2_4("W2-4", R.drawable.sign_t_junction_sign),
        W3_1("W3-1", R.drawable.sign_traffic_lights_ahead_sign),
        W5_3("W5-3", R.drawable.sign_hump_sign),
        W5_4("W5-4", R.drawable.sign_steep_descent_sign),
        W7_4A("W7-4A", R.drawable.sign_runaway_truck_ramp_1km),
        W7_4B("W7-4B", R.drawable.sign_runaway_truck_ramp_500m),
        W7_4C("W7-4C", R.drawable.sign_truck_escape_ramp),
        W7_4L("W7-4L", R.drawable.sign_runaway_truck_ramp_left),
        W7_4R("W7-4R", R.drawable.sign_runaway_truck_ramp_right),
        W8_3A("W8-3A", R.drawable.sign_hump_in_next_50m_sign),
        W9_5("W9-5", R.drawable.sign_slow_down_weighbridge_ahead_sign),
        G1_1("G1-1", R.drawable.sign_advance_direction_sign),
        G1_2R("G1-2R", R.drawable.sign_cclex_toll_road_cebu_sign_2right),
        G2_1A("G2-1A", R.drawable.sign_cordova_direction_sign),
        G2_1B("G2-1B", R.drawable.sign_MCIA_direction_sign),
        G2_1L("G2-1L", R.drawable.sign_cclex_toll_road_cebu_sign_left),
        G2_1R("G2-1R", R.drawable.sign_cclex_toll_road_cebu_sign_1right),
        G7_2("G7-2", R.drawable.sign_weighbridge_station),
        G7_4("G7-4", R.drawable.sign_emergency_parking_500m),
        G7_5("G7-5", R.drawable.sign_emergency_parking_right),
        GE1_2("GE1-2", R.drawable.sign_expressway_approach_sign),
        GE2_1A("GE2-1A", R.drawable.sign_prohibited_on_espressway_sign),
        GE2_1A_1("GE2-1A-1", R.drawable.sign_prohibited_on_espressway_sign_1a1),
        GE2_1C_1("GE2-1C-1", R.drawable.sign_prohibited_on_espressway_sign_1c1),
        GE2_2("GE2-2", R.drawable.sign_toll_charges_sign),
        GE3_1C("GE3-1C", R.drawable.sign_advance_exit_sign),
        GE3_1E("GE3-1E", R.drawable.sign_cclex_toll_road_cebu_exit_1km_sign),
        GE3_1F("GE3-1F", R.drawable.sign_cclex_toll_road_cebu_exit_500m_sign),
        GE7_1("GE7-1", R.drawable.sign_toll_plaza_ahead_2km),
        GE7_1A("GE7-1A", R.drawable.sign_toll_plaza_ahead_1km),
        GE7_2("GE7-2", R.drawable.sign_stop_at_all_toll_gates),
        GE8_2M("GE8-2M", R.drawable.sign_reduce_speed_sign),
        GE8_7("GE8-7", R.drawable.sign_slow_vehicles_on_right_lane_sign),
        GE8_8("GE8-8", R.drawable.sign_motorcycles_keep_right_sign)
    }

    private fun getRoadSignImageResource(code: String): Int {
        val roadSign = RoadSignCode.values().find { it.code == code }
        return roadSign?.imageResource ?: R.drawable.sign_loading
    }
}