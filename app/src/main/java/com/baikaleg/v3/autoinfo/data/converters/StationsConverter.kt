package com.baikaleg.v3.autoinfo.data.converters

import android.arch.persistence.room.TypeConverter
import com.baikaleg.v3.autoinfo.data.exportimport.jsonArrayToStations
import com.baikaleg.v3.autoinfo.data.exportimport.stationsToJSON
import com.baikaleg.v3.autoinfo.data.model.Station


class StationsConverter {

    @TypeConverter
    fun toStations(body: String?): List<Station>? {
        body?.let { return jsonArrayToStations(it) }
        return null
    }

    @TypeConverter
    fun toString(list: List<Station>?): String? {
        list?.let {
            return stationsToJSON(it).toString()
        }
        return null
    }
}