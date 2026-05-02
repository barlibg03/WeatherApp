# 🌤️ WeatherApp — Android приложение за прогноза за времето

Пълно Android приложение написано в **Kotlin** с **MVVM архитектура**, **Material Design** и реално **OpenWeatherMap API**.

---

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

---

## 🚀 Стъпки за отваряне в Android Studio

### 1. Отвори проекта
```
File → Open → избери папката WeatherApp
```
Android Studio ще разпознае `settings.gradle` и ще зареди проекта автоматично.

### 2. Вземи безплатен API ключ
1. Отиди на **https://openweathermap.org/**
2. Регистрирай се безплатно
3. Отиди в **API Keys** в профила си
4. Копирай генерирания ключ

### 3. Постави API ключа
Отвори файла:
```
app/build.gradle
```
Намери реда:
```groovy
buildConfigField "String", "WEATHER_API_KEY", "\"YOUR_API_KEY_HERE\""
```
Замени `YOUR_API_KEY_HERE` с твоя ключ:
```groovy
buildConfigField "String", "WEATHER_API_KEY", "\"a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6\""
```

> ⚠️ **Важно:** Новите ключове стават активни след ~10 минути.

### 4. Sync Gradle
```
File → Sync Project with Gradle Files
```
или натисни бутона **"Sync Now"** в лентата горе.

### 5. Стартирай
- Свържи Android устройство **или** стартирай емулатор (AVD Manager)
- Натисни **▶ Run** (Shift+F10)

---

## 🏗️ Архитектура — MVVM

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                         │
│   MainActivity  ←  наблюдава LiveData  ←  ViewModel    │
│   Adapters (RecyclerView)                               │
└─────────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────────┐
│                    ViewModel Layer                       │
│   WeatherViewModel                                      │
│   • съдържа бизнес логиката                             │
│   • не зависи от Android контекст (тестваем)            │
│   • излага LiveData на UI                               │
└─────────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                           │
│   WeatherRepository   ← централна точка за данни       │
│        ↕                      ↕                         │
│   RetrofitClient          WeatherPreferences            │
│   (REST API)              (SharedPreferences)           │
└─────────────────────────────────────────────────────────┘
```

---

## ✅ Реализирани функционалности

| Функционалност | Статус |
|---|---|
| Текущо време (температура, описание, икона) | ✅ |
| 5-дневна прогноза | ✅ |
| Прогноза по часове (хоризонтален скрол) | ✅ |
| Търсене на град по име | ✅ |
| Запазване на последно търсен град | ✅ SharedPreferences |
| GPS локация — „Използвай моята локация" | ✅ FusedLocationProvider |
| Ръчно обновяване (бутон + SwipeRefresh) | ✅ |
| Клик на ден → разгъваеми детайли | ✅ |
| Ежедневна нотификация в 08:00 | ✅ WorkManager |
| Динамичен фон по вид на времето | ✅ 8 цветови схеми |
| Влажност и вятър | ✅ |
| Смяна °C / °F | ✅ |
| Обработка на грешки (няма интернет, невалиден град) | ✅ |
| Material Design | ✅ |
| CardView за прогнозата | ✅ |

---

## 📦 Зависимости (Dependencies)

| Библиотека | Цел |
|---|---|
| **Retrofit2 + Gson** | REST API заявки |
| **OkHttp3 + Logging** | HTTP клиент и логване |
| **Coroutines** | Асинхронни заявки |
| **ViewModel + LiveData** | MVVM архитектура |
| **Glide** | Зареждане на иконки от URL |
| **WorkManager** | Планиране на нотификации |
| **Play Services Location** | GPS координати |
| **SwipeRefreshLayout** | Жест за обновяване |
| **Material Components** | Material Design UI |

---

## 🌐 API Endpoints

| Заявка | Endpoint |
|---|---|
| Текущо време по град | `GET /weather?q={city}&appid={key}&units=metric&lang=bg` |
| Текущо време по GPS | `GET /weather?lat={lat}&lon={lon}&appid={key}&units=metric` |
| 5-дневна прогноза по град | `GET /forecast?q={city}&appid={key}&units=metric&lang=bg` |
| 5-дневна прогноза по GPS | `GET /forecast?lat={lat}&lon={lon}&appid={key}&units=metric` |

Параметърът `lang=bg` връща описания на **Български** директно от API.

---

## 🎨 Цветови схеми по вид на времето

| Вид | Икони (OpenWeather) | Цветове |
|---|---|---|
| ☀️ Слънчево | 01d | #1565C0 → #42A5F5 |
| ⛅ Частично облачно | 02d, 03d | #455A64 → #90A4AE |
| ☁️ Облачно | 04d | #37474F → #78909C |
| 🌧️ Дъжд | 09d, 10d | #1A237E → #5C6BC0 |
| ⛈️ Буря | 11d | #212121 → #546E7A |
| ❄️ Сняг | 13d | #B3E5FC → #81D4FA |
| 🌫️ Мъгла | 50d | #607D8B → #B0BEC5 |
| 🌙 Нощ | *n суфикс | #0D1B2A → #1B2A4A |

---

## 🔔 Нотификации

Нотификациите използват **WorkManager** за надеждно планиране:
- Изпращат се всеки ден в **08:00 сутринта**
- Взимат актуалните данни от API
- Показват: град, температура, описание
- Активират се автоматично при стартиране на приложението

---

## ❗ Чести проблеми

**„Gradel sync failed"**
→ Провери интернет връзката и версията на Android Studio (трябва Hedgehog+)

**„City not found" / 404**
→ Пиши имена на английски: `Sofia`, `Plovdiv`, `Varna`

**Иконките не се зареждат**
→ Провери дали `INTERNET` правото е в Manifest и дали устройството има интернет

**Нотификациите не се появяват**
→ На Android 13+ трябва ръчно разрешение: Settings → Apps → WeatherApp → Notifications → Allow

**API ключът не работи**
→ Новите ключове от OpenWeatherMap активират след ~10 минути

---

## 📝 Бележки за защита

Приложението демонстрира:

1. **REST API интеграция** — Retrofit с coroutines, error handling, response mapping
2. **MVVM архитектура** — чисто разделение UI / логика / данни
3. **LiveData & ViewModel** — реактивен UI, оцелява при завъртане на екрана
4. **GPS** — FusedLocationProviderClient с runtime permissions
5. **SharedPreferences** — персистиране на потребителски предпочитания
6. **WorkManager** — надеждни фонови задачи (нотификации)
7. **RecyclerView** — с DiffUtil за оптимален rendering
8. **Material Design** — CardView, динамични градиенти, ripple ефекти
9. **Обработка на грешки** — мрежови грешки, невалиден град, без интернет
