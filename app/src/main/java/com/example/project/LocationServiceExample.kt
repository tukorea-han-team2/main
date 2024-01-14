package com.example.project

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import java.io.IOException
import java.util.*

class LocationServiceExample(private val context: Context) {

    private var locationManager: LocationManager? = null

    init {
        // 위치 관리자 초기화
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        // 위치 권한을 확인하고, 사용자에게 요청
        // (권한 처리 부분은 실제 상황에 따라 적절하게 처리해야 합니다.)
    }

    fun startLocationUpdates() {
        // 위치 업데이트를 받을 LocationListener 등록
        val locationListener = MyLocationListener()
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0, // 업데이트 간격 (0: 가능한 빠르게)
                0f, // 업데이트 간격 (미터 단위)
                locationListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        // 위치 업데이트 중지
        locationManager?.removeUpdates(MyLocationListener())
    }

    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            // 위치가 변경되었을 때 호출
            val latitude = location.latitude
            val longitude = location.longitude

            // 좌표를 주소로 변환
            val address = getAddressFromLocation(latitude, longitude)

            // 변환된 주소를 사용하여 필요한 작업 수행
            Toast.makeText(context, "현재 주소: $address", Toast.LENGTH_SHORT).show()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // 위치 공급자 상태 변경 시 호출
        }

        override fun onProviderEnabled(provider: String) {
            // 위치 공급자가 사용 가능해질 때 호출
        }

        override fun onProviderDisabled(provider: String) {
            // 위치 공급자가 사용 불가능해질 때 호출
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addressText = "주소를 가져오는 중 오류가 발생했습니다."

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                addressText = address.getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressText
    }
}
