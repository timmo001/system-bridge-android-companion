package dev.timmo.systembridge.data.bridge

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface Endpoints {
    @GET("/api/data/system")
    fun getSystem(@Header("api-key") apiKey: String): Call<SystemBridgeSystem>

    @POST("/api/open")
    fun postOpen(@Header("api-key") apiKey: String, @Body data: SystemBridgeOpen): Call<SystemBridgeOpen>
}