package com.example.project


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class LocationServiceExample(private val activity: Activity) {

    private var locationManager: LocationManager? = null
    private var myLocationListener: MyLocationListener? = null
    private var previousSggKorNm: String? = null

    init {
        // 위치 관리자 초기화
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    private var selectedLevel: Int = 4

    fun getSelectedLevel(): Int {
        return selectedLevel
    }

    fun setSelectedLevel(level: Int) {
        selectedLevel = level
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
        val latitude = location.latitude
        val longitude = location.longitude

        val currentSelectedLevel = getSelectedLevel()

        FetchDataTask(activity, latitude, longitude) { result ->
            result?.let { newSggKorNm ->
                displayPopupIfLocationChanged(newSggKorNm, currentSelectedLevel)
            }
        }.execute()
    }

    fun displayPopupIfLocationChanged(newSggKorNm: String?, selectedLevel: Int) {
        if (newSggKorNm != previousSggKorNm) {
            // 이전 위치 정보와 현재 위치 정보가 다른 경우에만 새로운 팝업 창 표시
            Crime(activity).fetchMatchingSGGKorNmAndSIGKorNm(selectedLevel) { sggKorNmList ->
                sggKorNmList?.let { list ->
                    if (list.contains(newSggKorNm)) {
                        // MURDER 값이 selectedLevel 이상인 경우에만 팝업 창 표시
                        displayPopupDialog(newSggKorNm as String, selectedLevel)
                    }
                }
            }

            // 이전 위치 정보 업데이트
            previousSggKorNm = newSggKorNm
        }
    }

    fun displayPopupDialog(newSggKorNm: String, selectedLevel: Int) {
        // Activity가 유효한지 확인
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        val message = "$newSggKorNm 는 $selectedLevel 단계 이상으로 위험지역입니다."

        // AlertDialog 생성
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss() // 확인 버튼을 누르면 팝업 창이 닫힘
            }
            .create()
            .show()
    }


    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            handleLocationUpdate(location) // 이게 있어야 위치업데이트 시작
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("StaticFieldLeak")
    private inner class FetchDataTask(
        private val context: Context,
        private val latitude: Double,
        private val longitude: Double,
        private val callback: (String?) -> Unit
    ) : AsyncTask<Void, Void, String?>() {

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): String? {
            return fetchData(latitude, longitude)
        }

        @Deprecated("Deprecated in Java", ReplaceWith("result?.let { callback(it) }"))
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