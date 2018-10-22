package com.baikaleg.v3.autoinfo.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "routeData")
data class Route(@ColumnInfo(name = "route") var route: String,
                 @ColumnInfo(name = "is_circle") var isCircle: Boolean,
                 @ColumnInfo(name = "city") var city: String,
                 @ColumnInfo(name = "stations") var stations: List<Station>) : Parcelable {

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.createTypedArrayList(Station)) {
        id = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(route)
        parcel.writeByte(if (isCircle) 1 else 0)
        parcel.writeString(city)
        parcel.writeTypedList(stations)
        parcel.writeLong(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Route> {
        override fun createFromParcel(parcel: Parcel): Route {
            return Route(parcel)
        }

        override fun newArray(size: Int): Array<Route?> {
            return arrayOfNulls(size)
        }
    }


}