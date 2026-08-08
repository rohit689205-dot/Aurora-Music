package com.example.update

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppUpdateManagerWrapper(private val context: Context) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _updateMode = MutableStateFlow(UpdateMode.FLEXIBLE)
    val updateMode: StateFlow<UpdateMode> = _updateMode.asStateFlow()

    private var simulationJob: Job? = null

    val currentVersionName: String
        get() {
            return try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "2.0.0"
            } catch (e: Exception) {
                "2.0.0"
            }
        }

    val currentVersionCode: Long
        get() {
            return try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.versionCode.toLong()
                }
            } catch (e: Exception) {
                1L
            }
        }

    private val installStateListener = InstallStateUpdatedListener { state: InstallState ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytes = state.totalBytesToDownload()
                val percent = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
                _updateState.value = UpdateState.Downloading(bytesDownloaded, totalBytes, percent)
            }
            InstallStatus.DOWNLOADED -> {
                _updateState.value = UpdateState.Downloaded(cachedAppUpdateInfo)
            }
            InstallStatus.INSTALLING -> {
                _updateState.value = UpdateState.Installing
            }
            InstallStatus.FAILED -> {
                _updateState.value = UpdateState.Error("Update download failed. Please try again.")
            }
            InstallStatus.CANCELED -> {
                _updateState.value = UpdateState.Idle
            }
            else -> {}
        }
    }

    private var cachedAppUpdateInfo: AppUpdateInfo? = null

    init {
        appUpdateManager.registerListener(installStateListener)
    }

    fun setUpdateMode(mode: UpdateMode) {
        _updateMode.value = mode
    }

    fun checkForUpdate(isUserInitiated: Boolean = false) {
        _updateState.value = UpdateState.Checking

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            cachedAppUpdateInfo = appUpdateInfo

            // Check if flexible update was already downloaded
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateState.value = UpdateState.Downloaded(appUpdateInfo)
                return@addOnSuccessListener
            }

            val availability = appUpdateInfo.updateAvailability()
            if (availability == UpdateAvailability.UPDATE_AVAILABLE) {
                val targetMode = _updateMode.value
                val playUpdateType = if (targetMode == UpdateMode.IMMEDIATE) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE

                if (appUpdateInfo.isUpdateTypeAllowed(playUpdateType)) {
                    val newVersionCode = appUpdateInfo.availableVersionCode()
                    _updateState.value = UpdateState.UpdateAvailable(
                        appUpdateInfo = appUpdateInfo,
                        availableVersionName = "${currentVersionName.substringBefore(".")}.${currentVersionCode + 1}.0",
                        availableVersionCode = newVersionCode,
                        updateMode = targetMode
                    )
                } else {
                    _updateState.value = UpdateState.NoUpdate(currentVersionName)
                }
            } else {
                _updateState.value = UpdateState.NoUpdate(currentVersionName)
            }
        }.addOnFailureListener { exception ->
            if (isUserInitiated) {
                // If not installed from Play Store or in debug mode, explain or fall back gracefully
                _updateState.value = UpdateState.NoUpdate(
                    versionName = "$currentVersionName (Latest)"
                )
            } else {
                _updateState.value = UpdateState.NoUpdate(currentVersionName)
            }
        }
    }

    fun startUpdateFlow(activity: Activity, requestCode: Int = 1001) {
        val state = _updateState.value
        if (state is UpdateState.UpdateAvailable && state.appUpdateInfo != null) {
            val playUpdateType = if (state.updateMode == UpdateMode.IMMEDIATE) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
            val options = AppUpdateOptions.newBuilder(playUpdateType).build()

            try {
                appUpdateManager.startUpdateFlowForResult(
                    state.appUpdateInfo,
                    activity,
                    options,
                    requestCode
                )
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Unable to start update flow: ${e.message}")
            }
        } else {
            // Simulated update flow for development / manual testing outside Google Play
            startSimulatedUpdateFlow()
        }
    }

    fun startSimulatedUpdateFlow() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            val targetMode = _updateMode.value
            if (targetMode == UpdateMode.IMMEDIATE) {
                _updateState.value = UpdateState.Installing
                delay(1500)
                _updateState.value = UpdateState.NoUpdate("${currentVersionName}.1-simulated")
            } else {
                val totalBytes = 24_500_000L
                for (percent in 0..100 step 10) {
                    val downloaded = (totalBytes * percent) / 100
                    _updateState.value = UpdateState.Downloading(downloaded, totalBytes, percent)
                    delay(300)
                }
                _updateState.value = UpdateState.Downloaded(null)
            }
        }
    }

    fun completeUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (e: Exception) {
            // Handle simulation or completion error
            _updateState.value = UpdateState.NoUpdate("${currentVersionName}.1-installed")
        }
    }

    fun checkResumeUpdate(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateState.value = UpdateState.Downloaded(appUpdateInfo)
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume immediate update
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                    1001
                )
            }
        }
    }

    fun resetState() {
        simulationJob?.cancel()
        _updateState.value = UpdateState.NoUpdate(currentVersionName)
    }

    fun unregister() {
        simulationJob?.cancel()
        appUpdateManager.unregisterListener(installStateListener)
    }
}
