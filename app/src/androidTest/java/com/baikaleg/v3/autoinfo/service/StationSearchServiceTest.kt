package com.baikaleg.v3.autoinfo.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ServiceTestRule
import android.support.test.runner.AndroidJUnit4
import com.baikaleg.v3.autoinfo.data.db.Station
import org.hamcrest.Matchers.*
import org.json.JSONArray
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


fun loadStationsList(context: Context): List<Station> {
    val stations: MutableList<Station> = mutableListOf()
    val jsonString = context.assets.open("test_stations.txt").bufferedReader().use {
        it.readText()
    }
    val array = JSONArray(jsonString)
    for (i in 0 until (array.length())) {
        val jsonObject = array.getJSONObject(i)
        val station = Station(
                jsonObject.getString("city"),
                jsonObject.getString("route"),
                jsonObject.getString("short_description"),
                jsonObject.getString("description"),
                jsonObject.getDouble("latitude"),
                jsonObject.getDouble("longitude"),
                jsonObject.getInt("type")
        )
        stations.add(station)
    }
    return stations
}

fun loadLocationArray(fileName: String, context: Context): JSONArray {
    val jsonString = context.assets.open(fileName).bufferedReader().use {
        it.readText()
    }
    return JSONArray(jsonString)
}

@RunWith(AndroidJUnit4::class)
class StationSearchServiceTest {

    @Rule
    @JvmField
    val mServiceRule = ServiceTestRule()
    private lateinit var service: StationSearchService

    @Before
    fun setup() {
        val intent = Intent(InstrumentationRegistry.getTargetContext(), StationSearchService::class.java)
        val binder: IBinder = mServiceRule.bindService(intent)
        service = (binder as StationSearchService.StationSearchBinder).getService()
    }

    @Test
    fun testCurrentStationsInOneDirect() {
        val stations = loadStationsList(InstrumentationRegistry.getTargetContext())
        val array = loadLocationArray("direct.txt", InstrumentationRegistry.getTargetContext())

        val listToTest: MutableList<Station> = mutableListOf()

        for (i in 0 until (array.length())) {
            val jsonObject = array.getJSONObject(i)
            service.announceNearestStationsState(
                    jsonObject.getDouble("latitude"),
                    jsonObject.getDouble("longitude"),
                    stations,
                    object : StationSearchService.OnStationStateChanged {
                        override fun announceCurrentStation(station: Station) {
                            listToTest.add(station)
                        }

                        override fun announceNextStation(station: Station) {
                        }

                        override fun isDirectionChanged(b: Boolean) {
                        }
                    })
        }

        assertThat(listToTest, `not`(empty()))
        assertThat(listToTest, equalTo(stations))
    }
}