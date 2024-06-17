package com.example.project

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("senddata/create/")
    fun uploadPostWithImage(
        @Part("id") id: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("category") category: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<PostWithImage>

    @FormUrlEncoded
    @POST("senddata/create/")  // 수정된 엔드포인트
    fun uploadPostWithoutImage(
        @Field("id") id: String,
        @Field("description") description: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("category") category: String
    ): Call<PostWithoutImage>

    // 게시글 목록 가져오기
    @GET("senddata/post/")
    fun getPosts(): Call<List<Post>>

}