package com.baikaleg.v3.autoinfo.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ServiceTestRule
import android.support.test.runner.AndroidJUnit4
import com.baikaleg.v3.autoinfo.audio.AudioController
import com.baikaleg.v3.autoinfo.data.*
import com.baikaleg.v3.autoinfo.data.model.GeoData
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.service.stationsearch.StationSearchService
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Ignore
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
                jsonObject.getString("route"),
                jsonObject.getString("short_description"),
                jsonObject.getDouble("latitude"),
                jsonObject.getDouble("longitude"),
                jsonObject.getBoolean("type")
        )
        station.id = jsonObject.getLong("id")
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
    private lateinit var stations: List<Station>
    private val geoData: MutableList<GeoData> = arrayListOf()
    private val geoReverseData: MutableList<GeoData> = arrayListOf()
    private lateinit var audioSystem: AudioController

    @Before
    fun setup() {
        stations = loadStationsList(InstrumentationRegistry.getTargetContext())

        val geoArray = loadLocationArray("direct.txt", InstrumentationRegistry.getTargetContext())
        (0 until (geoArray.length()))
                .map { geoArray.getJSONObject(it) }
                .forEach { geoData.add(GeoData(it.getDouble("latitude"), it.getDouble("longitude"))) }

        geoReverseData.addAll(geoData)
        geoReverseData.addAll(geoData.reversed())

        audioSystem = AudioController(InstrumentationRegistry.getTargetContext())
    }

    @Ignore
    @Test
    fun testCurrentStationsAnnouncementInOneDirection() {
        val intent = Intent(InstrumentationRegistry.getTargetContext(), StationSearchService::class.java)
        val pref = QueryPreferences(InstrumentationRegistry.getTargetContext())
        pref.setAnnounceStationType(ANNOUNCE_STATION_TYPE_EMPTY)
        pref.setAnnounceAudioType(ANNOUNCE_AUDIO_TYPE_TTS)

        val binder: IBinder = mServiceRule.bindService(intent)
        val service = (binder as StationSearchService.StationSearchBinder).getService()

        val currentStationsListToTest: MutableList<Station> = mutableListOf()
        val nextStationsListToTest: MutableList<Station> = mutableListOf()

        for (data in geoData) {
            Thread.sleep(100)
            service.announceNearestStation(
                    data.latitude,
                    data.longitude,
                    stations,
                    object : StationSearchService.OnStationStateChanged {
                        override fun announceCurrentStation(station: Station) {
                            currentStationsListToTest.add(station)
                            audioSystem.announceStation(station.shortDescription, 1)
                        }

                        override fun announceNextStation(station: Station) {
                            nextStationsListToTest.add(station)
                        }

                        override fun isDirectionChanged(b: Boolean) {
                        }
                    })
        }

        assertThat(currentStationsListToTest, equalTo(stations.filter { station -> station.isDirect }))
        assertThat(nextStationsListToTest, empty())
    }

    @Test
    fun testWithImmediateNextStationsAnnouncementInOneDirection() {
        val intent = Intent(InstrumentationRegistry.getTargetContext(), StationSearchService::class.java)
        val pref = QueryPreferences(InstrumentationRegistry.getTargetContext())
        pref.setAnnounceStationType(ANNOUNCE_STATION_TYPE_NEXT)

        val binder: IBinder = mServiceRule.bindService(intent)
        val service = (binder as StationSearchService.StationSearchBinder).getService()

        val nextStationsListToTest: MutableList<Station> = mutableListOf()

        for (data in geoData) {
            Thread.sleep(500)
            service.announceNearestStation(
                    data.latitude,
                    data.longitude,
                    stations,
                    object : StationSearchService.OnStationStateChanged {
                        override fun announceCurrentStation(station: Station) {
                            audioSystem.announceStation(station.shortDescription, 1)
                        }

                        override fun announceNextStation(station: Station) {
                            nextStationsListToTest.add(station)
                            audioSystem.announceStation(station.shortDescription, 2)
                        }

                        override fun isDirectionChanged(b: Boolean) {
                        }
                    })
        }

        val tempNextStationsList: MutableList<Station> = stations.asSequence()
                .filter { station -> station.id != 0L }
                .filter { station -> station.isDirect }
                .toMutableList()

        assertThat(nextStationsListToTest, equalTo(tempNextStationsList))
    }
/*
    @Test
    fun testWithDelayNextStationsAnnouncementInOneDirection() {
        val intent = Intent(InstrumentationRegistry.getTargetContext(), StationSearchService::class.java)
        val pref = QueryPreferences(InstrumentationRegistry.getTargetContext())
        pref.setAnnounceStationType(ANNOUNCE_STATION_TYPE_NEXT_WITH_DELAY)

        val binder: IBinder = mServiceRule.bindService(intent)
        val service = (binder as StationSearchService.StationSearchBinder).getService()

        val nextStationsListToTest: MutableList<Station> = mutableListOf()

        for (data in geoData) {
            service.announceNearestStation(
                    data.latitude,
                    data.longitude,
                    stations,
                    object : StationSearchService.OnStationStateChanged {
                        override fun announceCurrentStation(station: Station) {

                        }

                        override fun announceNextStation(station: Station) {
                            nextStationsListToTest.add(station)
                        }

                        override fun isDirectionChanged(b: Boolean) {
                        }
                    })
        }

        val tempNextStationsList: MutableList<Station> = stations
                .filter { station -> station.id != 0L }
                .filter { station -> station.route_type == 0 }
                .toMutableList()

        assertThat(nextStationsListToTest, equalTo(tempNextStationsList))
    }

    @Test
    fun testWithChangedDirection() {
        val intent = Intent(InstrumentationRegistry.getTargetContext(), StationSearchService::class.java)
        val pref = QueryPreferences(InstrumentationRegistry.getTargetContext())
        pref.setAnnounceStationType(ANNOUNCE_STATION_TYPE_NEXT)

        val binder: IBinder = mServiceRule.bindService(intent)
        val service = (binder as StationSearchService.StationSearchBinder).getService()

        val currentStationsListToTest: MutableList<Station> = mutableListOf()
        val nextStationsListToTest: MutableList<Station> = mutableListOf()

        for (data in geoReverseData) {
            service.announceNearestStation(
                    data.latitude,
                    data.longitude,
                    stations,
                    object : StationSearchService.OnStationStateChanged {
                        override fun announceCurrentStation(station: Station) {
                            currentStationsListToTest.add(station)
                        }

                        override fun announceNextStation(station: Station) {
                            nextStationsListToTest.add(station)
                        }

                        override fun isDirectionChanged(b: Boolean) {

                        }
                    })
        }
        val tempCurrentStationsList: MutableList<Station> = stations.filter { station -> station.id != 7L }.toMutableList()
        val tempNextStationsList: MutableList<Station> = stations
                .filter { station -> station.id != 0L }
                .filter { station -> station.id != 7L }
                .filter { station -> station.id != 8L }
                .toMutableList()

        assertThat(currentStationsListToTest, equalTo(tempCurrentStationsList))
        assertThat(nextStationsListToTest, equalTo(tempNextStationsList))
    }
    */

}