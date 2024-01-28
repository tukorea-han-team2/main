package com.example.project

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
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

    private var previousLocality: String? = null
    private var previousSubLocality: String? = null

    private inner class MyLocationListener : LocationListener {

        private var previousCity: String? = null

        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            val newAddress = getAddressFromLocation(latitude, longitude)

            // 주소 정보를 Geocoder를 사용하여 다시 가져오기
            //val (newCity, newSubLocality) = extractCityAndSubLocalityFromAddress(context, latitude, longitude)
            val newCity = extractCityFromAddress(context, latitude, longitude)
            //val newSubLocality = extractCityFromAddress(context, latitude, longitude)

            // 주소가 변경되었을 때 진동 울림
            if (newCity != previousCity) {
                vibrate()
                previousCity = newCity
                println("City changed! New City: $newCity, New Address: $newAddress")
            }

            println("현재 주소: $newAddress") // 주소 확인용 출력 추가
        }

        // 특정 주소 구성 요소 추출 함수
        private fun extractCityFromAddress(context: Context, latitude: Double, longitude: Double): String? {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            return addresses?.firstOrNull()?.locality
        }

        private fun extractCityAndSubLocalityFromAddress(context: Context, latitude: Double, longitude: Double): Pair<String?, String?> {
            val geocoder = Geocoder(context, Locale.getDefault())
            val address = getAddressFromLocation(latitude, longitude)

            // 주소 정보를 조합
            val newAddress = buildString {
                append("$address ")
            }

            println("Geocoder result - Address: $address")
            println("Combined Address: $newAddress")

            return Pair(null, null) // 이 부분을 지번 주소에서 추출된 값으로 변경
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

        private fun vibrate() {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

            // 디바이스에 진동기가 있는지 및 권한이 부여되었는지 확인
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    // Android Oreo 이전 버전을 위한 코드
                    vibrator.vibrate(1000)
                }
            }
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


