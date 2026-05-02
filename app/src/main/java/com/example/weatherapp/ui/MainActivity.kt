package com.example.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.data.local.WeatherPreferences
import com.example.weatherapp.data.model.CurrentWeatherUi
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.ui.adapter.DailyForecastAdapter
import com.example.weatherapp.ui.adapter.HourlyForecastAdapter
import com.example.weatherapp.ui.viewmodel.WeatherBackground
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
import com.example.weatherapp.util.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var prefs: WeatherPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyAdapter: DailyForecastAdapter

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getDeviceLocation()
        } else {
            Toast.makeText(this, "Достъпът до локация е отказан", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = WeatherPreferences(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerViews()
        setupObservers()
        setupListeners()
        setupNotifications()

        viewModel.loadWeatherByCity(prefs.getLastCity())


        val isCelsius = prefs.isCelsius()
        viewModel.toggleUnit(isCelsius)
        binding.btnCelsius.isSelected = isCelsius
        binding.btnFahrenheit.isSelected = !isCelsius
    }

    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyForecastAdapter()
        binding.rvHourly.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }

        dailyAdapter = DailyForecastAdapter(isCelsius = prefs.isCelsius())
        binding.rvDaily.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = dailyAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewModel.currentWeather.observe(this) { weather ->
            weather?.let { updateCurrentWeatherUI(it) }
        }
        viewModel.dailyForecast.observe(this) { forecast ->
            dailyAdapter.submitList(forecast)
        }
        viewModel.hourlyForecast.observe(this) { hourly ->
            hourlyAdapter.submitList(hourly)
        }
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.tvError.text = message
                binding.tvError.visibility = View.VISIBLE
                viewModel.clearError()
            } else {
                binding.tvError.visibility = View.GONE
            }
        }
        viewModel.isCelsius.observe(this) { isCelsius ->
            hourlyAdapter.setIsCelsius(isCelsius)
            dailyAdapter.setIsCelsius(isCelsius)
            viewModel.currentWeather.value?.let { updateCurrentWeatherUI(it) }
        }
    }

    private fun setupListeners() {
        binding.btnRefresh.setOnClickListener {
            viewModel.refresh()
            it.animate().rotationBy(360f).setDuration(600).start()
        }
        binding.btnRefresh.setOnLongClickListener {
            NotificationHelper.showTestNotification(
                this,
                prefs.getLastCity(),
                viewModel.currentWeather.value?.tempC ?: 21,
                viewModel.currentWeather.value?.description ?: "Слънчево"
            )
            true
        }

        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val city = v.text.toString().trim()
                if (city.isNotEmpty()) searchCity(city)
                true
            } else false
        }

        binding.btnSearch.setOnClickListener {
            val city = binding.etSearch.text.toString().trim()
            if (city.isNotEmpty()) searchCity(city)
            else binding.etSearch.error = "Въведете град"
        }

        binding.btnMyLocation.setOnClickListener { requestLocationPermission() }

        binding.btnCelsius.setOnClickListener {
            viewModel.toggleUnit(true)
            prefs.setIsCelsius(true)
            binding.btnCelsius.isSelected = true
            binding.btnFahrenheit.isSelected = false
        }

        binding.btnFahrenheit.setOnClickListener {
            viewModel.toggleUnit(false)
            prefs.setIsCelsius(false)
            binding.btnCelsius.isSelected = false
            binding.btnFahrenheit.isSelected = true
        }
    }

    private fun setupNotifications() {
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.scheduleDaily(this)
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentWeatherUI(weather: CurrentWeatherUi) {
        val isCelsius = viewModel.isCelsius.value ?: true
        binding.tvCityName.text = "${weather.cityName}, ${weather.country}"
        binding.tvTemperature.text = weather.tempDisplay(isCelsius)
        binding.tvDescription.text = weather.description
        binding.tvFeelsLike.text = weather.feelsLikeDisplay(isCelsius)
        binding.tvDateTime.text = weather.dateFormatted
        binding.tvHumidity.text = "${weather.humidity}%"
        binding.tvWindSpeed.text = "${weather.windKmh} км/ч"
        binding.tvVisibility.text = "${weather.visibilityKm} км"
        binding.tvSunrise.text = weather.sunrise
        binding.tvSunset.text = weather.sunset

        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${weather.iconCode}@2x.png")
            .into(binding.ivWeatherIcon)

        updateBackground(viewModel.getWeatherBackground(weather.iconCode))
        binding.tvError.visibility = View.GONE
    }

    private fun updateBackground(bg: WeatherBackground) {
        val (startColor, endColor) = when (bg) {
            WeatherBackground.SUNNY       -> Pair(getColor(R.color.bg_sunny_start),  getColor(R.color.bg_sunny_end))
            WeatherBackground.PARTLY_CLOUDY -> Pair(getColor(R.color.bg_partly_start), getColor(R.color.bg_partly_end))
            WeatherBackground.CLOUDY      -> Pair(getColor(R.color.bg_cloudy_start), getColor(R.color.bg_cloudy_end))
            WeatherBackground.RAINY       -> Pair(getColor(R.color.bg_rainy_start),  getColor(R.color.bg_rainy_end))
            WeatherBackground.STORMY      -> Pair(getColor(R.color.bg_stormy_start), getColor(R.color.bg_stormy_end))
            WeatherBackground.SNOWY       -> Pair(getColor(R.color.bg_snowy_start),  getColor(R.color.bg_snowy_end))
            WeatherBackground.FOGGY       -> Pair(getColor(R.color.bg_foggy_start),  getColor(R.color.bg_foggy_end))
            WeatherBackground.NIGHT       -> Pair(getColor(R.color.bg_night_start),  getColor(R.color.bg_night_end))
        }
        binding.root.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(startColor, endColor)
        )
    }

    private fun searchCity(cityName: String) {
        hideKeyboard()
        viewModel.loadWeatherByCity(cityName)
        prefs.saveLastCity(cityName)
        binding.etSearch.setText("")
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        Toast.makeText(this, "Определяне на локация...", Toast.LENGTH_SHORT).show()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.loadWeatherByLocation(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "Не може да се определи локацията", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Грешка при GPS: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
