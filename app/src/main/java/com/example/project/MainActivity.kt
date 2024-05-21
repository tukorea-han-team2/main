package com.example.project

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import net.daum.mf.map.api.MapView

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var locationService: LocationServiceExample
    private lateinit var crime: Crime
    private lateinit var fetchDataFromServerTask: FetchDataFromServerTask
    private lateinit var mapController: MapController
    private lateinit var mapControllerAccident: MapControllerAccident
    private lateinit var alarmSet: AlarmSet
    private var gpsUse: Boolean? = null
    private val locationRequest: LocationRequest = LocationRequest.create()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // FetchDataFromServerTask 초기화
        // fetchDataFromServerTask = FetchDataFromServerTask(this, mapView)

        // 위치 서비스 초기화
        locationService = LocationServiceExample(this)

        alarmSet = AlarmSet(this, locationService)

        // 위치 서비스 시작
        locationService.startLocationUpdates()

        // 위치 권한 체크 및 요청
        checkLocationPermission()


        // 서버로부터 데이터 가져오기
        // fetchDataFromServerTask.execute()

        // MapView 초기화
        mapView = MapView(this)
        val mapViewContainer: ViewGroup = findViewById(R.id.map_view)
        mapViewContainer.addView(mapView)

        // GPS 체크
        gpsCheck()

        val crimeButton: Button = findViewById(R.id.crime_button)
        val accidentButton: Button = findViewById(R.id.accident_button)
        val zoomInButton: ImageButton = findViewById(R.id.zoomInButton)
        val zoomOutButton: ImageButton = findViewById(R.id.zoomOutButton)


        crimeButton.setOnClickListener {
            stopLocationUpdates()

            showCrimeMarkersAndPolygons()
        }

        accidentButton.setOnClickListener {
            clearMarkers()
            // MapControllerAccident 클래스 초기화
            mapControllerAccident = MapControllerAccident(this)

            // 현재 위치를 가져와서 해당 위치의 도로 정보를 가져옵니다.
            fetchCurrentLocationForAccident()

        }


        zoomInButton.setOnClickListener{
            onZoomInButtonClick(mapView)
        }

        zoomOutButton.setOnClickListener{
            onZoomOutButtonClick(mapView)
        }

        // 추가된 부분: Alarm 버튼 클릭 이벤트 처리
        val alarmButton: ImageButton = findViewById(R.id.alarmButton)
        alarmButton.setOnClickListener {
            val intent = Intent(this, AlarmSetActivity::class.java)
            startActivity(intent)
        }

        // 위치 업데이트 주기 설정
        locationRequest.interval = 20000 // 20초마다 업데이트
        locationRequest.fastestInterval = 10000 // 최소 10초 간격으로 업데이트
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    fun onZoomInButtonClick(mapView: MapView) {
        // 맵의 줌 레벨을 확대합니다.
        val currentZoomLevel = mapView.zoomLevel
        mapView.setZoomLevel(currentZoomLevel + 1, true)
    }

    fun onZoomOutButtonClick(mapView: MapView) {
        // 맵의 줌 레벨을 축소합니다.
        val currentZoomLevel = mapView.zoomLevel
        mapView.setZoomLevel(currentZoomLevel - 1, true)
    }



    private fun showCrimeMarkersAndPolygons(){
        val mapDataFetcher = MapDataFetcher(this)
        val crimeDataFetcher = CrimeDataFetcher(this)
        mapController = MapController(mapView, crimeDataFetcher)
        mapController.initialize()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location: Location? = locationResult.lastLocation
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                // 위치 서비스 내부의 위치 업데이트 처리 함수 호출
                locationService.handleLocationUpdate(location)

                // 사고 정보 업데이트 요청
                mapControllerAccident.getRoadInformation(latitude, longitude)
            }
        }
    }
    private fun fetchCurrentLocationForAccident() {

        // 위치 권한이 있는지 확인
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // 위치 업데이트 요청
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun clearPolygons() {
        mapView.removeAllPolylines() // 기존 다각형 삭제
    }

    private fun clearMarkers() {
        mapView.removeAllPOIItems()
    }

    private fun checkLocationPermission() {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun gpsCheck() {
        if (gpsUse == null) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                showLocation()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }

    private fun showLocation() {
        if (gpsUse == null) {
            gpsUse = true
            val options = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            mapView.currentLocationTrackingMode = options
            mapView.setShowCurrentLocationMarker(true)
            mapView.currentLocationTrackingMode = options
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용되면 현재 위치 가져오기
                fetchCurrentLocationForAccident()
            } else {
                // 위치 권한이 거부되면 메시지 표시
                Toast.makeText(this, "위치 권한이 거부되어 현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }



    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}