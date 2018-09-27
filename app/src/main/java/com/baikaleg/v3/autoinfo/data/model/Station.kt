package com.baikaleg.v3.autoinfo.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * @param route_type <code>0</code> if station is in forward direction
 *                   <code>1</code> if station is in backward direction
 *                   <code>2</code> if station is in circular direction
 */
@Entity(tableName = "stationData")
data class Station(@ColumnInfo(name = "city") var city: String,
                   @ColumnInfo(name = "route") var route: String,
                   @ColumnInfo(name = "short_description") var short_description: String,
                   @ColumnInfo(name = "description") var description: String,
                   @ColumnInfo(name = "latitude") var latitude: Double,
                   @ColumnInfo(name = "longitude") var longitude: Double,
                   @ColumnInfo(name = "path") var path: String,
                   @ColumnInfo(name = "route_type") var route_type: Int) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
