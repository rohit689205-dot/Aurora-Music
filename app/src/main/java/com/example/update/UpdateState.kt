package com.example.update

import com.google.android.play.core.appupdate.AppUpdateInfo

enum class UpdateMode {
    FLEXIBLE,
    IMMEDIATE
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    
    data class NoUpdate(
        val versionName: String,
        val lastCheckedMs: Long = System.currentTimeMillis()
    ) : UpdateState()

    data class UpdateAvailable(
        val appUpdateInfo: AppUpdateInfo?,
        val availableVersionName: String,
        val availableVersionCode: Int,
        val updateMode: UpdateMode,
        val releaseNotes: String = "Bug fixes, performance improvements, and enhanced audio engine features."
    ) : UpdateState()

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytesToDownload: Long,
        val progressPercent: Int
    ) : UpdateState()

    data class Downloaded(
        val appUpdateInfo: AppUpdateInfo?
    ) : UpdateState()

    object Installing : UpdateState()

    data class Error(
        val message: String
    ) : UpdateState()
}
