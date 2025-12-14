# Tasks: Circadia Health Connect Sync MVP

## Relevant Files

- `app/build.gradle.kts` - Add Health Connect, Retrofit, and Coroutines dependencies
- `app/src/main/AndroidManifest.xml` - Add Health Connect permissions and queries
- `app/src/main/res/xml/network_security_config.xml` - Allow cleartext HTTP for local development
- `app/src/main/java/com/circadia/healthsync/data/HealthConnectManager.kt` - Health Connect client wrapper for reading steps data
- `app/src/main/java/com/circadia/healthsync/data/api/CircadiaApi.kt` - Retrofit API interface for sync endpoint
- `app/src/main/java/com/circadia/healthsync/data/api/ApiClient.kt` - Retrofit client configuration
- `app/src/main/java/com/circadia/healthsync/data/model/SyncRequest.kt` - Request body data class
- `app/src/main/java/com/circadia/healthsync/data/model/SyncResponse.kt` - Response body data class
- `app/src/main/java/com/circadia/healthsync/data/model/StepRecord.kt` - Step record data class
- `app/src/main/java/com/circadia/healthsync/ui/SyncViewModel.kt` - ViewModel managing sync state and business logic
- `app/src/main/java/com/circadia/healthsync/ui/SyncScreen.kt` - Main Compose UI screen
- `app/src/main/java/com/circadia/healthsync/MainActivity.kt` - Update to use SyncScreen
- `app/src/main/res/values/strings.xml` - String resources for UI text

### Notes

- Health Connect requires API 26+ (Android 8.0)
- Use `10.0.2.2` for localhost when running on Android Emulator
- Health Connect may need to be installed on the emulator/device for testing
- Unit tests are deferred for MVP to keep scope minimal

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch: `git checkout -b feature/basic-sync`

- [x] 1.0 Configure project dependencies and permissions
  - [x] 1.1 Add Health Connect dependency to `app/build.gradle.kts`
  - [x] 1.2 Add Retrofit and Gson converter dependencies
  - [x] 1.3 Add Coroutines dependencies (if not present)
  - [x] 1.4 Add Health Connect READ_STEPS permission to AndroidManifest.xml
  - [x] 1.5 Add Health Connect package query to AndroidManifest.xml
  - [x] 1.6 Create network security config XML to allow cleartext HTTP for development
  - [x] 1.7 Reference network security config in AndroidManifest.xml
  - [x] 1.8 Add INTERNET permission to AndroidManifest.xml
  - [x] 1.9 Sync Gradle and verify project builds

- [x] 2.0 Implement Health Connect integration
  - [x] 2.1 Create `HealthConnectManager` class with Health Connect client initialization
  - [x] 2.2 Add method to check if Health Connect is available on device
  - [x] 2.3 Add method to check/request steps read permission
  - [x] 2.4 Add method to read step count records for the last 7 days
  - [x] 2.5 Add method to aggregate daily step counts from raw records
  - [x] 2.6 Handle Health Connect not installed scenario

- [x] 3.0 Implement API client and data models
  - [x] 3.1 Create `StepRecord` data class matching API schema (type, date, count)
  - [x] 3.2 Create `SyncRequest` data class with records list
  - [x] 3.3 Create `SyncResponse` data class (success, message, recordCount, timestamp)
  - [x] 3.4 Create `CircadiaApi` Retrofit interface with POST sync endpoint
  - [x] 3.5 Create `ApiClient` singleton with configurable base URL (default: 10.0.2.2:4000)
  - [x] 3.6 Add error handling for network failures

- [x] 4.0 Implement ViewModel and sync logic
  - [x] 4.1 Create `SyncUiState` sealed class/enum for UI states (Ready, Syncing, Success, Error, NoPermission, NoHealthConnect)
  - [x] 4.2 Create `SyncViewModel` with StateFlow for UI state
  - [x] 4.3 Add method to check Health Connect availability and permissions on init
  - [x] 4.4 Add sync method that: reads steps → transforms to API format → POSTs to backend
  - [x] 4.5 Handle success response and update UI state with record count and timestamp
  - [x] 4.6 Handle error responses (network error, server error) with descriptive messages
  - [x] 4.7 Add loading state management (disable button during sync)

- [x] 5.0 Build the Sync UI screen
  - [x] 5.1 Create `SyncScreen` composable with app title "Circadia"
  - [x] 5.2 Add prominent Sync button with icon
  - [x] 5.3 Add status area displaying current sync state
  - [x] 5.4 Show loading spinner when syncing
  - [x] 5.5 Display success message with record count and timestamp
  - [x] 5.6 Display error messages with description
  - [x] 5.7 Disable Sync button while sync is in progress
  - [x] 5.8 Add permission request handling with UI feedback
  - [x] 5.9 Add "Install Health Connect" prompt when not available

- [x] 6.0 Integrate and test end-to-end
  - [x] 6.1 Update MainActivity to use SyncScreen and SyncViewModel
  - [x] 6.2 Wire up Health Connect permission request flow in Activity
  - [x] 6.3 Set up test backend (see Testing Notes below)
  - [x] 6.4 Add sample step data to Health Connect on emulator/device
  - [x] 6.5 Test Health Connect permission request flow
  - [x] 6.6 Test reading steps data from Health Connect
  - [x] 6.7 Test sync button - verify data reaches backend
  - [x] 6.8 Verify success state displays record count and timestamp
  - [x] 6.9 Test error states: stop backend and verify error message shows
  - [x] 6.10 Test edge cases: no Health Connect, permission denied

## Testing Notes

### Running the Backend
Start your Circadia web backend locally:
```bash
cd /path/to/circadia-web
npm run dev  # or your start command
# Backend runs on http://localhost:4000
```

The Android app will POST to: `POST /api/sync/health-data/test`

### Network Configuration

| Device | Backend URL |
|--------|-------------|
| Android Emulator | `http://10.0.2.2:4000` (default in app) |
| Physical Device | `http://YOUR_COMPUTER_IP:4000` (e.g., `192.168.1.100`) |

**Tip:** Find your IP with `ifconfig | grep inet` on Mac.

### Adding Test Data to Health Connect
On the emulator/device:
1. Open the **Health Connect** app
2. Go to **Data and access** → **Browse data** → **Steps**
3. Tap **Add data** to manually add step records
4. Alternatively, use a fitness app (like Google Fit) that writes to Health Connect

### Verification Checklist
- [x] Backend is running and accessible
- [x] Health Connect has sample step data
- [x] App granted Health Connect permission
- [x] Tap Sync → see "Sync successful! X records"
- [x] Check backend logs/database to confirm data arrived
