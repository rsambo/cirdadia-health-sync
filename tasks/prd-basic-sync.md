# PRD: Circadia Android App - Health Connect Sync MVP

## 1. Introduction/Overview

The Circadia Android app is a companion mobile application that reads health data from Google Health Connect and syncs it to the Circadia web application. This MVP establishes the basic data pipeline from the user's Android device to the web backend.

**Goal:** Demonstrate a working end-to-end sync flow - read steps data from Health Connect, send it to the web backend, and display confirmation of successful sync.

## 2. Goals

1. **Read Health Connect data** - Access the user's step count data from Google Health Connect
2. **Send data to web backend** - POST the data to the Circadia web API
3. **Confirm successful sync** - Display server response to verify the pipeline works
4. **Minimal viable implementation** - Simplest possible app to prove the concept

## 3. User Stories

### US-1: Grant Health Connect Permission
**As a** user  
**I want to** grant the app permission to read my Health Connect data  
**So that** the app can access my step count information

### US-2: Sync Health Data
**As a** user  
**I want to** tap a "Sync" button to send my health data to the web app  
**So that** I can view my data on the web dashboard

### US-3: See Sync Result
**As a** user  
**I want to** see confirmation that my sync succeeded (or failed)  
**So that** I know if my data was transmitted successfully

## 4. Functional Requirements

### 4.1 Health Connect Integration

1. The app must request permission to read Steps data from Health Connect
2. The app must handle the case where Health Connect is not installed (prompt user to install)
3. The app must read step count records for the last 7 days
4. The app must handle permission denied gracefully with a clear message

### 4.2 User Interface

5. The app must have a single screen with:
    - App title/branding ("Circadia")
    - A prominent "Sync" button
    - A status/result area showing sync outcome
6. The Sync button must be disabled while a sync is in progress
7. The app must show a loading indicator during sync
8. On success, display:
    - "Sync successful!" message
    - Number of records sent (from server response)
    - Timestamp of sync (from server response)
9. On failure, display:
    - "Sync failed" message
    - Error description (e.g., "Network error", "Server error")

### 4.3 Data Sync

10. The app must POST data to: `POST /api/sync/health-data/test` (unauthenticated test endpoint)
11. The request body must be JSON with this structure:
    ```json
    {
      "records": [
        {
          "type": "steps",
          "date": "2025-12-14",
          "count": 8500
        }
      ]
    }
    ```
12. The app must handle network errors (no connection, timeout)
13. The app must handle server errors (4xx, 5xx responses)

### 4.4 Configuration

14. The backend URL must be configurable (for switching between localhost and deployed server)
15. Default URL: `http://10.0.2.2:4000` (Android emulator localhost alias)

## 5. Non-Goals (Out of Scope)

- **User authentication** - No Google Sign-In or login flow for MVP
- **Sleep data** - Only steps for MVP (sleep will be added later)
- **Persistent storage** - No local database or sync history
- **Background sync** - Manual sync only, no WorkManager/scheduled syncs
- **Multiple data types** - Steps only
- **Data visualization** - No charts or graphs in the Android app
- **Offline queue** - No queuing of failed syncs for retry
- **Settings screen** - No user preferences

## 6. Design Considerations

### UI Layout (Single Screen)

```
┌─────────────────────────────┐
│         Circadia            │  <- App title
│                             │
│     ┌─────────────────┐     │
│     │                 │     │
│     │   [Sync Icon]   │     │
│     │                 │     │
│     │   SYNC DATA     │     │  <- Large button
│     │                 │     │
│     └─────────────────┘     │
│                             │
│  ┌───────────────────────┐  │
│  │ Status:               │  │
│  │ Ready to sync         │  │  <- Status area
│  │                       │  │
│  │ Last sync: Never      │  │
│  └───────────────────────┘  │
│                             │
└─────────────────────────────┘
```

### Status States

| State | Display |
|-------|---------|
| Initial | "Ready to sync" |
| Syncing | "Syncing..." + spinner |
| Success | "✓ Sync successful! Sent X records at [time]" |
| Error | "✗ Sync failed: [error message]" |
| No Permission | "Health Connect permission required" |
| No Health Connect | "Please install Health Connect" |

## 7. Technical Considerations

### Platform Requirements

- **Minimum SDK:** API 26 (Android 8.0) - Health Connect requirement
- **Target SDK:** API 34 (Android 14) or latest stable
- **Language:** Kotlin
- **Build:** Gradle with Kotlin DSL

### Dependencies

```kotlin
// Health Connect
implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

// Networking (choose one)
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// OR
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// Coroutines for async operations
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

### Health Connect Permissions

Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.health.READ_STEPS" />

<queries>
    <package android:name="com.google.android.apps.healthdata" />
</queries>
```

### Backend Test Endpoint

**Note:** The web backend needs a new unauthenticated test endpoint for MVP:
- `POST /api/sync/health-data/test` - Same as authenticated endpoint but without auth middleware
- This should be disabled/removed before production

### Network Configuration

For local development with Android Emulator:
- Use `10.0.2.2` instead of `localhost` (emulator's host loopback)
- Add network security config to allow cleartext HTTP for development

## 8. Success Metrics

1. **Health Connect reads successfully** - App can retrieve step data
2. **Network request completes** - Data reaches the backend
3. **Server responds correctly** - Receives 200 OK with expected JSON
4. **UI updates appropriately** - User sees success message with record count
5. **End-to-end verification** - Data appears in web app's Health Data page

## 9. Open Questions

1. **Health Connect availability** - Should we provide a fallback for devices without Health Connect, or just require it?
2. **Date range** - Is 7 days of step data appropriate for MVP, or should it be configurable?
3. **Emulator testing** - Does Health Connect work in Android Emulator, or is a physical device required?
4. **Backend deployment** - Will the web backend be deployed for testing, or use localhost only?

---

## Appendix: API Reference

### POST `/api/sync/health-data/test`

**URL:** `http://10.0.2.2:4000/api/sync/health-data/test` (emulator) or deployed URL

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "records": [
    {
      "type": "steps",
      "date": "2025-12-14",
      "count": 8500
    },
    {
      "type": "steps",
      "date": "2025-12-13",
      "count": 10200
    }
  ]
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Data received successfully",
  "recordCount": 2,
  "timestamp": "2025-12-14T08:15:00.000Z"
}
```

**Error Responses:**
- `400 Bad Request` - Missing or invalid `records` array
- `500 Internal Server Error` - Server processing error
