package com.example.weatherapp.adapter


import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weatherapp.fragments.AdvanceWeatherFragment
import com.example.weatherapp.fragments.BasicWeatherFragment
import com.example.weatherapp.fragments.NextDaysWeatherFragment
import com.example.weatherapp.fragments.WeatherFragment

class FragmentPagedAdapter(
    fragmentActivity: FragmentActivity,
    private var cityName: String?
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        Log.d("createFragment", "XXXXX ")

        Log.d("citynamee", "cn0 $cityName")
        return if (position == 0)
            BasicWeatherFragment()
        else if (position == 1)
            AdvanceWeatherFragment().newInstance(cityName)
        else{
            NextDaysWeatherFragment().newInstance(cityName)
        }

        Log.d("createFragment", "XXXXX-- ")
    }
}