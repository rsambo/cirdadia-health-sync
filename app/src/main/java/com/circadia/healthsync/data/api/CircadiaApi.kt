package com.circadia.healthsync.data.api

import com.circadia.healthsync.data.model.SyncRequest
import com.circadia.healthsync.data.model.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for Circadia backend endpoints.
 */
interface CircadiaApi {

    /**
     * Sync health data to the backend.
     * POST /api/sync/health-data/test (unauthenticated test endpoint)
     */
    @POST("api/sync/health-data/test")
    suspend fun syncHealthData(@Body request: SyncRequest): Response<SyncResponse>
}
