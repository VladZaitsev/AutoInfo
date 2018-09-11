package com.baikaleg.v3.autoinfo.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "stationData")
data class Station(@ColumnInfo(name = "city") var city: String,
                   @ColumnInfo(name = "route") var route: String,
                   @ColumnInfo(name = "short_description") var short_description: String,
                   @ColumnInfo(name = "description") var description: String,
                   @ColumnInfo(name = "latitude") var latitude: Double,
                   @ColumnInfo(name = "longitude") var longitude: Double,
                   @ColumnInfo(name = "type") var type: Int) { //0 - to; 1 - from; 2 - circle
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
