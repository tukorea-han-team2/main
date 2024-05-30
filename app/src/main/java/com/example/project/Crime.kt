package com.example.project

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


class Crime(private val context: Context) {

    fun fetchMatchingSGGKorNmAndSIGKorNm(
        murderCountThreshold: Int,
        callback: (List<String>?) -> Unit
    ) {
        FetchDataTask(context, murderCountThreshold, callback).execute()
    }

    companion object {
        private class FetchDataTask(
            @SuppressLint("StaticFieldLeak") private val context: Context,
            private var murderCountThreshold: Int,
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
                    val inputStream = context.resources.openRawResource(R.raw.crime)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    val jsonObject = JSONObject(response.toString())
                    val mURDERArray = jsonObject.getJSONArray("MURDER")
                    val SGG_KOR_NM_Array = jsonObject.getJSONArray("SGG_KOR_NM")

                    // MURDER이 임계값 이상인 지역의 SGG_KOR_NM 찾기
                    val sggKorNmList = mutableListOf<String>()
                    for (i in 0 until mURDERArray.length()) {
                        val murderValue = mURDERArray.getInt(i)
                        if (murderValue >= murderCountThreshold) {
                            val sggKorNm = SGG_KOR_NM_Array.getString(i)
                            sggKorNmList.add(sggKorNm)
                        }
                    }

                    // SGG_KOR_NM 리스트 반환
                    if (sggKorNmList.isNotEmpty()) {
                        return sggKorNmList
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }
}
