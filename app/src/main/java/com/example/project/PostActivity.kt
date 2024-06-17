package com.example.project

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val idEditText: EditText = findViewById(R.id.id)
        val descriptionEditText: EditText = findViewById(R.id.description)
        val latitudeEditText: EditText = findViewById(R.id.latitude)
        val longitudeEditText: EditText = findViewById(R.id.longitude)
        val categoryEditText: EditText = findViewById(R.id.category)
        imageView = findViewById(R.id.imageView)
        val selectImageButton: Button = findViewById(R.id.selectImage)
        val uploadButton: Button = findViewById(R.id.upload)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, 1)
        }

        uploadButton.setOnClickListener {
            val id = idEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val latitude = latitudeEditText.text.toString()
            val longitude = longitudeEditText.text.toString()
            val category = categoryEditText.text.toString()

            if (::selectedImageUri.isInitialized) {
                uploadPostWithImage(id, description, latitude, longitude, category)
            } else {
                uploadPostWithoutImage(id, description, latitude, longitude, category)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            imageView.setImageURI(selectedImageUri)
        }
    }

    private fun uploadPostWithImage(id: String, description: String, latitude: String, longitude: String, category: String) {
        val idPart = RequestBody.create("text/plain".toMediaTypeOrNull(), id)
        val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val latitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), latitude)
        val longitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), longitude)
        val categoryPart = RequestBody.create("text/plain".toMediaTypeOrNull(), category)

        // Convert content URI to file path
        val filePath = getFilePathFromUri(this, selectedImageUri)
        if (filePath == null) {
            Toast.makeText(this, "Failed to get image path", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(filePath)
        val requestFile = RequestBody.create(contentResolver.getType(selectedImageUri)!!.toMediaTypeOrNull(), file)
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val call = RetrofitClient.instance.uploadPostWithImage(idPart, descriptionPart, latitudePart, longitudePart, categoryPart, imagePart)
        call.enqueue(object : Callback<PostWithImage> {
            override fun onResponse(call: Call<PostWithImage>, response: Response<PostWithImage>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@PostActivity, "Post with image uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PostActivity, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostWithImage>, t: Throwable) {
                Toast.makeText(this@PostActivity, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Function to get file path from URI
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

    private fun uploadPostWithoutImage(id: String, description: String, latitude: String, longitude: String, category: String) {
        val call = RetrofitClient.instance.uploadPostWithoutImage(id, description, latitude, longitude, category)
        call.enqueue(object : Callback<PostWithoutImage> {
            override fun onResponse(call: Call<PostWithoutImage>, response: Response<PostWithoutImage>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@PostActivity, "Post without image uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PostActivity, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostWithoutImage>, t: Throwable) {
                Toast.makeText(this@PostActivity, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}