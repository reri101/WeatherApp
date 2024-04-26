package com.example.weatherapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.dataND.NextDayWeatherItem

class NextDaysWeatherAdapter(private val nextDayWeatherList: List<NextDayWeatherItem>) : RecyclerView.Adapter<NextDaysWeatherAdapter.NextDayWeatherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NextDayWeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_next_day, parent, false)
        return NextDayWeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: NextDayWeatherViewHolder, position: Int) {
        holder.bind(nextDayWeatherList[position])
    }

    override fun getItemCount(): Int = nextDayWeatherList.size

    class NextDayWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(nextDayWeather: NextDayWeatherItem) {
            itemView.apply {
                itemView.findViewById<TextView>(R.id.dayDateText).text = nextDayWeather.dayDate
                itemView.findViewById<TextView>(R.id.tempDisplayForeCast).text = nextDayWeather.temperature
                itemView.findViewById<TextView>(R.id.weatherDescr).text = nextDayWeather.weatherDescription
                itemView.findViewById<TextView>(R.id.windSpeed).text = nextDayWeather.windSpeed
                itemView.findViewById<TextView>(R.id.humidity).text = nextDayWeather.humidity

                val imgG = itemView.findViewById<ImageView>(R.id.imageGraphic)
//                when(nextDayWeather.condition){
//                    "Clear Sky", "Sunny", "Clear" ->{
//                        imgG.setBackgroundResource(R.drawable.sunny)
//                    }
//
//                    "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Haze" ->{
//                        imgG.setBackgroundResource(R.drawable.cloud_black)
//                    }
//
//                    "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" ->{
//                        imgG.setBackgroundResource(R.drawable.rain)
//                    }
//
//                    "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
//                        imgG.setBackgroundResource(R.drawable.snow)
//                    }
//                    else ->{
//                        imgG.setBackgroundResource(R.drawable.sunny)
//                    }
//                }
            }
        }
    }
}
