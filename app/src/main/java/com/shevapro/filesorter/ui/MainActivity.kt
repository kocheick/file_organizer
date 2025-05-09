package com.shevapro.filesorter.ui

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.shevapro.filesorter.App
import com.shevapro.filesorter.service.TaskExecutionService
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import com.shevapro.filesorter.ui.viewmodel.RuleViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        App.vm
    }

    val ruleViewModel: RuleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the task execution service
        TaskExecutionService.startService(this)

        setContent {
            supportActionBar?.hide()
            AppNavigation(viewModel = viewModel, ruleViewModel = ruleViewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the service when the app is destroyed
        TaskExecutionService.stopService(this)
    }
}

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}