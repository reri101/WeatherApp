package com.example.weatherapp.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.R
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
//            Log.d("citynamee", "cn2 $cityName")
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWeatherBinding.inflate(inflater, container, false)

        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

        if (isTablet) {
            setupTabletLayout()
        } else {
            setupPhoneLayout()
        }



        return binding.root
    }

    fun setupTabletLayout(){
        val basicWeatherFragment = BasicWeatherFragment()
        val advanceWeatherFragment = AdvanceWeatherFragment()
        val nextDaysWeatherFragmentFragment = NextDaysWeatherFragment()

        replaceFrame(basicWeatherFragment, R.id.standardFrame)
        replaceFrame(advanceWeatherFragment, R.id.advanceFrame)
        replaceFrame(nextDaysWeatherFragmentFragment, R.id.nextDFrame)

    }
    fun setupPhoneLayout(){
        tabLayout = binding.tabLayout2!!
        viewPager2 = binding.viewPager!!
        val sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences.getString("city_setted", "Warsaw") ?: "Warsaw"
//        Log.d("citynamee", "cn3 $cityName")
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
    }
    private fun replaceFrame(fragment: Fragment, r: Int) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(r, fragment)
        fragmentTransaction.commit()
    }

}