package com.example.weatherapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import com.example.weatherapp.adapter.CityAdapter
import com.example.weatherapp.data.ApiInterface
import com.example.weatherapp.data.WeatherApp
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// odczytywanie z json gdy przy ladowaniu fragmentu czas od ostatniego pobhrania informacji z inrternetu nie wyniosl wymaganej liczby
// informacja o polaczeniu z internetem
// widok poziomy dla teleofnu
// widok pionowy dla tabletow podzielony na 3 zamiast viewpagera
// widok poziomy dla tabletu
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
        val favouriteCitiesSet = sharedPreferences.getStringSet("favoriteCities", emptySet()) ?: emptySet()
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
                    checkCityAvailabilityAndAddToList(query)
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
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            // Możesz dodać inne typy transportu, jeśli są potrzebne
            else -> false
        }
    }
    // Metoda obsługująca kliknięcie przycisku "Pogoda"
    override fun onWeatherClick(position: Int) {
        sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        editor?.putString("city_setted", adapter.getCity(position))
        editor?.apply()

        val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tabLayout)
        val pogodaTab = tabLayout.getTabAt(1)
        pogodaTab?.select()


        // Pobierz nazwę miasta z adaptera na podstawie pozycji
        val cityName = adapter.getCity(position)
        // Przejdź do fragmentu z informacjami o pogodzie, przekazując nazwę miasta
        val weatherFragment = WeatherFragment.newInstance(cityName)
        Log.d("citynamee", "cn1 $cityName")
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
        }else{
            view?.let { Snackbar.make(it, "Podane miasto już znajduje się na liście ulubionych.", Snackbar.LENGTH_SHORT).show() }
        }
    }
    private fun checkCityAvailabilityAndAddToList(cityName: String) {
        if (isNetworkAvailable()) {
            testFetchWeatherData(cityName, "metric") { isAvailable ->
                if (isAvailable) {
                    addCityToList(cityName)
                } else {
                    Toast.makeText(requireContext(), "Nie można odczytać danych o pogodzie dla $cityName", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
        }
    }
    private fun testFetchWeatherData(cityName: String, units: String, onResult: (Boolean) -> Unit) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName, "pl", "bd04d1ce49301ed0175976c62138cd19", units)
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                onResult(false)
            }
        })
    }

    private fun removeCityFromList(position: Int) {
        val cityName = adapter.getCity(position)
        adapter.removeCity(position)
        val favouriteCitiesSet = sharedPreferences.getStringSet("favoriteCities", emptySet())?.toMutableSet() ?: mutableSetOf()
        favouriteCitiesSet.remove(cityName)
        val editor = sharedPreferences.edit()
        editor?.putStringSet("favoriteCities", favouriteCitiesSet)?.apply()
    }
}
