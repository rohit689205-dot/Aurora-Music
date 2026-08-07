package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AuroraApp
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.AuroraTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val settingsViewModel: SettingsViewModel = viewModel()
      val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
      val uiDensity by settingsViewModel.uiDensity.collectAsStateWithLifecycle()
      val accentColor by settingsViewModel.accentColor.collectAsStateWithLifecycle()
      val dynamicColor by settingsViewModel.dynamicColor.collectAsStateWithLifecycle()

      AuroraTheme(
        themeMode = themeMode,
        uiDensity = uiDensity,
        accentColor = accentColor,
        dynamicColor = dynamicColor
      ) {
        AuroraApp()
      }
    }
  }
}


