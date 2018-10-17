package com.baikaleg.v3.autoinfo.ui.routes.route

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.databinding.ItemViewRouteBinding

class RouteViewAdapter(private val navigator: RouteClickNavigator) : RecyclerView.Adapter<RouteViewAdapter.RouteViewHolder>() {

    private val routes = mutableListOf<Route>()

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val viewmodel = RouteItemViewModel(routes[position])
        holder.binding.viewmodel = viewmodel

        holder.binding.root.setOnClickListener { v -> navigator.onClick(routes[position]) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        return RouteViewHolder(ItemViewRouteBinding.inflate(LayoutInflater.from(parent.context)))
    }

    fun refresh(list: List<Route>) {
        routes.clear()
        routes.addAll(list)
        notifyDataSetChanged()
    }

    class RouteViewHolder(val binding: ItemViewRouteBinding) : RecyclerView.ViewHolder(binding.root)

    interface RouteClickNavigator {
        fun onClick(route: Route)
    }
}