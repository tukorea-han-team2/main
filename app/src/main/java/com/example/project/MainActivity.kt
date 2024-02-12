package com.example.project


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var gpsUse: Boolean? = null
    private var gpsLat: Double? = null
    private var gpsLng: Double? = null
    private lateinit var mapView: MapView

    private val locationService: LocationServiceExample by lazy {
        LocationServiceExample(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mapView = MapView(this)
        val mapViewContainer: ViewGroup = findViewById(R.id.map_view)
        mapViewContainer.addView(mapView)

        gpsCheck()

        // 위치 권한 체크 및 요청
        checkLocationPermission()

        // 위치 서비스 시작
        locationService.startLocationUpdates()


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

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
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
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showLocation()
            } else {
                Toast.makeText(this, "위치 권한이 거부되어 GPS 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                gpsUse = false
            }
        }
    }

}