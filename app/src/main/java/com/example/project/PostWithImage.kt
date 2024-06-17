package com.example.project

import com.google.gson.annotations.SerializedName


data class PostWithImage(
    @SerializedName("id") val id: String,
    @SerializedName("description") val description: String,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("category") val category: String,
    @SerializedName("timestamp") val timestamp: String
)