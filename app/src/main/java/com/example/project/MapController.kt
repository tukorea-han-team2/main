package com.example.project


import android.graphics.Point
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import net.daum.mf.map.api.*
import net.daum.mf.map.api.MapView.CurrentLocationEventListener
import net.daum.mf.map.api.MapView.MapViewEventListener

class MapController(
    private val mapView: MapView,
    private val mapDataFetcher: MapDataFetcher,
    private val crimeDataFetcher: CrimeDataFetcher
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

    private var isSidoDataFetched = false // 시도 행정구역 데이터를 이미 가져왔는지 여부를 저장하는 변수
    private var isSigDataFetched = false // 시도 행정구역 데이터를 이미 가져왔는지 여부를 저장하는 변수
    private var isCrime5DataFetched = false // 5단계 마커 데이터를 이미 가져왔는지 여부를 저장하는 변수
    private var isCrime4DataFetched = true // 4단계 마커 데이터를 이미 가져왔는지 여부를 저장하는 변수
    private var isCrime3DataFetched = false // 4단계 마커 데이터를 이미 가져왔는지 여부를 저장하는 변수
    private var isCrime31DataFetched = false // 4단계 마커 데이터를 이미 가져왔는지 여부를 저장하는 변수

    private fun loadMapDataAccordingToZoomLevel(currentZoomLevel: Int) {
        // 취소 요청을 핸들링하기 위해 기존 작업을 취소
        job?.cancel()

        job = scope.launch {
            try {
                if (currentZoomLevel >= 8) {
                    if (!isSidoDataFetched && !isCrime5DataFetched) {
                        val sidoDataDeferred = async(Dispatchers.IO) {
                            mapDataFetcher.fetchSidoData()
                        }
                        val crimeDataDeferred5 = async(Dispatchers.IO) {
                            crimeDataFetcher.fetchCrime5Data()
                        }

                        val sidoData = sidoDataDeferred.await() ?: return@launch
                        val crimeData5 = crimeDataDeferred5.await() ?: return@launch

                        drawPolygons(sidoData)
                        clearMarkers()
                        addMarkersToMap(crimeData5)

                        isSidoDataFetched = true
                        isCrime4DataFetched = true
                        isCrime5DataFetched = true
                        isCrime3DataFetched = false
                        isCrime31DataFetched = false
                    }
                } else if (currentZoomLevel in 5..7) {
                    if (isSidoDataFetched && isCrime4DataFetched && !isCrime3DataFetched && !isSigDataFetched) {
                        val sigDataDeferred = async(Dispatchers.IO) {
                            mapDataFetcher.fetchSigData()
                        }
                        val crimeDataDeferred5 = async(Dispatchers.IO) {
                            crimeDataFetcher.fetchCrime5Data()
                        }
                        val crimeDataDeferred4 = async(Dispatchers.IO) {
                            crimeDataFetcher.fetchCrime4Data()
                        }

                        val sigData = sigDataDeferred.await() ?: return@launch
                        val crimeData5 = crimeDataDeferred5.await() ?: return@launch
                        val crimeData4 = crimeDataDeferred4.await() ?: return@launch

                        drawPolygons(sigData)
                        clearMarkers()
                        addMarkersToMap(crimeData5)
                        addMarkersToMap(crimeData4)

                        isSidoDataFetched = false
                        isSigDataFetched = true
                        isCrime4DataFetched = false
                        isCrime5DataFetched = false
                        isCrime3DataFetched = true
                        isCrime31DataFetched = false
                    }
                } else {
                    if (!isCrime31DataFetched) {
                        val crimeDataDeferred3 = async(Dispatchers.IO) {
                            crimeDataFetcher.fetchCrime3Data()
                        }
                        val crimeDataDeferred2 = async(Dispatchers.IO) {
                            crimeDataFetcher.fetchCrime2Data()
                        }
                        val crimeDataDeferred1 = async(Dispatchers.IO) {
                            crimeDataFetcher.fetchCrime1Data()
                        }

                        val crimeData3 = crimeDataDeferred3.await() ?: return@launch
                        val crimeData2 = crimeDataDeferred2.await() ?: return@launch
                        val crimeData1 = crimeDataDeferred1.await() ?: return@launch

                        addMarkersToMap(crimeData3)
                        addMarkersToMap(crimeData2)
                        addMarkersToMap(crimeData1)

                        isSigDataFetched = false
                        isCrime4DataFetched = true
                        isCrime5DataFetched = false
                        isCrime3DataFetched = false
                        isCrime31DataFetched = true
                    }
                }
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
        mapView.setZoomLevel(11, true)
    }
}

