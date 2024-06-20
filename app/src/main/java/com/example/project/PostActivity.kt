package com.example.project

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class PostActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var selectedImageUri: Uri
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var userId: String
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val idEditText: EditText = findViewById(R.id.id)
        val descriptionEditText: EditText = findViewById(R.id.description)
        val latitudeEditText: EditText = findViewById(R.id.latitude)
        val longitudeEditText: EditText = findViewById(R.id.longitude)
        //val categoryEditText: EditText = findViewById(R.id.category)
        val categorySpinner: Spinner = findViewById(R.id.category)
        imageView = findViewById(R.id.imageView)
        val selectImageButton: Button = findViewById(R.id.selectImage)
        val uploadButton: Button = findViewById(R.id.upload)

        // User ID 가져오기
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userId = currentUser?.uid ?: ""
        idEditText.setText(userId)

        // 위치 정보 가져오기
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation(latitudeEditText, longitudeEditText)

        // 카테고리 Spinner 설정
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // 이미지 선택 버튼 클릭 리스너
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, 1)
        }


        // 업로드 버튼 클릭 리스너
        uploadButton.setOnClickListener {
            val id = idEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val latitude = latitudeEditText.text.toString()
            val longitude = longitudeEditText.text.toString()
            val category = categorySpinner.selectedItem.toString()

            if (::selectedImageUri.isInitialized) {
                uploadPost(id, description, latitude, longitude, category)
            } else {
                Toast.makeText(this, "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(latitudeEditText: EditText, longitudeEditText: EditText) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    latitudeEditText.setText(latitude.toString())
                    longitudeEditText.setText(longitude.toString())
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            imageView.setImageURI(selectedImageUri)
        }
    }

    private fun uploadPost(id: String, description: String, latitude: String, longitude: String, category: String) {
        val idPart = RequestBody.create("text/plain".toMediaTypeOrNull(), id)
        val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val latitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), latitude)
        val longitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), longitude)
        val categoryPart = RequestBody.create("text/plain".toMediaTypeOrNull(), category)

        val filePath = getFilePathFromUri(this, selectedImageUri)
        if (filePath == null) {
            Toast.makeText(this, "Failed to get image path", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(filePath)
        val requestFile = RequestBody.create(contentResolver.getType(selectedImageUri)!!.toMediaTypeOrNull(), file)
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val call = RetrofitClient.apiService.uploadPostWithImage(idPart, descriptionPart, latitudePart, longitudePart, categoryPart, imagePart)
        call.enqueue(object : Callback<PostWithImage> {
            override fun onResponse(call: Call<PostWithImage>, response: Response<PostWithImage>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@PostActivity, "Post with image uploaded successfully", Toast.LENGTH_SHORT).show()

                    // MainActivity로 이동하여 위치 정보 전달
                    val intent = Intent(this@PostActivity, MainActivity::class.java).apply {
                        putExtra("latitude", latitude.toDouble())
                        putExtra("longitude", longitude.toDouble())
                        putExtra("description", description)
                        putExtra("category", category)
                        putExtra("imageUrl", selectedImageUri.toString())  // 이미지 URL 전달
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this@PostActivity, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostWithImage>, t: Throwable) {
                Toast.makeText(this@PostActivity, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFilePathFromUri(uri1: PostActivity, uri: Uri): String? {
        var filePath: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            filePath = it.getString(columnIndex)
        }
        return filePath
    }


    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101 // 추가
    }

}