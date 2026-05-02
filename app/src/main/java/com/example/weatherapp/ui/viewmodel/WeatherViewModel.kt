package com.example.weatherapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.CurrentWeatherUi
import com.example.weatherapp.data.model.DayForecastUi
import com.example.weatherapp.data.model.HourForecastUi
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.launch

/**
 * WeatherViewModel — съдържа бизнес логиката.
 * UI наблюдава LiveData обектите и реагира на промени.
 */
class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    // ── LiveData ──────────────────────────────────────────

    private val _currentWeather = MutableLiveData<CurrentWeatherUi?>()
    val currentWeather: LiveData<CurrentWeatherUi?> = _currentWeather

    private val _dailyForecast = MutableLiveData<List<DayForecastUi>>()
    val dailyForecast: LiveData<List<DayForecastUi>> = _dailyForecast

    private val _hourlyForecast = MutableLiveData<List<HourForecastUi>>()
    val hourlyForecast: LiveData<List<HourForecastUi>> = _hourlyForecast

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _currentCityName = MutableLiveData<String>()
    val currentCityName: LiveData<String> = _currentCityName

    private val _isCelsius = MutableLiveData<Boolean>(true)
    val isCelsius: LiveData<Boolean> = _isCelsius

    // ── Публични методи ───────────────────────────────────

    /**
     * Зарежда времето по назование на град
     */
    fun loadWeatherByCity(cityName: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            // Текущо Време
            val currentResult = repository.getCurrentWeather(cityName)
            currentResult.onSuccess { weather ->
                _currentWeather.value = weather
                _currentCityName.value = weather.cityName
            }.onFailure { error ->
                _errorMessage.value = error.message
                _isLoading.value = false
                return@launch
            }

            // 5-дневна прогноза
            val forecastResult = repository.getForecast(cityName)
            forecastResult.onSuccess { forecast ->
                _dailyForecast.value = forecast
            }

            // Прогноза по часове
            val hourlyResult = repository.getHourlyForecast(cityName)
            hourlyResult.onSuccess { hourly ->
                _hourlyForecast.value = hourly
            }

            _isLoading.value = false
        }
    }

    /**
     * Зарежда времето по GPS координати
     */
    fun loadWeatherByLocation(latitude: Double, longitude: Double) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val currentResult = repository.getCurrentWeatherByLocation(latitude, longitude)
            currentResult.onSuccess { weather ->
                _currentWeather.value = weather
                _currentCityName.value = weather.cityName
                // Зареждаме прогнозата за намерения град
                loadForecastForCity(weather.cityName)
            }.onFailure { error ->
                _errorMessage.value = error.message
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadForecastForCity(cityName: String) {
        val forecastResult = repository.getForecast(cityName)
        forecastResult.onSuccess { _dailyForecast.value = it }

        val hourlyResult = repository.getHourlyForecast(cityName)
        hourlyResult.onSuccess { _hourlyForecast.value = it }

        _isLoading.value = false
    }

    /** Обновява данните за текущия град */
    fun refresh() {
        val city = _currentCityName.value ?: return
        loadWeatherByCity(city)
    }

    /** Превключва мерна единица °C / °F */
    fun toggleUnit(isCelsius: Boolean) {
        _isCelsius.value = isCelsius
    }

    /** Изчиства съобщението за грешка */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Връща описание на背景а спрямо иконката на времето
     */
    fun getWeatherBackground(iconCode: String): WeatherBackground {
        return when {
            iconCode.startsWith("01") -> WeatherBackground.SUNNY
            iconCode.startsWith("02") || iconCode.startsWith("03") -> WeatherBackground.PARTLY_CLOUDY
            iconCode.startsWith("04") -> WeatherBackground.CLOUDY
            iconCode.startsWith("09") || iconCode.startsWith("10") -> WeatherBackground.RAINY
            iconCode.startsWith("11") -> WeatherBackground.STORMY
            iconCode.startsWith("13") -> WeatherBackground.SNOWY
            iconCode.startsWith("50") -> WeatherBackground.FOGGY
            iconCode.endsWith("n") -> WeatherBackground.NIGHT
            else -> WeatherBackground.SUNNY
        }
    }
}

enum class WeatherBackground {
    SUNNY, PARTLY_CLOUDY, CLOUDY, RAINY, STORMY, SNOWY, FOGGY, NIGHT
}
