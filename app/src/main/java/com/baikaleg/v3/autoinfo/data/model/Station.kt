package com.baikaleg.v3.autoinfo.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "stationData")
data class Station(@ColumnInfo(name = "route") var route: String,
                   @ColumnInfo(name = "short_description") var shortDescription: String,
                   @ColumnInfo(name = "latitude") var latitude: Double,
                   @ColumnInfo(name = "longitude") var longitude: Double,
                   @ColumnInfo(name = "is_direct") var isDirect: Boolean) {
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
