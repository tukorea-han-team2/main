package com.example.project


import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import com.example.project.gs.MatchingLevelManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader



class LocationServiceExample(private val context: Context) {

    private var locationManager: LocationManager? = null
    private var myLocationListener: MyLocationListener? = null
    private val crimeInstance = Crime(context)
    private var previousSggKorNm: String? = null
    private var matchingLevelManager = MatchingLevelManager(4)

    init {
        // 위치 관리자 초기화
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    // 매칭 레벨을 저장하기 위한 변수
    private var selectedLevel = 4

    // 선택된 매칭 레벨을 설정하는 함수
    fun setSelectedLevel(level: Int) {
        selectedLevel = level
    }

    // 현재 선택된 매칭 레벨을 가져오는 함수
    fun getSelectedLevel(): Int {
        return selectedLevel
    }

    fun startLocationUpdates() {
        // 위치 업데이트를 받을 LocationListener 등록
        if (myLocationListener == null) {
            myLocationListener = MyLocationListener()
        }
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100, // 업데이트 간격 (0: 가능한 빠르게)
                100f, // 업데이트 간격 (미터 단위)
                myLocationListener!!
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        // 위치 업데이트 중지
        myLocationListener?.let { listener ->
            locationManager?.removeUpdates(listener)
        }
    }

    fun handleLocationUpdate(location: Location) {
        // 위치가 변경될 때 호출됩니다.
        // 새로운 위치 정보를 처리하는 코드를 여기에 추가하세요.
        val latitude = location.latitude
        val longitude = location.longitude

        FetchDataTask(context, latitude, longitude) { result ->
            result?.let { newSggKorNm ->
                if (newSggKorNm != previousSggKorNm) {
                    previousSggKorNm = newSggKorNm
                    displayToast(newSggKorNm)
                }
            }
        }.execute()
    }

    private fun displayToast(newSggKorNm: String) {
        val toastMessage = "$newSggKorNm 는 ${getSelectedLevel()}단계 이상으로 위험지역입니다."
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            handleLocationUpdate(location)
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private inner class FetchDataTask(
        private val context: Context,
        private val latitude: Double,
        private val longitude: Double,
        private val callback: (String?) -> Unit
    ) : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg params: Void?): String? {
            return fetchData(latitude, longitude)
        }

        override fun onPostExecute(result: String?) {
            result?.let { callback(it) }
        }

        private fun fetchData(latitude: Double, longitude: Double): String? {
            try {
                val inputStream = context.resources.openRawResource(R.raw.sig)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                val jsonObject = JSONObject(response.toString())
                val jsonArray = jsonObject.getJSONArray("features")

                for (i in 0 until jsonArray.length()) {
                    val feature = jsonArray.getJSONObject(i)
                    val geometry = feature.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    val properties = feature.getJSONObject("properties")

                    if (isLocationInsidePolygon(latitude, longitude, coordinates)) {
                        return properties.getString("SIG_KOR_NM")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return "데이터를 가져오는 중 오류가 발생"
            }
            return null
        }

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