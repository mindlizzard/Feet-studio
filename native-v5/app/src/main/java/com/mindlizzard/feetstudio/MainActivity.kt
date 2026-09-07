package com.mindlizzard.feetstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindlizzard.feetstudio.ui.FeetStudioApp
import com.mindlizzard.feetstudio.ui.theme.FeetStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeetStudioTheme {
                val studioViewModel: StudioViewModel = viewModel()
                FeetStudioApp(studioViewModel)
            }
        }
    }
}
