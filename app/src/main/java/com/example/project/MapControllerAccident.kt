package com.example.project


import android.app.AlertDialog
import android.content.Context

class MapControllerAccident(private val context: Context) {

    private val accidentDataFetcher = AccidentDataFetcher(context)

    fun getRoadInformation(latitude: Double, longitude: Double) {
        accidentDataFetcher.fetchRoadInformation(latitude, longitude) { roadData ->
            roadData?.let { data ->
                val roadName = data.road
                val epdo = data.epdo
                val dangerous = data.dangerous

                // 팝업을 표시할 조건을 설정합니다.
                if (dangerous >= 1) {
                    showDangerousPopup(roadName, epdo, dangerous)
                } else {
                    // 팝업을 표시하지 않을 경우에 대한 로직을 여기에 추가할 수 있습니다.
                }
            } ?: run {
                // 도로 정보를 가져오는 데 실패한 경우, 에러 처리를 수행합니다.
                showError("Failed to fetch road information")
            }
        }
    }

    fun showDangerousPopup(roadName: String, epdo: Double, dangerous: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("위험 도로 안내")
        builder.setMessage("현재 위치 근처에 $roadName 도로의 위험도가 높습니다. 위험도: $dangerous")
        builder.setPositiveButton("확인") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun showError(message: String) {
        // 에러를 보여주는 UI 처리를 수행하는 코드를 작성합니다.
    }
}