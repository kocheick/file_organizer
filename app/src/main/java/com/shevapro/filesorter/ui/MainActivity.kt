package com.shevapro.filesorter.ui

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.shevapro.filesorter.App
import com.shevapro.filesorter.ui.components.MainScreen
import com.shevapro.filesorter.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        App.vm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            supportActionBar?.hide()

            MainScreen(viewModel)
        }
    }

}

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

