package com.compose.mapsdkcompose.api

import com.compose.mapsdkcompose.models.MapsModelModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    val service: DirectionsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsApiService::class.java)
    }
}

//interface RetrofitService {
//
//    @GET("directions/json")
//    fun getDirections(
//        @Query("origin") origin: String,
//        @Query("destination") destination: String,
//        @Query("key") apiKey: String
//    ): Call<MapsModelModel>
//}