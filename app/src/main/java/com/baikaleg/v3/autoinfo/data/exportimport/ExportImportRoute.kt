package com.baikaleg.v3.autoinfo.data.exportimport

import android.content.Context
import android.os.Environment
import android.util.Log
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import android.widget.Toast
import com.baikaleg.v3.autoinfo.R
import io.reactivex.disposables.CompositeDisposable
import java.io.*

const val DATA_PATH = "/AutoInfo/data/"

private const val ROUTE = "route"
private const val CITY = "city"
private const val IS_CIRCLE = "is_circle"
private const val STATIONS = "stations"

const val ORDER_NUMBER = "order_number"
const val DESCRIPTION = "description"
const val SHORT_DESCRIPTION = "short_description"
const val LATITUDE = "latitude"
const val LONGITUDE = "longitude"
const val IS_DIRECT = "is_direct"

fun stationsToJSON(stations: List<Station>): JSONArray {
    val array = JSONArray()
    for (s in stations) {
        val o = JSONObject()
        o.put(ORDER_NUMBER, s.id)
        o.put(DESCRIPTION, s.description)
        o.put(SHORT_DESCRIPTION, s.shortDescription)
        o.put(LATITUDE, s.latitude)
        o.put(LONGITUDE, s.longitude)
        o.put(IS_DIRECT, s.isDirect)
        array.put(o)
    }
    return array
}

fun jsonArrayToStations(body: String): List<Station> {
    val stations = mutableListOf<Station>()
    val array = JSONArray(body)
    for (i in 0 until (array.length())) {
        val jsonObject = array.getJSONObject(i)
        val station = Station(
                jsonObject.getInt(ORDER_NUMBER),
                jsonObject.getString(SHORT_DESCRIPTION),
                jsonObject.getString(DESCRIPTION),
                jsonObject.getDouble(LATITUDE),
                jsonObject.getDouble(LONGITUDE),
                jsonObject.getBoolean(IS_DIRECT)
        )
        stations.add(station)
    }
    return stations
}

class ExportImportRoute(val context: Context) {
    private val repository: Repository = Repository.getInstance(context)
    private val compositeDisposable = CompositeDisposable()

    fun exportToFile(route: Route) {
        compositeDisposable.add(repository.getRoute(route.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data: Route -> writeToFile("${route.route}_${route.city}", routeToJSON(data).toString()) }
        )
    }

    fun importFromFile(fileName: String) {
        val body = readFromFile(fileName)
        if (body == "[]" || body.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.something_wrong_in_importing), Toast.LENGTH_SHORT).show()
            return
        }
        repository.saveRoute(jsonArrayToRoute(body))
    }

    private fun jsonArrayToRoute(body: String): Route {
        val mainObject = JSONObject(body)
        return Route(mainObject.getString(ROUTE),
                mainObject.getBoolean(IS_CIRCLE),
                mainObject.getString(CITY),
                jsonArrayToStations(mainObject.getString(STATIONS)))
    }

    private fun routeToJSON(route: Route): JSONObject {
        val mainObject = JSONObject()
        mainObject.put(ROUTE, route.route)
        mainObject.put(CITY, route.city)
        mainObject.put(IS_CIRCLE, route.isCircle)
        mainObject.put(STATIONS, stationsToJSON(route.stations))
        return mainObject
    }

    private fun writeToFile(fileName: String, body: String) {
        try {
            val root = File(Environment.getExternalStorageDirectory(), DATA_PATH)
            if (!root.exists()) {
                root.mkdirs()
            }
            val gpxfile = File(root, "$fileName.txt")
            val writer = FileWriter(gpxfile)
            writer.append(body)
            writer.flush()
            writer.close()
            Toast.makeText(context, context.getString(R.string.export_is_done), Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readFromFile(fileName: String): String {
        val builder = StringBuilder()
        val root = File(Environment.getExternalStorageDirectory(), DATA_PATH)
        var success = true
        if (!root.exists()) {
            success = root.mkdirs()
        }
        if (success) {
            val dest = File(root, fileName)
            if (!dest.exists()) {
                success = dest.mkdir()
            }
            if (success) {
                try {
                    builder.append(FileInputStream(dest).bufferedReader().use { it.readText() })
                } catch (e: Exception) {
                    Log.e("readFromFileException", e.toString())
                }
            }
        }
        return builder.toString()
    }

    fun clear() {
        compositeDisposable.clear()
    }
}