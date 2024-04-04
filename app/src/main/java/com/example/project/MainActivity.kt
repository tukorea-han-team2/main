package com.example.project

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import net.daum.mf.map.api.MapView

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationUpdateListener: LocationUpdateListener
    private lateinit var mapView: MapView
    private lateinit var locationService: LocationServiceExample
    private lateinit var crime: Crime
    private lateinit var fetchDataFromServerTask: FetchDataFromServerTask
    private lateinit var mapController: MapController
    private var gpsUse: Boolean? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val alarmImageView: ImageView = findViewById(R.id.imageButton1)

        alarmImageView.setOnClickListener {
            // AlarmSetActivity로 이동하는 Intent 생성
            val intent = Intent(this@MainActivity, Alarmset::class.java)
            // 액티비티 시작
            startActivity(intent)
        }

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // FetchDataFromServerTask 초기화
        // fetchDataFromServerTask = FetchDataFromServerTask(this, mapView)

        // Murder 클래스 초기화
        crime = Crime()

        // 위치 서비스 초기화
        locationService = LocationServiceExample(this)

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
        // 현재 위치 가져오기
        fetchCurrentLocation()

        val mapDataFetcher = MapDataFetcher(this)
        val crimeDataFetcher = CrimeDataFetcher()
        mapController = MapController(mapView, mapDataFetcher, crimeDataFetcher)
        mapController.initialize()
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


    private fun fetchCurrentLocation() {
        // 위치 권한이 있는지 확인
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // 현재 위치 가져오기
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // 위치를 성공적으로 가져왔을 때 실행되는 콜백
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude

                    // 위치 서비스 내부의 위치 업데이트 처리 함수 호출
                    locationService.handleLocationUpdate(location)
                }
            }
            .addOnFailureListener { e ->
                // 위치를 가져오지 못했을 때 실행되는 콜백
                e.printStackTrace()
            }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용되면 현재 위치 가져오기
                fetchCurrentLocation()
            } else {
                // 위치 권한이 거부되면 메시지 표시
                Toast.makeText(this, "위치 권한이 거부되어 현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
