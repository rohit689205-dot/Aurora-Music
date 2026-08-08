package com.example.update

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    val wrapper = AppUpdateManagerWrapper(application)

    val updateState: StateFlow<UpdateState> = wrapper.updateState
    val updateMode: StateFlow<UpdateMode> = wrapper.updateMode

    val currentVersionName: String
        get() = wrapper.currentVersionName

    val currentVersionCode: Long
        get() = wrapper.currentVersionCode

    init {
        // Initial non-blocking check on app launch
        checkForUpdate(isUserInitiated = false)
    }

    fun checkForUpdate(isUserInitiated: Boolean = false) {
        wrapper.checkForUpdate(isUserInitiated)
    }

    fun setUpdateMode(mode: UpdateMode) {
        wrapper.setUpdateMode(mode)
    }

    fun startUpdateFlow(activity: Activity) {
        wrapper.startUpdateFlow(activity)
    }

    fun startSimulatedUpdate() {
        wrapper.startSimulatedUpdateFlow()
    }

    fun completeUpdate() {
        wrapper.completeUpdate()
    }

    fun checkResumeUpdate(activity: Activity) {
        wrapper.checkResumeUpdate(activity)
    }

    fun resetState() {
        wrapper.resetState()
    }

    override fun onCleared() {
        super.onCleared()
        wrapper.unregister()
    }
}
