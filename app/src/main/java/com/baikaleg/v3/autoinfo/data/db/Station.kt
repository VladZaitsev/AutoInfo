package com.baikaleg.v3.autoinfo.data.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "stationData")
data  class Station(@PrimaryKey(autoGenerate = true) var id: Long?,
                    @ColumnInfo(name = "city") var city: String,
                    @ColumnInfo(name = "route") var route: String,
                    @ColumnInfo(name = "description") var description: String,
                    @ColumnInfo(name = "latitude") var latitude: Float,
                    @ColumnInfo(name = "longitude") var longitude: Float
)
