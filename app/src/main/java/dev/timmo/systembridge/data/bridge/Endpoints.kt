package dev.timmo.systembridge.data.bridge

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Query

interface Endpoints {
    @GET("/information")
    fun getInformation(
        @Header("api-key") apiKey: String,
    ): Call<Information>

    @POST("/open")
    fun postOpen(
        @Header("api-key") apiKey: String,
        @Body data: Open,
    ): Call<Open>

    @Multipart
    @POST("/filesystem/files/file")
    fun postFile(
        @Header("api-key") apiKey: String,
        @Query("path") path: String,
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
    ): Call<Any>
}