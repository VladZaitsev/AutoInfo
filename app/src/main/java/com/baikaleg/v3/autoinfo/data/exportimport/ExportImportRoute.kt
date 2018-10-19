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
private const val SHORT_DESCRIPTION = "short_description"
private const val LATITUDE = "latitude"
private const val LONGITUDE = "longitude"
private const val IS_DIRECT = "is_direct"
private const val ORDER_NUMBER = "order_number"
private const val CITY = "city"
private const val DESCRIPTION = "description"

class ExportImportRoute(val context: Context) {
    private val repository: Repository = Repository.getInstance(context)
    private val compositeDisposable = CompositeDisposable()

    fun exportToFile(route: Route) {
        compositeDisposable.add(repository.getStations(route.name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data: List<Station> ->
                    writeToFile("${route.name}_${route.city}", convertToJSON(data))
                })
    }

    fun importFromFile(fileName: String) {
        val body = readFromFile(fileName)
        if (body == "[]" || body.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.something_wrong_in_importing), Toast.LENGTH_SHORT).show()
            return
        }
        val stations = convertToStations(body)
        var isCircle = false
        for (station in stations) {
            repository.saveStation(station)
            if (!station.isDirect && !isCircle) {
                isCircle = true
            }
        }

        val route = Route(
                fileName.split("_")[0],
                isCircle,
                fileName.split("_")[1])
        repository.saveRoute(route)
    }

    private fun convertToJSON(list: List<Station>): String {
        val array = JSONArray()
        for (s in list) {
            val o = JSONObject()
            o.put(ROUTE, s.route)
            o.put(SHORT_DESCRIPTION, s.shortDescription)
            o.put(LATITUDE, s.latitude)
            o.put(LONGITUDE, s.longitude)
            o.put(IS_DIRECT, s.isDirect)
            o.put(ORDER_NUMBER, s.orderNumber)
            o.put(CITY, s.city)
            o.put(DESCRIPTION, s.description)
            array.put(o)
        }
        return array.toString()
    }

    private fun convertToStations(body: String): List<Station> {
        val list = mutableListOf<Station>()
        val array = JSONArray(body)
        for (i in 0 until (array.length())) {
            val jsonObject = array.getJSONObject(i)
            val station = Station(
                    jsonObject.getString(ROUTE),
                    jsonObject.getString(SHORT_DESCRIPTION),
                    jsonObject.getDouble(LATITUDE),
                    jsonObject.getDouble(LONGITUDE),
                    jsonObject.getBoolean(IS_DIRECT)
            )
            station.orderNumber = jsonObject.getInt(ORDER_NUMBER)
            station.city = jsonObject.getString(CITY)
            station.description = jsonObject.getString(DESCRIPTION)
            list.add(station)
        }
        return list
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

    fun clear(){
        compositeDisposable.clear()
    }
}