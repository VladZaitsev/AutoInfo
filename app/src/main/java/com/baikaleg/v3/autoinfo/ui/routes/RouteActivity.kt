package com.baikaleg.v3.autoinfo.ui.routes

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.databinding.ActivityRouteBinding
import com.baikaleg.v3.autoinfo.ui.routes.dialog.AddEditRouteDialog
import com.baikaleg.v3.autoinfo.ui.routes.route.RouteTouchCallback
import com.baikaleg.v3.autoinfo.ui.routes.route.RouteViewAdapter
import com.baikaleg.v3.autoinfo.ui.stations.AddEditStationActivity
import com.baikaleg.v3.autoinfo.ui.stations.ROUTE_EXTRA_DATA

import kotlinx.android.synthetic.main.activity_route.*

@BindingAdapter("app:routes")
fun setRoutes(recyclerView: RecyclerView, routes: List<Route>?) {
    val adapter = recyclerView.adapter as RouteViewAdapter
    if (routes != null) {
        adapter.refresh(routes)
    }
}

class RouteActivity : AppCompatActivity(), RouteViewAdapter.RouteClickNavigator {

    private lateinit var viewmodel: RouteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setSupportActionBar(toolbar)

        val routesAdapter = RouteViewAdapter(this)
        val binding: ActivityRouteBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_route)
        viewmodel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
        with(binding) {
            setLifecycleOwner(this@RouteActivity)
            viewmodel = viewmodel

            with(routes) {
                layoutManager = LinearLayoutManager(this@RouteActivity)
                adapter = routesAdapter
            }
        }

        val callback = RouteTouchCallback(this, viewmodel)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.routes)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.route_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.add_route) {
            val dialog = AddEditRouteDialog.getInstance(null)
            dialog.show(fragmentManager, "dialog")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(route: Route) {
        val intent = Intent(this, AddEditStationActivity::class.java)
        intent.putExtra(ROUTE_EXTRA_DATA, route)
        startActivity(intent)
    }
}
