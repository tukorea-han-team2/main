package com.example.project

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MapDataFetcher(private val context: Context) {

    fun fetchSidoData(): List<List<Pair<Double, Double>>> {
        val polygons = mutableListOf<List<Pair<Double, Double>>>()

        try {
            val inputStream = context.resources.openRawResource(R.raw.sido)
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
                    val coordinatesArray =
                        geometryObject.getJSONArray("coordinates").getJSONArray(0)

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

    fun fetchSigData(): List<List<Pair<Double, Double>>> {
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
                    val coordinatesArray =
                        geometryObject.getJSONArray("coordinates").getJSONArray(0)

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
}