package com.baikaleg.v3.autoinfo.data.model

import android.os.Parcel
import android.os.Parcelable

data class Station(var id: Int = 0,
                   var shortDescription: String,
                   var description: String,
                   var latitude: Double,
                   var longitude: Double,
                   var isDirect: Boolean): Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(shortDescription)
        parcel.writeString(description)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeByte(if (isDirect) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel): Station {
            return Station(parcel)
        }

        override fun newArray(size: Int): Array<Station?> {
            return arrayOfNulls(size)
        }
    }
}
