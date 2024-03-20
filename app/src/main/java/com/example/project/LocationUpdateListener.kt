package com.example.project

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class LocationUpdateListener(private val context: Context) : LocationListener {

    private val Alarmset = Alarmset()
    private val locationServiceExample = LocationServiceExample(context)
    private val crime = Crime(context)

    override fun onLocationChanged(location: Location) {
        // 위치가 변경될 때 호출합니다.
        locationServiceExample.handleLocationUpdate(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // 위치 공급자의 상태가 변경될 때 호출됩니다.
    }

    override fun onProviderEnabled(provider: String) {
        // 위치 공급자가 사용 가능해질 때 호출됩니다.
    }

    override fun onProviderDisabled(provider: String) {
        // 위치 공급자가 사용 불가능해질 때 호출됩니다.
    }
}