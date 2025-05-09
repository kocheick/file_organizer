package com.shevapro.filesorter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.shevapro.filesorter.model.TaskStats
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.ui.NavigationRoutes
import com.shevapro.filesorter.ui.components.ProgressScreen
import com.shevapro.filesorter.ui.components.ads.AdBanner
import com.shevapro.filesorter.ui.components.main.HeaderComponent
import com.shevapro.filesorter.ui.theme.AppTheme
import com.shevapro.filesorter.ui.viewmodel.MainViewModel

@Composable
fun ProcessingScreen(
 mainState: UiState ,
    onDone: () -> Unit
) {

    val currentStats = when (mainState) {
        is UiState.Processing -> (mainState as UiState.Processing).stats
        is UiState.ProcessingComplete -> (mainState as UiState.ProcessingComplete).stats
        else -> TaskStats(currentFileName = "")
    }

    val isComplete = mainState is UiState.ProcessingComplete

    AppTheme {
        Scaffold(
            topBar = { HeaderComponent() },
            bottomBar = { AdBanner() }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                ProgressScreen(
                    currentMoveStats = currentStats,
                    isComplete = isComplete,
                    onDone = {
                     onDone()
                    }
                )
            }
        }
    }
}