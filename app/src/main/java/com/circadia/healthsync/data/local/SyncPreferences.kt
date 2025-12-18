package com.circadia.healthsync.data.local

import android.content.Context
import android.content.SharedPreferences
import com.circadia.healthsync.data.model.CachedSyncData
import com.google.gson.Gson
import java.time.Instant

/**
 * SharedPreferences wrapper for storing sync-related data.
 * Handles caching of sync timestamps and step data.
 */
class SyncPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "sync_preferences"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        private const val KEY_CACHED_SYNC_DATA = "cached_sync_data"
        private const val KEY_CHANGES_TOKEN = "changes_token"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Save the last sync timestamp in ISO 8601 format.
     * @param timestamp The instant of the last successful sync
     */
    fun saveLastSyncTimestamp(timestamp: Instant) {
        prefs.edit()
            .putString(KEY_LAST_SYNC_TIMESTAMP, timestamp.toString())
            .apply()
    }

    /**
     * Get the last sync timestamp.
     * @return The instant of the last successful sync, or null if never synced
     */
    fun getLastSyncTimestamp(): Instant? {
        val timestampString = prefs.getString(KEY_LAST_SYNC_TIMESTAMP, null)
        return timestampString?.let {
            try {
                Instant.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Save cached step data as JSON.
     * @param data The cached sync data to save
     */
    fun saveCachedSyncData(data: CachedSyncData) {
        val json = gson.toJson(data)
        prefs.edit()
            .putString(KEY_CACHED_SYNC_DATA, json)
            .apply()
    }

    /**
     * Get cached step data.
     * @return The cached sync data, or null if no data is cached
     */
    fun getCachedSyncData(): CachedSyncData? {
        val json = prefs.getString(KEY_CACHED_SYNC_DATA, null)
        return json?.let {
            try {
                gson.fromJson(it, CachedSyncData::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Save the Health Connect changes token.
     * @param token The changes token from Health Connect
     */
    fun saveChangesToken(token: String) {
        prefs.edit()
            .putString(KEY_CHANGES_TOKEN, token)
            .apply()
    }

    /**
     * Get the stored Health Connect changes token.
     * @return The changes token, or null if not stored
     */
    fun getChangesToken(): String? {
        return prefs.getString(KEY_CHANGES_TOKEN, null)
    }

    /**
     * Clear the changes token.
     * Used when token expires or on fallback to full sync.
     */
    fun clearChangesToken() {
        prefs.edit()
            .remove(KEY_CHANGES_TOKEN)
            .apply()
    }

    /**
     * Clear all cached data.
     * Useful for testing or logout scenarios.
     */
    fun clearAll() {
        prefs.edit()
            .remove(KEY_LAST_SYNC_TIMESTAMP)
            .remove(KEY_CACHED_SYNC_DATA)
            .remove(KEY_CHANGES_TOKEN)
            .apply()
    }
}

