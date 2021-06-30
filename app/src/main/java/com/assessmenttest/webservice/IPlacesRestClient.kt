package com.assessmenttest.webservice

import com.assessmenttest.BuildConfig
import com.assessmenttest.constants.Consts
import com.assessmenttest.models.directionapi.DirectionApiResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

internal interface IPlacesRestClient {

    @GET(Consts.GoogleApiTags.PLACES_DIRECTIONS_EXT_URL)
    fun getSmartRoute(
        @Query(Consts.GoogleApiParams.ORIGIN) origin: String,
        @Query(Consts.GoogleApiParams.DESTINATION) destination: String,
        @Query(Consts.GoogleApiParams.KEY) key: String
    ): Call<DirectionApiResponse>

    companion object {
        val restClient by lazy {
            invoke()
        }

        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        private val client: OkHttpClient = OkHttpClient.Builder().apply {
            connectTimeout(1, TimeUnit.MINUTES)
            readTimeout(1, TimeUnit.MINUTES)
            writeTimeout(1, TimeUnit.MINUTES)
            interceptors().add(loggingInterceptor)
        }.build()


        /**
         * this method builds a retrofit client for Google Places server
         *
         * @return IPlacesRestClient
         */
        private operator fun invoke(): IPlacesRestClient {
            return Retrofit.Builder()
                .client(client)
                .baseUrl(Consts.GoogleApiTags.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IPlacesRestClient::class.java)
        }
    }
}