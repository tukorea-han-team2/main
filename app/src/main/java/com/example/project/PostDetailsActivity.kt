package com.example.project

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PostDetailsActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_post_info)

        val description = intent.getStringExtra("description") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val imageUrl = intent.getStringExtra("image")

        val tvDescription: TextView = findViewById(R.id.tvDescription)
        val tvCategory: TextView = findViewById(R.id.tvCategory)
        val imageView: ImageView = findViewById(R.id.imageView)

        tvDescription.text = "내용: $description"
        tvCategory.text = "카테고리: $category"

        // 이미지 로드
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(imageView)
        }
    }
}


