package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName


data class WeatherResponse(
    val name: String,                          // Град
    val main: Main,
    val weather: List<WeatherDescription>,
    val wind: Wind,
    val visibility: Int,
    val sys: Sys,
    val dt: Long                               // Unix timestamp
)

data class Main(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    val humidity: Int,
    val pressure: Int
)

data class WeatherDescription(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)


data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<WeatherDescription>,
    val wind: Wind,
    @SerializedName("pop") val precipProbability: Double,  // вероятност за валеж
    @SerializedName("dt_txt") val dtTxt: String
)

data class City(
    val name: String,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)


data class CurrentWeatherUi(
    val cityName: String,
    val country: String,
    val tempC: Int,
    val feelsLikeC: Int,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windKmh: Int,
    val visibilityKm: Int,
    val precipPercent: Int,
    val sunrise: String,
    val sunset: String,
    val dateFormatted: String
) {
    fun tempDisplay(isCelsius: Boolean): String {
        val t = if (isCelsius) tempC else celsiusToFahrenheit(tempC)
        return "$t°${if (isCelsius) "C" else "F"}"
    }

    fun feelsLikeDisplay(isCelsius: Boolean): String {
        val t = if (isCelsius) feelsLikeC else celsiusToFahrenheit(feelsLikeC)
        return "Усеща се като $t°${if (isCelsius) "C" else "F"}"
    }

    private fun celsiusToFahrenheit(c: Int) = (c * 9.0 / 5 + 32).toInt()
}

data class DayForecastUi(
    val dayName: String,
    val dateFormatted: String,
    val iconCode: String,
    val description: String,
    val tempHighC: Int,
    val tempLowC: Int,
    val humidity: Int,
    val windKmh: Int,
    val precipPercent: Int,
    val uvIndex: Int = 0,
    val sunrise: String = "",
    val sunset: String = ""
) {
    fun highDisplay(isCelsius: Boolean): String {
        val t = if (isCelsius) tempHighC else (tempHighC * 9.0 / 5 + 32).toInt()
        return "$t°"
    }

    fun lowDisplay(isCelsius: Boolean): String {
        val t = if (isCelsius) tempLowC else (tempLowC * 9.0 / 5 + 32).toInt()
        return "$t°"
    }
}

data class HourForecastUi(
    val timeLabel: String,
    val iconCode: String,
    val tempC: Int
) {
    fun tempDisplay(isCelsius: Boolean): String {
        val t = if (isCelsius) tempC else (tempC * 9.0 / 5 + 32).toInt()
        return "$t°"
    }
}
