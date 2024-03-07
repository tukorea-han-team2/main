package com.example.project

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
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

            // AsyncTask를 통해 백그라운드에서 네트워크 호출 수행
            FetchDataTask(context).execute(latitude, longitude)
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

    // AsyncTask 클래스 정의
    private inner class FetchDataTask(private val context: Context) : AsyncTask<Double, Void, String?>() {

        override fun doInBackground(vararg params: Double?): String? {
            val latitude = params[0]
            val longitude = params[1]

            // 백그라운드에서 네트워크 호출 수행하여 시/도 코드 반환
            return fetchData(latitude, longitude)
        }

        override fun onPostExecute(result: String?) {
            // 네트워크 호출 완료 후 UI 업데이트 등을 수행
            Toast.makeText(context, "시/도 코드: $result", Toast.LENGTH_SHORT).show()
        }

        // 네트워크 호출을 수행하는 함수
        private fun fetchData(latitude: Double?, longitude: Double?): String? {
            try {
                val url = URL("https://cha8041.pythonanywhere.com/senddata/sido1")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    val jsonObject = JSONObject(response.toString())
                    val jsonArray = jsonObject.getJSONArray("features")

                    // 주어진 위치의 위도와 경도를 사용하여 시/도 코드를 결정
                    if (latitude != null && longitude != null) {
                        for (i in 0 until jsonArray.length()) {
                            val feature = jsonArray.getJSONObject(i)
                            val geometry = feature.getJSONObject("geometry")
                            val coordinates = geometry.getJSONArray("coordinates")
                            val properties = feature.getJSONObject("properties")

                            // 현재 위치가 다각형 내부에 있는지 확인
                            if (isLocationInsidePolygon(latitude, longitude, coordinates)) {
                                return properties.getString("SIG_KOR_NM")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return "데이터를 가져오는 중 오류가 발생했습니다."
            }
            return null
        }

        // 주어진 위치가 다각형 내부에 있는지 확인하는 함수
        private fun isLocationInsidePolygon(latitude: Double, longitude: Double, coordinates: JSONArray): Boolean {
            var isInside = false
            for (i in 0 until coordinates.length()) {
                val polygon = coordinates.getJSONArray(i)
                var j = polygon.length() - 1
                var intersect = false
                for (k in 0 until polygon.length()) {
                    val yi = polygon.getJSONArray(k).getDouble(0)
                    val xi = polygon.getJSONArray(k).getDouble(1)
                    val yj = polygon.getJSONArray(j).getDouble(0)
                    val xj = polygon.getJSONArray(j).getDouble(1)

                    // 다각형의 각 변과 주어진 점 사이의 관계를 확인합니다.
                    val intersectCondition = (yi < longitude && yj >= longitude) || (yj < longitude && yi >= longitude)
                    val xIntersection = xi + (longitude - yi) / (yj - yi) * (xj - xi)

                    if (intersectCondition && xIntersection < latitude) {
                        intersect = !intersect
                    }
                    j = k
                }
                isInside = if (intersect) !isInside else isInside
            }
            return isInside
        }
    }
}
