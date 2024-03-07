package com.example.project

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapPolyline
import net.daum.mf.map.api.MapView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class FetchDataFromServerTask(private val context: Context, private val mapView: MapView) : AsyncTask<Void, Void, Pair<List<MapPOIItem>, List<List<Pair<Double, Double>>>>>() {

    override fun doInBackground(vararg params: Void?): Pair<List<MapPOIItem>, List<List<Pair<Double, Double>>>>? {
        val trafficMarkers = fetchTrafficData()
        val sidoPolygons = fetchSidoData()

        return Pair(trafficMarkers, sidoPolygons)
    }

    private fun fetchTrafficData(): List<MapPOIItem> {
        val markers = mutableListOf<MapPOIItem>()

        try {
            val url = URL("https://cha8041.pythonanywhere.com/senddata/traffic_data")
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

                val latitudeArray = jsonObject.getJSONArray("la_crd")
                val longitudeArray = jsonObject.getJSONArray("lo_crd")

                val length = minOf(latitudeArray.length(), longitudeArray.length())

                for (i in 0 until length) {
                    val latitudeString = latitudeArray.getString(i)
                    val longitudeString = longitudeArray.getString(i)

                    val latitude = latitudeString.trim().toDoubleOrNull()
                    val longitude = longitudeString.trim().toDoubleOrNull()

                    if (latitude != null && longitude != null) {
                        val marker = MapPOIItem()
                        marker.itemName = "Marker $i"
                        marker.tag = i
                        marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                        marker.markerType = MapPOIItem.MarkerType.RedPin
                        marker.isShowCalloutBalloonOnTouch = true
                        markers.add(marker)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return markers
    }

    private fun fetchSidoData(): List<List<Pair<Double, Double>>> {
        val polygons = mutableListOf<List<Pair<Double, Double>>>()

        try {
            val urlPolygon = URL("https://cha8041.pythonanywhere.com/senddata/sido1")
            val connectionPolygon = urlPolygon.openConnection() as HttpURLConnection
            connectionPolygon.requestMethod = "GET"
            connectionPolygon.connect()

            val responseCodePolygon = connectionPolygon.responseCode
            if (responseCodePolygon == HttpURLConnection.HTTP_OK) {
                val inputStreamPolygon = connectionPolygon.inputStream
                val readerPolygon = BufferedReader(InputStreamReader(inputStreamPolygon))
                val responsePolygon = StringBuilder()
                var linePolygon: String?
                while (readerPolygon.readLine().also { linePolygon = it } != null) {
                    responsePolygon.append(linePolygon)
                }

                val jsonObjectPolygon = JSONObject(responsePolygon.toString())
                val featuresArray = jsonObjectPolygon.getJSONArray("features")

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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return polygons
    }

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
