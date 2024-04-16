package com.example.weatherapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentBasicWeatherBinding
import com.example.weatherapp.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {
    private val binding: FragmentFirstBinding by lazy {
        FragmentFirstBinding.inflate(layoutInflater)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        val textView = view.findViewById<TextView>(R.id.textView2)
        textView.text = "XXX"
        textView.setTextColor(Color.parseColor("#FF0000")) // Ustawienie koloru tekstu na czerowny
        return view
    }
}
