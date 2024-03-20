package com.example.project

import android.content.Context
import android.os.AsyncTask
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Crime(private val context: Context) {

    // 수정된 fetchMatchingSGGKorNmAndSIGKorNm 메서드
    fun fetchMatchingSGGKorNmAndSIGKorNm(murderCountThreshold: Int, callback: (List<String>?) -> Unit) {
        FetchDataTask(context, murderCountThreshold, callback).execute()
    }

    private inner class FetchDataTask(
        private val context: Context,
        private val murderCountThreshold: Int,
        private val callback: (List<String>?) -> Unit
    ) : AsyncTask<Void, Void, List<String>?>() {

        override fun doInBackground(vararg params: Void?): List<String>? {
            // 백그라운드에서 네트워크 호출 수행하여 데이터 가져오기
            return fetchData()
        }

        override fun onPostExecute(result: List<String>?) {
            // UI 업데이트 등 결과 처리
            callback(result)
        }

        private fun fetchData(): List<String>? {
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
                    val MURDERArray = jsonObject.getJSONArray("MURDER")
                    val SGG_KOR_NM_Array = jsonObject.getJSONArray("SGG_KOR_NM")

                    // MURDER이 임계값 이상인 지역의 SGG_KOR_NM 찾기
                    val sggKorNmList = mutableListOf<String>()
                    if (MURDERArray != null)
                        for (i in 0 until MURDERArray.length()) {
                            val murderValue = MURDERArray.getInt(i)
                            if (murderValue >= murderCountThreshold) {
                                val sggKorNm = SGG_KOR_NM_Array.getString(i)
                                sggKorNmList.add(sggKorNm)
                            }
                    }

                    // SGG_KOR_NM 리스트 반환
                    if (sggKorNmList.isNotEmpty()) {
                        return sggKorNmList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}
