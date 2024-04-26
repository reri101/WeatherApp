package com.example.weatherapp.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import java.util.ArrayList

class CityAdapter(private val listener: OnDeleteClickListener, private val cities: MutableList<String> = mutableListOf()) : RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    private lateinit var weatherClickListener: OnWeatherClickListener
    private lateinit var sharedPreferences: SharedPreferences

    fun setOnWeatherClickListener(listener: OnWeatherClickListener) {
        this.weatherClickListener = listener
    }
    // Definicja interfejsu dla kliknięcia przycisku "Usuń"
    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    interface OnWeatherClickListener {

        fun onWeatherClick(position: Int)
    }

    // Tworzy nowe widoki (wywoływane przez layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return ViewHolder(view)
    }

    // Zastępuje zawartość widoku (wywoływane przez layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = cities[position]
        holder.bind(city)
        holder.deleteButton.setOnClickListener {
            listener.onDeleteClick(position) // Wywołanie metody onDeleteClick interfejsu
        }
    }

    // Zwraca rozmiar danych (wywoływane przez layout manager)
    override fun getItemCount(): Int {
        return cities.size
    }

    // Metoda do dodawania miasta do listy
    fun addCity(city: String) {
        val s = cities.size
        Log.d("citiesCounter", "$s")
        cities.add(city)
        notifyItemInserted(cities.size - 1)
    }

    fun removeCity(position: Int) {
        if (position in 0 until cities.size) {
            cities.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount - position)
        }
    }

    // Metoda do sprawdzania, czy miasto już istnieje na liście
    fun containsCity(city: String): Boolean {
        return cities.contains(city)
    }

    // Metoda zwracająca nazwę miasta na podstawie pozycji
    fun getCity(position: Int): String {
        return cities[position]
    }

    // Klasa ViewHolder, która przechowuje widok każdego elementu listy
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cityTextView: TextView = itemView.findViewById(R.id.cityTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(city: String) {
            cityTextView.text = city
        }

        init {
            // Inicjalizacja przycisku "Pogoda" i dodanie obsługi kliknięcia
            itemView.findViewById<Button>(R.id.weatherButton).setOnClickListener {

                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    weatherClickListener.onWeatherClick(position)
                }
            }
        }

    }
}
