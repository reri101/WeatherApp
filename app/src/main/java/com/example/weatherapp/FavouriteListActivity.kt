package com.example.weatherapp

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ActivityFavouriteListBinding

class FavouriteListActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityFavouriteListBinding
    private lateinit var locationAdapter: LocationAdapter
    private val locationList: MutableList<String> = mutableListOf()
    private val filteredLocationList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFavouriteListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadLocations()

        binding.searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterLocations(newText)
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter(locationList)
        binding.recyclerView.apply {
            adapter = locationAdapter
            layoutManager = LinearLayoutManager(this@FavouriteListActivity)
        }
    }

    private fun loadLocations() {
        sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE)
        val savedLocations = sharedPreferences.getStringSet("locations", setOf())?.toList()
        if (savedLocations != null) {
            locationList.addAll(savedLocations)
            filteredLocationList.addAll(savedLocations)
            locationAdapter.notifyDataSetChanged()
        }
    }

    private fun saveLocations() {
        sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("locations", locationList.toSet())
        editor.apply()
    }

    private fun filterLocations(query: String?) {
        filteredLocationList.clear()
        if (query.isNullOrEmpty()) {
            filteredLocationList.addAll(locationList)
        } else {
            locationList.forEach { location ->
                if (location.contains(query, true)) {
                    filteredLocationList.add(location)
                }
            }
        }
        locationAdapter.notifyDataSetChanged()
    }

    inner class LocationAdapter(private val locations: List<String>) : RecyclerView.Adapter<LocationViewHolder>(), Filterable {
        private var filteredLocations: MutableList<String> = locations.toMutableList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
            return LocationViewHolder(view)
        }

        override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
            holder.bind(filteredLocations[position])
        }

        override fun getItemCount(): Int = filteredLocations.size

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val filteredList = if (constraint.isNullOrEmpty()) {
                        locations
                    } else {
                        locations.filter { it.contains(constraint, true) }
                    }
                    val filterResults = FilterResults()
                    filterResults.values = filteredList
                    return filterResults
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredLocations.clear()
                    filteredLocations.addAll(results?.values as List<String>)
                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)

        fun bind(location: String) {
            locationTextView.text = location
        }
    }

    override fun onPause() {
        super.onPause()
        saveLocations()
    }
}
