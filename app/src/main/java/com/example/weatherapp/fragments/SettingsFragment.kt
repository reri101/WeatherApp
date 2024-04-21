import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.weatherapp.R

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)

        // Inicjalizacja widoków
        val temperatureUnitRadioGroup: RadioGroup = view.findViewById(R.id.temperatureUnitRadioGroup)
        val refreshFrequencyEditText: EditText = view.findViewById(R.id.refreshFrequencyEditText)

        // Ustaw obecne wartości
        val currentTemperatureUnit = sharedPreferences.getString("temperatureUnit", "metric")
        when (currentTemperatureUnit) {
            "metric" -> temperatureUnitRadioGroup.check(R.id.celsiusRadioButton)
            "imperial" -> temperatureUnitRadioGroup.check(R.id.fahrenheitRadioButton)
        }

        val currentRefreshFrequency = sharedPreferences.getInt("refreshFrequency", 6)
        refreshFrequencyEditText.setText(currentRefreshFrequency.toString())

        // Obsługa zmiany jednostki temperatury
        temperatureUnitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedUnit = if (checkedId == R.id.celsiusRadioButton) "metric" else "imperial"
            sharedPreferences.edit().putString("temperatureUnit", selectedUnit).apply()
        }

        // Obsługa zmiany częstotliwości odświeżania
        refreshFrequencyEditText.addTextChangedListener {
            val frequencyText = it.toString()
            val frequency = frequencyText.toIntOrNull()
            if (frequency != null && frequency > 0) {
                sharedPreferences.edit().putInt("refreshFrequency", frequency).apply()
            } else {
                Toast.makeText(requireContext(), "Podaj poprawną liczbę godzin", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
