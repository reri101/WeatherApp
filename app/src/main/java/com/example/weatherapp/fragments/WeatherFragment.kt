package com.example.weatherapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.adapter.FragmentPagedAdapter
import com.example.weatherapp.databinding.FragmentWeatherBinding
import com.google.android.material.tabs.TabLayout



/**
 * A simple [Fragment] subclass.
 * Use the [WeatherFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeatherFragment : Fragment() {
    private lateinit  var binding: FragmentWeatherBinding
    private lateinit var  viewPager2: ViewPager2
    private lateinit var  tabLayout: TabLayout
    private lateinit var adapter: FragmentPagedAdapter


    companion object {
        fun newInstance(cityName: String): WeatherFragment {
            val fragment = WeatherFragment()
            val args = Bundle()
            args.putString("cityName", cityName)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWeatherBinding.inflate(inflater, container, false)

        tabLayout = binding.tabLayout2
        viewPager2 = binding.viewPager
        val cityName = arguments?.getString("cityName")
        adapter = FragmentPagedAdapter(requireActivity(), cityName)
        viewPager2.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })

        return binding.root    }

}