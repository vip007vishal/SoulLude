package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.getThemeSingleColor
import com.example.ui.PulsePlayerApp
import com.example.viewmodel.MusicViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {

    private val musicViewModel: MusicViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val customPrimaryInt by musicViewModel.themePrimaryColor.collectAsState()
            val themeMode by musicViewModel.themeMode.collectAsState()
            val customPrimary = Color(customPrimaryInt)
            val customBackground = getThemeSingleColor(themeMode)
            MyApplicationTheme(
                customPrimaryColor = customPrimary,
                customBackgroundColor = customBackground
            ) {
                val permissionState = rememberPermissionState(
                    permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        android.Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                )

                LaunchedEffect(Unit) {
                    if (!permissionState.status.isGranted) {
                        permissionState.launchPermissionRequest()
                    }
                }

                if (permissionState.status.isGranted) {
                    LaunchedEffect(Unit) {
                        musicViewModel.loadLocalMusic()
                    }
                    PulsePlayerApp(viewModel = musicViewModel)
                } else {
                    com.example.ui.screens.OnboardingScreen(
                        onRequestPermission = { permissionState.launchPermissionRequest() }
                    )
                }
            }
        }
    }
}
