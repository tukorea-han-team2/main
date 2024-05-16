package com.example.project

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class AccidentDataFetcher(private val context: Context) {

    data class AccidentLocation(
        val latitude: Double,
        val longitude: Double,
        val rn: String
    )

    data class RoadData(
        val road: String,
        val epdo: Double,
        val dangerous: Int
    )

    /* fun fetchAccidentData(callback: (List<AccidentLocation>?) -> Unit) {
        val accidentLocations = mutableListOf<AccidentLocation>()

        try {
            val inputStream = context.resources.openRawResource(R.raw.accident1)
            val reader = inputStream.bufferedReader()
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            val jsonArray = JSONArray(response.toString())

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val latitude = jsonObject.getDouble("위도")
                val longitude = jsonObject.getDouble("경도")
                val roadName = jsonObject.getString("rn")
                accidentLocations.add(AccidentLocation(latitude, longitude, roadName))
            }

            callback(accidentLocations)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        }
    }
*/
    fun fetchRoadInformation(latitude: Double, longitude: Double, callback: (RoadData?) -> Unit) {
        val url = "http://220.88.8.183:8000/senddata/road?longitude=$longitude&latitude=$latitude"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val road = response.getString("road")
                    val epdo = response.getDouble("epdo")
                    val dangerous = response.getInt("dangerous")
                    callback(RoadData(road, epdo, dangerous))
                } catch (e: JSONException) {
                    e.printStackTrace()
                    callback(null)
                }
            },
            { error ->
                error?.printStackTrace()
                callback(null)
            }
        )

        Volley.newRequestQueue(context).add(request)
    }
}