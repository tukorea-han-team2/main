package com.example.project

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Vibrator
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import net.daum.mf.map.api.MapView

class MainActivity : AppCompatActivity(){
    private lateinit var mapView: MapView
    private lateinit var vibrator: Vibrator
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationService: LocationServiceExample
    private var gpsUse: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = MapView(this)
        val mapViewContainer: ViewGroup = findViewById(R.id.map_view)
        mapViewContainer.addView(mapView)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 위치 서비스 초기화
        locationService = LocationServiceExample(this)

        // 위치 권한 체크 및 요청
        checkLocationPermission()

        // GPS 체크
        gpsCheck()

        // 위치 서비스 시작
        locationService.startLocationUpdates()

        // 서버로부터 데이터 받아오기
        FetchDataFromServerTask(this, mapView).execute()
    }

    override fun onDestroy() {
        // 위치 서비스 중지
        locationService.stopLocationUpdates()
        super.onDestroy()
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

    fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationService.startLocationUpdates()
            } else {
                Toast.makeText(this, "위치 권한이 거부되어 GPS 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                gpsUse = false
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
