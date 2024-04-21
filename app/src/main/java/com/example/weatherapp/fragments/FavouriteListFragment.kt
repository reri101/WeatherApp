package com.example.weatherapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import android.widget.SearchView
import com.example.weatherapp.adapter.CityAdapter
import com.google.android.material.tabs.TabLayout

class FavouriteListFragment : Fragment(), CityAdapter.OnDeleteClickListener, CityAdapter.OnWeatherClickListener {

    private lateinit var adapter: CityAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite_list, container, false)

        // Inicjalizacja SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("FavCities4", Context.MODE_PRIVATE)

        // Inicjalizacja RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = CityAdapter(this)
        adapter.setOnWeatherClickListener(this)


        // Inicjalizacja SearchView
        searchView = view.findViewById(R.id.searchView)

        // Inicjalizacja adaptera i przypisanie do RecyclerView
        setupRecyclerView()

        // Wczytaj listę ulubionych miast z SharedPreferences
        val favouriteCitiesSet = sharedPreferences?.getStringSet("favoriteCities", emptySet()) ?: emptySet()
        if (favouriteCitiesSet != null) {
            for (city in favouriteCitiesSet) {
                Log.d("cities", "$city")
                adapter.addCity(city)
            }
        }

        // Obsługa zdarzeń dla SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Sprawdź, czy zdarzenie jest wywoływane po naciśnięciu Enter
                Log.d("AddCity", "XXX")
                if (!query.isNullOrEmpty()) {
                    // Wywołaj metodę do dodawania miasta do RecyclerView
                    addCityToList(query)
                    searchView.setQuery("", false)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("AddCity", "X")
                // Tutaj możesz reagować na zmiany tekstu w SearchView, jeśli to potrzebne
                return false
            }
        })

        return view

    }
    // Metoda obsługująca kliknięcie przycisku "Pogoda"
    override fun onWeatherClick(position: Int) {
        // Pobierz nazwę miasta z adaptera na podstawie pozycji
        val cityName = adapter.getCity(position)
        // Przejdź do fragmentu z informacjami o pogodzie, przekazując nazwę miasta
        val weatherFragment = WeatherFragment.newInstance(cityName)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.constLayout, weatherFragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }

    override fun onDeleteClick(position: Int) {
        removeCityFromList(position)
    }

    private fun setupRecyclerView() {

        // Ustaw adapter dla RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun addCityToList(cityName: String) {
        // Sprawdź, czy miasto nie istnieje już na liście
        if (!adapter.containsCity(cityName)) {
            // Dodaj nowe miasto do listy miast w adapterze
            adapter.addCity(cityName)
            // Zapisz listę ulubionych miast do SharedPreferences
            val favouriteCitiesSet = sharedPreferences.getStringSet("favoriteCities", emptySet())?.toMutableSet() ?: mutableSetOf()
            favouriteCitiesSet.add(cityName)
            val editor = sharedPreferences.edit()

            editor?.putStringSet("favoriteCities", favouriteCitiesSet)?.apply()
        }
    }

    // Usuń miasto z listy na podstawie jego pozycji
    private fun removeCityFromList(position: Int) {
        val cityName = adapter.getCity(position)
        adapter.removeCity(position)
        // Usuń miasto z listy ulubionych miast w SharedPreferences
        val favouriteCitiesSet = sharedPreferences.getStringSet("favoriteCities", emptySet())?.toMutableSet() ?: mutableSetOf()
        favouriteCitiesSet.remove(cityName)
        val editor = sharedPreferences.edit()
        editor?.putStringSet("favoriteCities", favouriteCitiesSet)?.apply()
    }
}
