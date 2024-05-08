package com.example.project

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class AccidentDataFetcher(private val context: Context) {

    fun fetchAccidentData(): List<List<Pair<Double, Double>>> {
        val polygons = mutableListOf<List<Pair<Double, Double>>>()

        try {
            val inputStream = context.resources.openRawResource(R.raw.accident)
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