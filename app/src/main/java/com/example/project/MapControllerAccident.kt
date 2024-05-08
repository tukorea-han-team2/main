package com.example.project


import android.graphics.Point
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import net.daum.mf.map.api.*
import net.daum.mf.map.api.MapView.CurrentLocationEventListener
import net.daum.mf.map.api.MapView.MapViewEventListener

class MapControllerAccident(
    private val mapView: MapView,
    private val accidentDataFetcher: AccidentDataFetcher
) : CurrentLocationEventListener, MapViewEventListener {

    private lateinit var kakaoMap: MapView

    private var currentZoomLevel: Int = 0
    private var job: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main)

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
    }

    override fun onMapViewZoomLevelChanged(mapView: MapView?, level: Int) {
        currentZoomLevel = level
        loadMapDataAccordingToZoomLevel(currentZoomLevel)
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
    }

    override fun onCurrentLocationUpdate(
        mapView: MapView?,
        currentLocation: MapPoint?,
        accuracyInMeters: Float
    ) {
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView, p1: Float) {
        // Empty implementation
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
    }


    private fun loadMapDataAccordingToZoomLevel(currentZoomLevel: Int) {
        // 취소 요청을 핸들링하기 위해 기존 작업을 취소
        job?.cancel()

        job = scope.launch {
            try {

                val accidentDataDataDeferred = async(Dispatchers.IO) {
                    accidentDataFetcher.fetchAccidentData()
                }

                val accidentData = accidentDataDataDeferred.await() ?: return@launch


                clearMarkers()
                drawPolygons(accidentData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




    private fun drawPolygons(polygons: List<List<Pair<Double, Double>>>) {
        clearPolygons()

        // 새로운 다각형 그리기
        polygons.forEach { points ->
            val polyline = MapPolyline()
            points.forEach { (latitude, longitude) ->
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude))
            }
            mapView.addPolyline(polyline) // 지도에 다각형 추가
        }
    }

    private fun clearPolygons() {
        mapView.removeAllPolylines() // 기존 다각형 삭제
    }

    private fun clearMarkers() {
        // 지도에 추가된 모든 마커들을 지웁니다.
        mapView.removeAllPOIItems()
    }

    private fun addMarkersToMap(markers: List<MapPOIItem>) {
        markers.forEach { marker ->
            mapView.addPOIItem(marker)
        }
    }

    fun initialize() {
        // 초기화 작업 수행
        mapView.setZoomLevel(3, true)
    }
}