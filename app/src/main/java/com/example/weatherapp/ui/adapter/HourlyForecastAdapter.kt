package com.example.weatherapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.data.model.HourForecastUi
import com.example.weatherapp.databinding.ItemHourlyForecastBinding


class HourlyForecastAdapter : ListAdapter<HourForecastUi, HourlyForecastAdapter.ViewHolder>(DiffCallback) {

    private var isCelsius = true

    fun setIsCelsius(celsius: Boolean) {
        isCelsius = celsius
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), isCelsius)
    }

    class ViewHolder(private val binding: ItemHourlyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HourForecastUi, isCelsius: Boolean) {
            binding.tvHourTime.text = item.timeLabel
            binding.tvHourTemp.text = item.tempDisplay(isCelsius)

            val iconUrl = "https://openweathermap.org/img/wn/${item.iconCode}@2x.png"
            Glide.with(binding.root.context)
                .load(iconUrl)
                .into(binding.ivHourIcon)


            if (item.timeLabel == "Сега") {
                binding.root.setBackgroundResource(com.example.weatherapp.R.drawable.bg_hour_card_selected)
            } else {
                binding.root.setBackgroundResource(com.example.weatherapp.R.drawable.bg_hour_card)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HourForecastUi>() {
        override fun areItemsTheSame(oldItem: HourForecastUi, newItem: HourForecastUi) =
            oldItem.timeLabel == newItem.timeLabel

        override fun areContentsTheSame(oldItem: HourForecastUi, newItem: HourForecastUi) =
            oldItem == newItem
    }
}
