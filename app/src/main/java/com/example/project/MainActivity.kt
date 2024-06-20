package com.example.project

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(),  MapView.POIItemEventListener{
    private lateinit var locationServiceExample: LocationServiceExample
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var mapController: MapController
    private lateinit var mapControllerAccident: MapControllerAccident
    private lateinit var alarmSet: AlarmSet
    private var gpsUse: Boolean? = null
    private val locationRequest: LocationRequest = LocationRequest.create()
    private var selectedLevel: Int = 4
    private var markers = mutableListOf<MapPOIItem>()
    private lateinit var apiService: ApiService
    private lateinit var posts: List<Post>


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiService = RetrofitClient.apiService

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        selectedLevel = sharedPreferences.getInt("selectedLevel", 4)

        val selectedLevelFromAlarmSet = intent.getIntExtra("SELECTED_LEVEL", -1)
        if (selectedLevelFromAlarmSet != -1) {
            selectedLevel = selectedLevelFromAlarmSet
            sharedPreferences.edit().putInt("selectedLevel", selectedLevel).apply()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationServiceExample = LocationServiceExample(this)
        locationServiceExample.setSelectedLevel(selectedLevel)
        alarmSet = AlarmSet(this, locationServiceExample)

        locationServiceExample.startLocationUpdates()
        checkLocationPermission()

        mapView = MapView(this)
        val mapViewContainer: ViewGroup = findViewById(R.id.map_view)
        mapViewContainer.addView(mapView)

        mapView.setPOIItemEventListener(this)

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
            mapControllerAccident = MapControllerAccident(this)
            fetchCurrentLocationForAccident()
        }

        zoomInButton.setOnClickListener {
            onZoomInButtonClick(mapView)
        }

        zoomOutButton.setOnClickListener {
            onZoomOutButtonClick(mapView)
        }

        val alarmButton: ImageButton = findViewById(R.id.alarmButton)
        alarmButton.setOnClickListener {
            val intent = Intent(this, AlarmSetActivity::class.java)
            startActivity(intent)
        }

        val btnOpenPostActivity: Button = findViewById(R.id.btnOpenPostActivity)
        btnOpenPostActivity.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }

        locationRequest.interval = 20000
        locationRequest.fastestInterval = 10000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fetchPosts()
    }

    private fun fetchPosts() {
        apiService.getPosts().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    posts = response.body() ?: emptyList()
                    addMarkers()
                } else {
                    Toast.makeText(this@MainActivity, "게시글을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMarkers() {
        for ((index, post) in posts.withIndex()) {
            val marker = MapPOIItem()
            marker.itemName = post.category
            marker.tag = index
            marker.mapPoint = MapPoint.mapPointWithGeoCoord(post.latitude, post.longitude)
            marker.markerType = MapPOIItem.MarkerType.BluePin
            //marker.isShowCalloutBalloonOnTouch = true // 말풍선 터치 이벤트 활성화
            mapView.addPOIItem(marker)
        }
    }

    override fun onPOIItemSelected(mapView: MapView?, marker: MapPOIItem?) {
        marker?.let {
            val postId = marker.tag as? Int
            postId?.let {
                val post = posts.getOrNull(postId)
                post?.let {
                    Log.d("MainActivity", "Selected post: ${post.description}, ${post.category}")
                    showPostDetails(post)
                } ?: Log.e("MainActivity", "post is null for postId $postId")
            } ?: Log.e("MainActivity", "postId is null")
        } ?: Log.e("MainActivity", "marker is null")
    }

    override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, marker: MapPOIItem?) {
        // 말풍선 클릭 시 (Deprecated)

    }

    override fun onCalloutBalloonOfPOIItemTouched(
        mapView: MapView?,
        marker: MapPOIItem?,
        aa: MapPOIItem.CalloutBalloonButtonType?
    ) {

    }


    override fun onDraggablePOIItemMoved(mapView: MapView?, poiItem: MapPOIItem?, mapPoint: MapPoint?) {
        // 마커의 속성 중 isDraggable = true 일 때 마커를 이동시켰을 경우
    }

    private fun showPostDetails(post: Post) {
        val intent = Intent(this@MainActivity, PostDetailsActivity::class.java).apply {
            putExtra("description", post.description)
            putExtra("category", post.category)
            putExtra("image", post.imageUrl)
        }
        startActivity(intent)
    }

    fun onZoomInButtonClick(mapView: MapView) {
        val currentZoomLevel = mapView.zoomLevel
        mapView.setZoomLevel(currentZoomLevel + 1, true)
    }

    fun onZoomOutButtonClick(mapView: MapView) {
        val currentZoomLevel = mapView.zoomLevel
        mapView.setZoomLevel(currentZoomLevel - 1, true)
    }

    private fun showCrimeMarkersAndPolygons() {

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
                mapControllerAccident.getRoadInformation(latitude, longitude)
            }
        }
    }

    private fun fetchCurrentLocationForAccident() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun clearMarkers() {
        mapView.removeAllPOIItems()
    }

    private fun checkLocationPermission() {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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
                fetchCurrentLocationForAccident()
            } else {
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
