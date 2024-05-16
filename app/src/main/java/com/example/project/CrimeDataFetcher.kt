package com.example.project

import android.content.Context
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
class CrimeDataFetcher(private val context: Context) {

    private val markerFilter = MarkerFilter()

    fun fetchCrimeData(centerLatitude: Double, centerLongitude: Double, radiusInKm: Double): List<MapPOIItem> {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val inputStream = context.resources.openRawResource(R.raw.crime)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val jsonObject = JSONObject(response.toString())

            val latitudeArray = jsonObject.getJSONArray("latitude")
            val longitudeArray = jsonObject.getJSONArray("longitude")
            val murderArray = jsonObject.getJSONArray("MURDER")

            val length =
                minOf(latitudeArray.length(), longitudeArray.length(), murderArray.length())

            for (i in 0 until length) {
                val latitudeString = latitudeArray.getString(i)
                val longitudeString = longitudeArray.getString(i)
                val latitude = latitudeString.trim().toDoubleOrNull()
                val longitude = longitudeString.trim().toDoubleOrNull()
                val murderLevel = murderArray.getInt(i)

                if (latitude != null && longitude != null) {
                    val distance = markerFilter.distanceInKmBetweenEarthCoordinates(
                        centerLatitude,
                        centerLongitude,
                        latitude,
                        longitude
                    )
                    if (distance <= radiusInKm) {
                        val marker = MapPOIItem()
                        marker.itemName = "Marker $i"
                        marker.tag = i
                        marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                        marker.isShowCalloutBalloonOnTouch = true

                        // MURDER 값에 따라 해당하는 리소스 아이디를 선택합니다.
                        marker.markerType = MapPOIItem.MarkerType.CustomImage
                        marker.customImageResourceId = getMarkerDrawableResourceId(murderLevel)
                        marker.setCustomImageAnchor(0.01f, 0.01f)

                        markers.add(marker)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return markers
    }

    private fun getMarkerDrawableResourceId(crimeLevel: Int): Int {
        return when (crimeLevel) {
            1 -> R.drawable.danger1
            2 -> R.drawable.danger2
            3 -> R.drawable.danger3
            4 -> R.drawable.danger4
            5 -> R.drawable.danger5
            else -> R.drawable.danger5
        }
    }

    fun fetchCrimeData1(mapView: MapView): List<MapPOIItem> {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val inputStream = context.resources.openRawResource(R.raw.crime)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val jsonObject = JSONObject(response.toString())

            val latitudeArray = jsonObject.getJSONArray("latitude")
            val longitudeArray = jsonObject.getJSONArray("longitude")
            val murderArray = jsonObject.getJSONArray("MURDER")

            val length =
                minOf(latitudeArray.length(), longitudeArray.length(), murderArray.length())

            for (i in 0 until length) {
                val latitudeString = latitudeArray.getString(i)
                val longitudeString = longitudeArray.getString(i)
                val latitude = latitudeString.trim().toDoubleOrNull()
                val longitude = longitudeString.trim().toDoubleOrNull()
                val murderLevel = murderArray.getInt(i)

                if (latitude != null && longitude != null) {

                        val marker = MapPOIItem()
                        marker.itemName = "Marker $i"
                        marker.tag = i
                        marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                        marker.isShowCalloutBalloonOnTouch = true

                        // MURDER 값에 따라 해당하는 리소스 아이디를 선택합니다.
                        marker.markerType = MapPOIItem.MarkerType.CustomImage
                        marker.customImageResourceId = getMarkerDrawableResourceId(murderLevel)
                        marker.setCustomImageAnchor(0.01f, 0.01f)

                        markers.add(marker)

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return markers
    }

    fun fetchCrimeSidoData(mapView: MapView): List<MapPOIItem> {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val inputStream = context.resources.openRawResource(R.raw.crimesido)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val jsonObject = JSONObject(response.toString())

            val cityNameArray = jsonObject.getJSONArray("CTPRVN_NM")
            val murderRateArray = jsonObject.getJSONArray("MURDER")
            val latitudeArray = jsonObject.getJSONArray("latitude")
            val longitudeArray = jsonObject.getJSONArray("longitude")

            val length = minOf(cityNameArray.length(), murderRateArray.length(), latitudeArray.length(), longitudeArray.length())

            for (i in 0 until length) {
                val cityName = cityNameArray.getString(i)
                val murderRate = murderRateArray.getDouble(i)
                val latitudeString = latitudeArray.getString(i)
                val longitudeString = longitudeArray.getString(i)
                val latitude = latitudeString.trim().toDoubleOrNull()
                val longitude = longitudeString.trim().toDoubleOrNull()

                if (cityName.isNotEmpty() && latitude != null && longitude != null) {
                    val marker = MapPOIItem()

                    marker.isShowCalloutBalloonOnTouch = true

                    marker.itemName = "시도 지역: $cityName"
                    marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                    marker.markerType = MapPOIItem.MarkerType.RedPin
                    marker.isShowCalloutBalloonOnTouch = true
                    // marker.tag = murderRate.toInt() // 마커에 살인 발생률 정보를 태그로 추가
                    marker.setCustomImageAnchor(0.5f, 1.0f)
                    markers.add(marker)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return markers
    }

    fun fetchCrimeSigData(mapView: MapView): List<MapPOIItem> {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val inputStream = context.resources.openRawResource(R.raw.crimesig)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val jsonObject = JSONObject(response.toString())

            val cityNameArray = jsonObject.getJSONArray("SGG_KOR_NM")
            val murderRateArray = jsonObject.getJSONArray("MURDER")
            val latitudeArray = jsonObject.getJSONArray("latitude")
            val longitudeArray = jsonObject.getJSONArray("longitude")

            val length = minOf(cityNameArray.length(), murderRateArray.length(), latitudeArray.length(), longitudeArray.length())

            for (i in 0 until length) {
                val cityName = cityNameArray.getString(i)
                val murderRate = murderRateArray.getDouble(i)
                val latitudeString = latitudeArray.getString(i)
                val longitudeString = longitudeArray.getString(i)
                val latitude = latitudeString.trim().toDoubleOrNull()
                val longitude = longitudeString.trim().toDoubleOrNull()

                if (cityName.isNotEmpty() && latitude != null && longitude != null) {
                    val marker = MapPOIItem()

                    marker.isShowCalloutBalloonOnTouch = true

                    marker.itemName = "시군구 지역: $cityName"
                    marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                    marker.markerType = MapPOIItem.MarkerType.RedPin
                    marker.isShowCalloutBalloonOnTouch = true
                    // marker.tag = murderRate.toInt() // 마커에 살인 발생률 정보를 태그로 추가
                    marker.setCustomImageAnchor(0.5f, 1.0f)
                    markers.add(marker)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return markers
    }

}