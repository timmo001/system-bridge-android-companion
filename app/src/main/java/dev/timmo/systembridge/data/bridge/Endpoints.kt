package dev.timmo.systembridge.data.bridge

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface Endpoints {
    @GET("/information")
    fun getInformation(@Header("api-key") apiKey: String): Call<Information>

    @POST("/open")
    fun postOpen(@Header("api-key") apiKey: String, @Body data: Open): Call<Open>
}