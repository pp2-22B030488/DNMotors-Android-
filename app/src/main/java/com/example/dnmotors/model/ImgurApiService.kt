package com.example.dnmotors.model

import com.example.domain.model.ImgurResponse
import retrofit2.Call
import retrofit2.http.*

interface ImgurApiService {
    @Headers("Authorization: Client-ID c367332f0584602")
    @POST("image")
    @FormUrlEncoded
    fun uploadImage(
        @Field("image") image: String
    ): Call<ImgurResponse>
}
