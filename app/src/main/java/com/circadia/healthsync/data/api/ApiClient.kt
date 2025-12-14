package com.circadia.healthsync.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton API client for network requests.
 * Default base URL is configured for Android Emulator (10.0.2.2:4000).
 */
object ApiClient {

    // Default URL for Android Emulator - points to host machine's localhost:4000
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:4000/"

    // Configurable base URL - change this for physical device testing
    var baseUrl: String = DEFAULT_BASE_URL
        private set

    private var retrofit: Retrofit? = null
    private var api: CircadiaApi? = null

    /**
     * Get the Retrofit instance, creating it if necessary.
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null || retrofit?.baseUrl()?.toString() != baseUrl) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    /**
     * Get the API interface instance.
     */
    fun getApi(): CircadiaApi {
        if (api == null || retrofit?.baseUrl()?.toString() != baseUrl) {
            api = getRetrofit().create(CircadiaApi::class.java)
        }
        return api!!
    }

    /**
     * Update the base URL (e.g., for physical device testing).
     * Call this before making any API requests.
     */
    fun setBaseUrl(url: String) {
        baseUrl = if (url.endsWith("/")) url else "$url/"
        // Reset instances to force recreation with new URL
        retrofit = null
        api = null
    }
}

