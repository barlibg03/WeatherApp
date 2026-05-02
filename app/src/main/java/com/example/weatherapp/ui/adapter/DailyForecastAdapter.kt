package com.example.weatherapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.data.model.DayForecastUi
import com.example.weatherapp.databinding.ItemDailyForecastBinding


class DailyForecastAdapter(
    private var isCelsius: Boolean
) : ListAdapter<DayForecastUi, DailyForecastAdapter.ViewHolder>(DiffCallback) {

    private val expandedPositions = mutableSetOf<Int>()

    fun setIsCelsius(celsius: Boolean) {
        isCelsius = celsius
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailyForecastBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isExpanded = expandedPositions.contains(position)
        holder.bind(getItem(position), isCelsius, isExpanded) {
            // Клик — разгъване/свиване
            if (expandedPositions.contains(position)) {
                expandedPositions.remove(position)
            } else {
                expandedPositions.clear()
                expandedPositions.add(position)
            }
            notifyDataSetChanged()
        }
    }

    class ViewHolder(private val binding: ItemDailyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DayForecastUi, isCelsius: Boolean, isExpanded: Boolean, onClick: () -> Unit) {
            // Основна информация
            binding.tvDayName.text = item.dayName
            binding.tvDayHigh.text = item.highDisplay(isCelsius)
            binding.tvDayLow.text = item.lowDisplay(isCelsius)

            val iconUrl = "https://openweathermap.org/img/wn/${item.iconCode}@2x.png"
            Glide.with(binding.root.context)
                .load(iconUrl)
                .into(binding.ivDayIcon)


            binding.ivExpandArrow.animate()
                .rotation(if (isExpanded) 180f else 0f)
                .setDuration(200)
                .start()


            binding.layoutDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
            if (isExpanded) {
                binding.tvDetailHumidity.text = "${item.humidity}%"
                binding.tvDetailWind.text = "${item.windKmh} км/ч"
                binding.tvDetailPrecip.text = "${item.precipPercent}%"
                binding.tvDetailDesc.text = item.description
                binding.tvDetailSunrise.text = item.sunrise
                binding.tvDetailSunset.text = item.sunset
            }

            // Клик listener
            binding.root.setOnClickListener { onClick() }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DayForecastUi>() {
        override fun areItemsTheSame(oldItem: DayForecastUi, newItem: DayForecastUi) =
            oldItem.dayName == newItem.dayName

        override fun areContentsTheSame(oldItem: DayForecastUi, newItem: DayForecastUi) =
            oldItem == newItem
    }
}
