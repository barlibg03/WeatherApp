package com.example.weatherapp.data.repository

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.model.*
import com.example.weatherapp.data.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*


class WeatherRepository {

    private val api = RetrofitClient.weatherApiService
    private val apiKey = BuildConfig.WEATHER_API_KEY



    suspend fun getCurrentWeather(cityName: String): Result<CurrentWeatherUi> {
        return try {
            val response = api.getCurrentWeatherByCity(cityName, apiKey)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Празен отговор"))
                Result.success(mapToCurrentUi(body))
            } else {
                when (response.code()) {
                    404 -> Result.failure(Exception("Градът не е намерен"))
                    401 -> Result.failure(Exception("Невалиден API ключ"))
                    else -> Result.failure(Exception("Грешка: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Няма интернет връзка"))
        }
    }

    suspend fun getCurrentWeatherByLocation(lat: Double, lon: Double): Result<CurrentWeatherUi> {
        return try {
            val response = api.getCurrentWeatherByLocation(lat, lon, apiKey)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Празен отговор"))
                Result.success(mapToCurrentUi(body))
            } else {
                Result.failure(Exception("Грешка при зареждане"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Няма интернет връзка"))
        }
    }



    suspend fun getForecast(cityName: String): Result<List<DayForecastUi>> {
        return try {
            val response = api.getForecastByCity(cityName, apiKey)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Празен отговор"))
                Result.success(mapToDailyForecast(body))
            } else {
                Result.failure(Exception("Грешка при прогнозата"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Няма интернет връзка"))
        }
    }

    suspend fun getForecastByLocation(lat: Double, lon: Double): Result<List<DayForecastUi>> {
        return try {
            val response = api.getForecastByLocation(lat, lon, apiKey)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Празен отговор"))
                Result.success(mapToDailyForecast(body))
            } else {
                Result.failure(Exception("Грешка при прогнозата"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Няма интернет връзка"))
        }
    }

    suspend fun getHourlyForecast(cityName: String): Result<List<HourForecastUi>> {
        return try {
            val response = api.getForecastByCity(cityName, apiKey)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Празен отговор"))
                Result.success(mapToHourlyForecast(body))
            } else {
                Result.failure(Exception("Грешка"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Няма интернет"))
        }
    }



    private fun mapToCurrentUi(w: WeatherResponse): CurrentWeatherUi {
        val sdf = SimpleDateFormat("EEEE, dd MMM • HH:mm", Locale("bg"))
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

        return CurrentWeatherUi(
            cityName = w.name,
            country = w.sys.country,
            tempC = w.main.temp.toInt(),
            feelsLikeC = w.main.feelsLike.toInt(),
            description = w.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
            iconCode = w.weather.firstOrNull()?.icon ?: "01d",
            humidity = w.main.humidity,
            windKmh = (w.wind.speed * 3.6).toInt(),
            visibilityKm = w.visibility / 1000,
            precipPercent = 0,
            sunrise = timeFmt.format(Date(w.sys.sunrise * 1000)),
            sunset = timeFmt.format(Date(w.sys.sunset * 1000)),
            dateFormatted = sdf.format(Date(w.dt * 1000))
                .replaceFirstChar { it.uppercase() }
        )
    }


    private fun mapToDailyForecast(forecast: ForecastResponse): List<DayForecastUi> {
        val dayFmt = SimpleDateFormat("EEEE", Locale("bg"))
        val dateFmt = SimpleDateFormat("dd MMM", Locale("bg"))
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dayKeyFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        // Групираме по ден
        val grouped = forecast.list.groupBy { dayKeyFmt.format(Date(it.dt * 1000)) }
        val today = dayKeyFmt.format(Date())

        return grouped.entries
            .filter { it.key >= today }
            .take(5)
            .map { (_, items) ->
                val date = Date(items.first().dt * 1000)
                val maxTemp = items.maxOf { it.main.temp }.toInt()
                val minTemp = items.minOf { it.main.temp }.toInt()
                val noonItem = items.minByOrNull { Math.abs(it.dt % 86400 - 43200) } ?: items.first()
                val avgHumidity = items.map { it.main.humidity }.average().toInt()
                val avgWind = items.map { it.wind.speed }.average()
                val maxPrecip = items.maxOf { it.precipProbability }

                val dayName = dayFmt.format(date).replaceFirstChar { it.uppercase() }
                val isToday = dayKeyFmt.format(date) == today

                DayForecastUi(
                    dayName = if (isToday) "Днес" else dayName,
                    dateFormatted = dateFmt.format(date),
                    iconCode = noonItem.weather.firstOrNull()?.icon ?: "01d",
                    description = noonItem.weather.firstOrNull()?.description
                        ?.replaceFirstChar { it.uppercase() } ?: "",
                    tempHighC = maxTemp,
                    tempLowC = minTemp,
                    humidity = avgHumidity,
                    windKmh = (avgWind * 3.6).toInt(),
                    precipPercent = (maxPrecip * 100).toInt(),
                    sunrise = timeFmt.format(Date(forecast.city.sunrise * 1000)),
                    sunset = timeFmt.format(Date(forecast.city.sunset * 1000))
                )
            }
    }

    private fun mapToHourlyForecast(forecast: ForecastResponse): List<HourForecastUi> {
        val timeFmt = SimpleDateFormat("HH:00", Locale.getDefault())
        return forecast.list.take(12).mapIndexed { index, item ->
            HourForecastUi(
                timeLabel = if (index == 0) "Сега" else timeFmt.format(Date(item.dt * 1000)),
                iconCode = item.weather.firstOrNull()?.icon ?: "01d",
                tempC = item.main.temp.toInt()
            )
        }
    }
}
