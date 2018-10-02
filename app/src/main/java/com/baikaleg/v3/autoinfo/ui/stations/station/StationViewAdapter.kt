package com.baikaleg.v3.autoinfo.ui.stations.station

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.databinding.ItemViewStationBinding

class StationViewAdapter : RecyclerView.Adapter<StationViewAdapter.StationViewHolder>() {
    private var stations: MutableList<Station> = mutableListOf()

    fun refresh(list: List<Station>) {
        stations = list.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return stations.size
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val viewModel = StationItemViewModel(stations.get(position))
        holder.binding.viewmodel = viewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val binding = ItemViewStationBinding.inflate(LayoutInflater.from(parent.context))
        return StationViewHolder(binding)
    }

    inner class StationViewHolder(val binding: ItemViewStationBinding) : RecyclerView.ViewHolder(binding.root)
}