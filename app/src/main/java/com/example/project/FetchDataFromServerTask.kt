package com.example.project

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapPolyline
import net.daum.mf.map.api.MapView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class FetchDataFromServerTask(private val context: Context, private val mapView: MapView) : AsyncTask<Void, Void, Pair<List<MapPOIItem>, List<List<Pair<Double, Double>>>>>() {

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): Pair<List<MapPOIItem>, List<List<Pair<Double, Double>>>>? {
        val crimeMarkers = fetchCrimeData()
        val sigPolygons = fetchSigData()

        return Pair(crimeMarkers, sigPolygons)
    }

    private fun fetchCrimeData(): List<MapPOIItem> {
        val markers = mutableListOf<MapPOIItem>()

        try {
            val url = URL("https://cha8041.pythonanywhere.com/senddata/crime_data")
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

                val latitudeArray = jsonObject.getJSONArray("latitude")
                val longitudeArray = jsonObject.getJSONArray("longitude")
                val murderArray = jsonObject.getJSONArray("MURDER")

                val length = minOf(latitudeArray.length(), longitudeArray.length(), murderArray.length())

                for (i in 0 until length) {
                    val latitudeString = latitudeArray.getString(i)
                    val longitudeString = longitudeArray.getString(i)
                    val crimeType = murderArray.getInt(i)

                    val latitude = latitudeString.trim().toDoubleOrNull()
                    val longitude = longitudeString.trim().toDoubleOrNull()

                    if (latitude != null && longitude != null) {
                        val marker = MapPOIItem()
                        marker.itemName = "Marker $i"
                        marker.tag = i
                        marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                        marker.isShowCalloutBalloonOnTouch = true

                        // Set marker color based on crime type
                        when (crimeType) {
                            1 -> {
                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger1
                                marker.setCustomImageAnchor(0.01f, 0.01f) // Optional: set anchor point for custom image
                            }
                            2 -> {
                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger2
                                marker.setCustomImageAnchor(0.01f, 0.01f) // Optional: set anchor point for custom image
                            }
                            3 -> {
                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger3
                                marker.setCustomImageAnchor(0.01f, 0.01f) // Optional: set anchor point for custom image
                            }
                            4 -> {
                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger4
                                marker.setCustomImageAnchor(0.01f, 0.01f) // Optional: set anchor point for custom image
                            }
                            5 -> {
                                marker.markerType = MapPOIItem.MarkerType.CustomImage
                                marker.customImageResourceId = R.drawable.danger5
                                marker.setCustomImageAnchor(0.01f, 0.01f) // Optional: set anchor point for custom image
                            }
                            // You can add more cases if needed
                            else -> marker.markerType = MapPOIItem.MarkerType.CustomImage
                        }

                        markers.add(marker)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return markers
    }


    private fun fetchSigData(): List<List<Pair<Double, Double>>> {
        val polygons = mutableListOf<List<Pair<Double, Double>>>()

        try {
            val inputStream = context.resources.openRawResource(R.raw.sig)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            val jsonObject = JSONObject(response.toString())
            val featuresArray = jsonObject.getJSONArray("features")

            for (i in 0 until featuresArray.length()) {
                val featureObject = featuresArray.getJSONObject(i)
                val geometryObject = featureObject.getJSONObject("geometry")
                val coordinatesArray = geometryObject.getJSONArray("coordinates").getJSONArray(0)

                val points = mutableListOf<Pair<Double, Double>>()
                for (j in 0 until coordinatesArray.length()) {
                    val coordinateArray = coordinatesArray.getJSONArray(j)
                    val latitude = coordinateArray.getDouble(1)
                    val longitude = coordinateArray.getDouble(0)
                    points.add(Pair(latitude, longitude))
                }
                polygons.add(points)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return polygons
    }




    @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Pair<List<MapPOIItem>, List<List<Pair<Double, Double>>>>?) {
        super.onPostExecute(result)
        result?.let { Pair ->
            val markers = Pair.first
            val polygons = Pair.second

            markers.forEach { marker ->
                mapView.addPOIItem(marker)
            }

            polygons.forEach { points ->
                val polyline = MapPolyline()
                points.forEach { (latitude, longitude) ->
                    polyline.addPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude))
                }
                polyline.lineColor = Color.argb(128, 255, 0, 0)
                mapView.addPolyline(polyline)
            }
        }
    }

}
