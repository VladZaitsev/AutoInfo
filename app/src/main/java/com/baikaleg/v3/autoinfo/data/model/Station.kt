package com.baikaleg.v3.autoinfo.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * @param routeType <code>0</code> if station is in forward direction
 *                   <code>1</code> if station is in backward direction
 *                   <code>2</code> if station is in circular direction
 */
@Entity(tableName = "stationData")
data class Station(@ColumnInfo(name = "route") var route: String,
                   @ColumnInfo(name = "short_description") var shortDescription: String,
                   @ColumnInfo(name = "latitude") var latitude: Double,
                   @ColumnInfo(name = "longitude") var longitude: Double,
                   @ColumnInfo(name = "route_type") var routeType: Int) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "order_number")
    var orderNumber: Int = 0

    @ColumnInfo(name = "voice_path")
    var voicePath: String = ""

    @ColumnInfo(name = "city")
    var city: String = ""

    @ColumnInfo(name = "description")
    var description: String = ""
}
