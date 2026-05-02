# 🌤️ WeatherApp — Android приложение за прогноза за времето

## 📁 Структура на проекта

```
WeatherApp/
├── app/
│   ├── build.gradle                          ← зависимости (Retrofit, Glide, WorkManager...)
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml               ← права: интернет, GPS, нотификации
│       ├── java/com/example/weatherapp/
│       │   ├── data/
│       │   │   ├── model/
│       │   │   │   └── WeatherModels.kt      ← всички data класове + UI модели
│       │   │   ├── network/
│       │   │   │   ├── WeatherApiService.kt  ← Retrofit API интерфейс
│       │   │   │   └── RetrofitClient.kt     ← Singleton Retrofit клиент
│       │   │   ├── repository/
│       │   │   │   └── WeatherRepository.kt  ← слой за данни + mapper функции
│       │   │   └── local/
│       │   │       └── WeatherPreferences.kt ← SharedPreferences (последен град, единица)
│       │   ├── ui/
│       │   │   ├── MainActivity.kt           ← главен екран, GPS, търсене, UI логика
│       │   │   ├── viewmodel/
│       │   │   │   └── WeatherViewModel.kt   ← бизнес логика, LiveData
│       │   │   └── adapter/
│       │   │       ├── HourlyForecastAdapter.kt  ← хоризонтален RecyclerView
│       │   │       └── DailyForecastAdapter.kt   ← вертикален с разгъваеми детайли
│       │   └── util/
│       │       └── NotificationHelper.kt     ← ежедневна нотификация в 08:00
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml         ← главен layout
│           │   ├── item_hourly_forecast.xml  ← карта за час
│           │   └── item_daily_forecast.xml   ← карта за ден (с детайли)
│           ├── drawable/                     ← всички фонове и иконки (XML vectors)
│           └── values/
│               ├── colors.xml               ← 8 цветови схеми (слънце, дъжд, сняг...)
│               ├── strings.xml              ← всички текстове на Български
│               └── themes.xml              ← Material Design тема
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## ✅ Реализирани функционалности

| Функционалност | Статус |
|---|---|
| Текущо време (температура, описание, икона) 
| 5-дневна прогноза 
| Прогноза по часове 
| Търсене на град по име 
| Запазване на последно търсен град 
| GPS локация — „Използвай моята локация" 
| Oбновяване 
| Клик на ден - допълнителна информация за определния ден
| Ежедневна нотификация в 08:00 
| Динамичен фон по вид на времето 
| Влажност и вятър 
| Смяна °C / °F 
| Обработка на грешки (няма интернет, невалиден град) 


## 🔔 Нотификации

Нотификациите използват **WorkManager** за надеждно планиране:
- Изпращат се всеки ден в **08:00 сутринта**
- Взимат актуалните данни от API
- Показват: град, температура, описание
- Активират се автоматично при стартиране на приложението
