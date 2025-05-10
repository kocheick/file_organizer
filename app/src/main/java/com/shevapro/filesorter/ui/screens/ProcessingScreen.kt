package com.shevapro.filesorter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.BackHandler
import com.shevapro.filesorter.model.AggregatedTaskStats
import com.shevapro.filesorter.model.TaskStats
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.ui.NavigationRoutes
import com.shevapro.filesorter.ui.components.ProgressScreen
import com.shevapro.filesorter.ui.components.AggregatedProgressScreen
import com.shevapro.filesorter.ui.components.ads.AdBanner
import com.shevapro.filesorter.ui.components.main.HeaderComponent
import com.shevapro.filesorter.ui.theme.AppTheme
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun ProcessingScreen(
    mainState: UiState,
    onDone: () -> Unit,
    onBackPressed: () -> Unit = onDone
) {
    var timeoutReached by remember { mutableStateOf(false) }
    var forceCompleteUI by remember { mutableStateOf(false) }

    BackHandler {
       onDone()
    }

    LaunchedEffect(key1 = Unit) {
        delay(2000)
        timeoutReached = true
        forceCompleteUI = true
        println("ProcessingScreen - Timeout reached, forcing Done button to appear")
    }

    println("ProcessingScreen - Current state: ${mainState::class.simpleName}")
    println("ProcessingScreen - forceCompleteUI: $forceCompleteUI, timeoutReached: $timeoutReached")

    val currentStats = if (mainState.isProcessing || mainState.isProcessingComplete) {
        mainState.stats ?: TaskStats(currentFileName = "")
    } else {
        TaskStats(currentFileName = "")
    }

    val aggregatedStats = if (mainState.isProcessingMultipleTasks || mainState.isProcessingMultipleTasksComplete) {
        mainState.aggregatedStats
    } else {
        null
    }

    val isComplete =
        mainState.isProcessingComplete || mainState.isProcessingMultipleTasksComplete || forceCompleteUI

    // Force Done button to appear if we're at 100% even if state is still Processing
    val forceComplete = when {
        forceCompleteUI -> true
        aggregatedStats != null && aggregatedStats.currentTask >= aggregatedStats.totalTasks -> true
        currentStats.numberOfFilesMoved >= currentStats.totalFiles && currentStats.totalFiles > 0 -> true
        else -> false
    }

    val showDoneButton = isComplete || forceComplete || timeoutReached
    println("ProcessingScreen - isComplete: $isComplete, forceComplete: $forceComplete, showDoneButton: $showDoneButton, timeoutReached: $timeoutReached")
    println("ProcessingScreen - isComplete: $isComplete, hasAggregatedStats: ${aggregatedStats != null}")

    Scaffold(
        topBar = { HeaderComponent() },
        floatingActionButton = {
            // Always show an emergency exit button after 5 seconds
            if (timeoutReached) {
                FloatingActionButton(
                    onClick = onDone,
                    backgroundColor = Color(0xFF4CAF50)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done"
                    )
                }
            }
        },
        bottomBar = { AdBanner() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (aggregatedStats != null) {
                AggregatedProgressScreen(
                    aggregatedStats = aggregatedStats,
                    isComplete = isComplete || forceComplete || timeoutReached,
                    onDone = onDone
                )
            } else {
                ProgressScreen(
                    currentMoveStats = currentStats,
                    isComplete = isComplete || forceComplete || timeoutReached,
                    onDone = onDone
                )
            }
        }
    }
}
