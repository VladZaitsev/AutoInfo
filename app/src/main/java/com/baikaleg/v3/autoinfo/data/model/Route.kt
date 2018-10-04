package com.baikaleg.v3.autoinfo.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "routeData")
data class Route(@ColumnInfo(name = "name") var name: String,
                 @ColumnInfo(name = "is_circle") var isCircle: Boolean) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}