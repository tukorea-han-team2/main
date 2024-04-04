package com.example.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CrimeDataFetcher {

    suspend fun fetchCrime1Data(): List<MapPOIItem> = withContext(Dispatchers.IO) {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val url = URL("https://cha8041.pythonanywhere.com/senddata/crime_data")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    val jsonObject = JSONObject(response.toString())

                    val latitudeArray = jsonObject.getJSONArray("latitude")
                    val longitudeArray = jsonObject.getJSONArray("longitude")
                    val murderArray = jsonObject.getJSONArray("MURDER")

                    val length = minOf(latitudeArray.length(), longitudeArray.length(), murderArray.length())

                    for (i in 0 until length) {
                        val latitudeString = latitudeArray.getString(i)
                        val longitudeString = longitudeArray.getString(i)
                        val crimeType = murderArray.getInt(i)

                        if (crimeType == 1) {

                            val latitude = latitudeString.trim().toDoubleOrNull()
                            val longitude = longitudeString.trim().toDoubleOrNull()

                            if (latitude != null && longitude != null) {
                                val marker = MapPOIItem()
                                marker.itemName = "Marker $i"
                                marker.tag = i
                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                                marker.isShowCalloutBalloonOnTouch = true

                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger1
                                marker.setCustomImageAnchor(
                                    0.01f,
                                    0.01f
                                ) // Optional: set anchor point for custom image

                                markers.add(marker)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        markers
    }


    suspend fun fetchCrime2Data(): List<MapPOIItem> = withContext(Dispatchers.IO) {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val url = URL("https://cha8041.pythonanywhere.com/senddata/crime_data")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    val jsonObject = JSONObject(response.toString())

                    val latitudeArray = jsonObject.getJSONArray("latitude")
                    val longitudeArray = jsonObject.getJSONArray("longitude")
                    val murderArray = jsonObject.getJSONArray("MURDER")

                    val length = minOf(latitudeArray.length(), longitudeArray.length(), murderArray.length())

                    for (i in 0 until length) {
                        val latitudeString = latitudeArray.getString(i)
                        val longitudeString = longitudeArray.getString(i)
                        val crimeType = murderArray.getInt(i)

                        if (crimeType == 2) {

                            val latitude = latitudeString.trim().toDoubleOrNull()
                            val longitude = longitudeString.trim().toDoubleOrNull()

                            if (latitude != null && longitude != null) {
                                val marker = MapPOIItem()
                                marker.itemName = "Marker $i"
                                marker.tag = i
                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                                marker.isShowCalloutBalloonOnTouch = true

                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger2
                                marker.setCustomImageAnchor(
                                    0.01f,
                                    0.01f
                                ) // Optional: set anchor point for custom image

                                markers.add(marker)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        markers
    }


    suspend fun fetchCrime3Data(): List<MapPOIItem> = withContext(Dispatchers.IO) {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val url = URL("https://cha8041.pythonanywhere.com/senddata/crime_data")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    val jsonObject = JSONObject(response.toString())

                    val latitudeArray = jsonObject.getJSONArray("latitude")
                    val longitudeArray = jsonObject.getJSONArray("longitude")
                    val murderArray = jsonObject.getJSONArray("MURDER")

                    val length = minOf(latitudeArray.length(), longitudeArray.length(), murderArray.length())

                    for (i in 0 until length) {
                        val latitudeString = latitudeArray.getString(i)
                        val longitudeString = longitudeArray.getString(i)
                        val crimeType = murderArray.getInt(i)

                        if (crimeType == 3) {

                            val latitude = latitudeString.trim().toDoubleOrNull()
                            val longitude = longitudeString.trim().toDoubleOrNull()

                            if (latitude != null && longitude != null) {
                                val marker = MapPOIItem()
                                marker.itemName = "Marker $i"
                                marker.tag = i
                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                                marker.isShowCalloutBalloonOnTouch = true

                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger3
                                marker.setCustomImageAnchor(
                                    0.01f,
                                    0.01f
                                ) // Optional: set anchor point for custom image

                                markers.add(marker)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        markers
    }


    suspend fun fetchCrime4Data(): List<MapPOIItem> = withContext(Dispatchers.IO) {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val url = URL("https://cha8041.pythonanywhere.com/senddata/crime_data")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    val jsonObject = JSONObject(response.toString())

                    val latitudeArray = jsonObject.getJSONArray("latitude")
                    val longitudeArray = jsonObject.getJSONArray("longitude")
                    val murderArray = jsonObject.getJSONArray("MURDER")

                    val length = minOf(latitudeArray.length(), longitudeArray.length(), murderArray.length())

                    for (i in 0 until length) {
                        val latitudeString = latitudeArray.getString(i)
                        val longitudeString = longitudeArray.getString(i)
                        val crimeType = murderArray.getInt(i)

                        if (crimeType == 4) {

                            val latitude = latitudeString.trim().toDoubleOrNull()
                            val longitude = longitudeString.trim().toDoubleOrNull()

                            if (latitude != null && longitude != null) {
                                val marker = MapPOIItem()
                                marker.itemName = "Marker $i"
                                marker.tag = i
                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                                marker.isShowCalloutBalloonOnTouch = true

                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger4
                                marker.setCustomImageAnchor(
                                    0.01f,
                                    0.01f
                                ) // Optional: set anchor point for custom image

                                markers.add(marker)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        markers
    }


    suspend fun fetchCrime5Data(): List<MapPOIItem> = withContext(Dispatchers.IO) {
        val markers = mutableListOf<MapPOIItem>()
        try {
            val url = URL("https://cha8041.pythonanywhere.com/senddata/crime_data")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    val jsonObject = JSONObject(response.toString())

                    val latitudeArray = jsonObject.getJSONArray("latitude")
                    val longitudeArray = jsonObject.getJSONArray("longitude")
                    val murderArray = jsonObject.getJSONArray("MURDER")

                    val length = minOf(latitudeArray.length(), longitudeArray.length(), murderArray.length())

                    for (i in 0 until length) {
                        val latitudeString = latitudeArray.getString(i)
                        val longitudeString = longitudeArray.getString(i)
                        val crimeType = murderArray.getInt(i)

                        if (crimeType == 5) {

                            val latitude = latitudeString.trim().toDoubleOrNull()
                            val longitude = longitudeString.trim().toDoubleOrNull()

                            if (latitude != null && longitude != null) {
                                val marker = MapPOIItem()
                                marker.itemName = "Marker $i"
                                marker.tag = i
                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                                marker.isShowCalloutBalloonOnTouch = true

                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger5
                                marker.setCustomImageAnchor(
                                    0.01f,
                                    0.01f
                                ) // Optional: set anchor point for custom image

                                markers.add(marker)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        markers
    }

}