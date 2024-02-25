package com.example.project

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class LocationServiceExample(private val context: Context) {

    private var locationManager: LocationManager? = null
    private lateinit var myLocationListener: MyLocationListener

    init {
        // 위치 관리자 초기화
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        // 위치 권한을 확인하고, 사용자에게 요청
        // (권한 처리 부분은 실제 상황에 따라 적절하게 처리해야 합니다.)
    }

    fun startLocationUpdates() {
        // 위치 업데이트를 받을 LocationListener 등록
        myLocationListener = MyLocationListener()
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0, // 업데이트 간격 (0: 가능한 빠르게)
                0f, // 업데이트 간격 (미터 단위)
                myLocationListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        // 위치 업데이트 중지
        locationManager?.removeUpdates(myLocationListener)
    }

    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            // 위치가 변경될 때 호출됩니다.
            // 새로운 위치 정보를 처리하는 코드를 여기에 추가하세요.
            val latitude = location.latitude
            val longitude = location.longitude


            // CTPRVN_CD 확인 및 출력
            fetchAndShowCTPRVN_CD(latitude, longitude)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // 위치 공급자의 상태가 변경될 때 호출됩니다.
            // 예를 들어, GPS가 사용 가능해지거나 사용 불가능해질 때 호출됩니다.
        }

        override fun onProviderEnabled(provider: String) {
            // 위치 공급자가 사용 가능해질 때 호출됩니다.
        }

        override fun onProviderDisabled(provider: String) {
            // 위치 공급자가 사용 불가능해질 때 호출됩니다.
        }
    }

    // 토스트 메시지 출력 함수
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 사용자 위치에 대한 CTPRVN_CD 확인 및 출력 함수
    private fun fetchAndShowCTPRVN_CD(latitude: Double, longitude: Double) {
        AsyncTask.execute {
            try {
                val url = URL("https://cha8041.pythonanywhere.com/senddata/sido")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    val jsonArray = JSONArray(response.toString())
                    val ctpCode = getCTPRVN_CD(latitude, longitude, jsonArray)

                    // UI 스레드에서 토스트 메시지를 표시
                    Handler(Looper.getMainLooper()).post {
                        showToast(ctpCode ?: "CTPRVN_CD not found")
                    }
                } else {
                    // UI 스레드에서 토스트 메시지를 표시
                    Handler(Looper.getMainLooper()).post {
                        showToast("Failed to fetch data")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("LocationServiceExample", "Error fetching location information: ${e.message}")
                // UI 스레드에서 토스트 메시지를 표시
                Handler(Looper.getMainLooper()).post {
                    showToast("Error fetching location information")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("LocationServiceExample", "Error parsing JSON response: ${e.message}")
                // UI 스레드에서 토스트 메시지를 표시
                Handler(Looper.getMainLooper()).post {
                    showToast("Error parsing JSON response")
                }
            }
        }
    }








    // 주어진 위치의 "CTPRVN_CD"를 반환하는 함수
    private fun getCTPRVN_CD(latitude: Double, longitude: Double, sidoData: JSONArray): String? {
        for (i in 0 until sidoData.length()) {
            val feature = sidoData.getJSONObject(i)
            val geometry = feature.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates").getJSONArray(0)
            if (isLocationInsidePolygon(latitude, longitude, coordinates)) {
                val properties = feature.getJSONObject("properties")
                return properties.getString("CTPRVN_CD")
            }
        }
        return null
    }

    // 주어진 위치가 다각형 내에 있는지 확인하는 함수
    private fun isLocationInsidePolygon(latitude: Double, longitude: Double, coordinates: JSONArray): Boolean {
        var isInside = false
        for (i in 0 until coordinates.length()) {
            val polygon = coordinates.getJSONArray(i)
            var j = polygon.length() - 1
            var intersect = false
            for (k in 0 until polygon.length()) {
                val xi = polygon.getJSONArray(k).getDouble(0)
                val yi = polygon.getJSONArray(k).getDouble(1)
                val xj = polygon.getJSONArray(j).getDouble(0)
                val yj = polygon.getJSONArray(j).getDouble(1)

                // 다각형의 각 변과 주어진 점 사이의 관계를 확인합니다.
                if (yi < longitude && yj >= longitude || yj < longitude && yi >= longitude) {
                    if (xi + (longitude - yi) / (yj - yi) * (xj - xi) < latitude) {
                        intersect = !intersect
                    }
                }
                j = k
            }
            isInside = if (intersect) !isInside else isInside
        }
        return isInside
    }



}