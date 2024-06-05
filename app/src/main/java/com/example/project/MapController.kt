package com.example.project


import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import net.daum.mf.map.api.MapView.CurrentLocationEventListener
import net.daum.mf.map.api.MapView.MapViewEventListener

class MapController(
    private val mapView: MapView,
    private val crimeDataFetcher: CrimeDataFetcher
) : CurrentLocationEventListener, MapViewEventListener, MapView.POIItemEventListener {

    private lateinit var kakaoMap: MapView

    private var currentZoomLevel: Int = 0
    private var job: Job? = null
    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("CoroutineException", "$exception")
    }
    private val scope = if (job != null) {
        CoroutineScope(Dispatchers.Main + job!! + handler)
    } else {
        CoroutineScope(Dispatchers.Main + handler)
    }

    private var currentLocation: MapPoint? = null

    init {
        mapView.setMapViewEventListener(this)
        mapView.setCurrentLocationEventListener(this)
        mapView.setPOIItemEventListener(this) // POIItemEventListener 설정
    }

    override fun onMapViewInitialized(mapView: MapView?) {
        mapView?.let {
            this.kakaoMap = it
        }
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
        // 이벤트 핸들러 구현
    }

    override fun onMapViewZoomLevelChanged(mapView: MapView?, level: Int) {
        currentZoomLevel = level
        currentLocation?.let { location ->
            loadMapDataAccordingToZoomLevel(
                location.mapPointGeoCoord.latitude,
                location.mapPointGeoCoord.longitude
            )
        }
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        // 이벤트 핸들러 구현
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
        // 이벤트 핸들러 구현
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
        // 이벤트 핸들러 구현
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
        // 이벤트 핸들러 구현
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
        // 이벤트 핸들러 구현
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
        // 이벤트 핸들러 구현
    }

    override fun onCurrentLocationUpdate(
        mapView: MapView?,
        currentLocation: MapPoint?,
        accuracyInMeters: Float
    ) {
        this.currentLocation = currentLocation
        loadMapDataAccordingToZoomLevel(
            currentLocation?.mapPointGeoCoord?.latitude ?: 0.0,
            currentLocation?.mapPointGeoCoord?.longitude ?: 0.0
        )
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView, p1: Float) {
        // 이벤트 핸들러 구현
    }

    private fun loadMapDataAccordingToZoomLevel(centerLatitude: Double, centerLongitude: Double) {
        // 취소 요청을 핸들링하기 위해 기존 작업을 취소
        job?.cancel()

        job = scope.launch {
            try {
                val crimeData = if(currentZoomLevel > 7){
                    crimeDataFetcher.fetchCrimeSidoData(kakaoMap)
                } else if (5 < currentZoomLevel) {
                    crimeDataFetcher.fetchCrimeSigData(kakaoMap)
                } else {
                    crimeDataFetcher.fetchCrimeData(centerLatitude, centerLongitude, 5.0)
                }
                addMarkersToMap(crimeData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addMarkersToMap(markers: List<MapPOIItem>) {
        clearMarkers()
        markers.forEach { marker ->
            mapView.addPOIItem(marker)
        }
    }

    private fun clearMarkers() {
        mapView.removeAllPOIItems()
    }

    fun initialize() {
        mapView.setZoomLevel(15, true)
        this.kakaoMap = mapView
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
        // 현재 위치 업데이트가 실패했을 때 수행할 작업을 여기에 추가하세요.
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
        // 이벤트 핸들러 구현
    }

    // 마커 클릭 이벤트 처리 메서드 추가
    override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
        poiItem?.let {
            val userObject = it.userObject
            if (userObject is Pair<*, *>) {
                val (cityName, murderRate) = userObject as Pair<String, Double>
                showMarkerInfoDialog(mapView?.context, cityName, murderRate)
            }
        }
    }

    private fun showMarkerInfoDialog(context: Context?, cityName: String, murderRate: Double) {
        context?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("지역 정보")
            builder.setMessage("시도 지역: $cityName\n평균 위험도: $murderRate")
            builder.setPositiveButton("확인", null)
            builder.show()
        }
    }

    override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?) {
        // 기본 마커의 말풍선 클릭 시 호출되는 메서드
    }

    override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?, buttonType: MapPOIItem.CalloutBalloonButtonType?) {
        // 말풍선 클릭 시 호출되는 메서드
    }

    override fun onDraggablePOIItemMoved(mapView: MapView?, poiItem: MapPOIItem?, mapPoint: MapPoint?) {
        // 드래그 가능한 마커를 이동시킬 때 호출되는 메서드
    }
}
