package com.example.project


import android.util.Log
import kotlinx.coroutines.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import net.daum.mf.map.api.MapView.CurrentLocationEventListener
import net.daum.mf.map.api.MapView.MapViewEventListener

class MapController(
    private val mapView: MapView,
    private val crimeDataFetcher: CrimeDataFetcher
) : CurrentLocationEventListener, MapViewEventListener {

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
                if (currentLocation != null) {
                    val crimeData = crimeDataFetcher.fetchCrimeData( centerLatitude, centerLongitude, 5.0)
                    addMarkersToMap(crimeData)
                }
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
        mapView.setZoomLevel(3, true)
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
        // 현재 위치 업데이트가 실패했을 때 수행할 작업을 여기에 추가하세요.
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
        // 이벤트 핸들러 구현
    }
}
