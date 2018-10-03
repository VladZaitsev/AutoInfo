package com.baikaleg.v3.autoinfo.ui.stations.station

import com.baikaleg.v3.autoinfo.data.model.Station

interface OnStationClickNavigator {
    fun onClick(station: Station)
}