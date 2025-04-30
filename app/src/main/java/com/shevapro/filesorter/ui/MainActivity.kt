package com.shevapro.filesorter.ui

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shevapro.filesorter.App
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.ui.components.MainScreen
import com.shevapro.filesorter.ui.screens.TaskEditorScreen
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import com.shevapro.filesorter.ui.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        App.vm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            supportActionBar?.hide()

            AppNavigation(viewModel)
        }
    }
}

enum class Screen {
    Main, AddTask, EditTask
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.Main) }
    val itemToAdd by viewModel.itemToAdd.collectAsState()
    val itemToEdit by viewModel.itemToEdit.collectAsState()
    val foundExtensions by viewModel.foundExtensions.collectAsState()

    when (currentScreen) {
        Screen.Main -> {
            MainScreen(
                viewModel = viewModel,
                onNavigateToAddTask = { 
                    currentScreen = Screen.AddTask
                    if (itemToAdd == null) viewModel.onUpdateItemToAdd(UITaskRecord.EMPTY_OBJECT)
                },
                onNavigateToEditTask = { task ->
                    currentScreen = Screen.EditTask
                    viewModel.onUpdateItemToEdit(task)
                }
            )
        }
        Screen.AddTask -> {
            TaskEditorScreen(
                item = itemToAdd ?: UITaskRecord.EMPTY_OBJECT,
                isEditMode = false,
                onSaveTask = { extension, source, destination ->
                    viewModel.addNewItemWith(extension, source, destination)
                    currentScreen = Screen.Main
                },
                onCancel = {
                    currentScreen = Screen.Main
                },
                onSaveUpdates = { viewModel.onUpdateItemToAdd(it) },
                foundExtensions = foundExtensions,
                onSourceSelected = { viewModel.getExtensionsForNewSource(it) }
            )
        }
        Screen.EditTask -> {
            TaskEditorScreen(
                item = itemToEdit ?: UITaskRecord.EMPTY_OBJECT,
                isEditMode = true,
                onSaveTask = { extension, source, destination ->
                    val updatedItem = (itemToEdit ?: UITaskRecord.EMPTY_OBJECT).copy(
                        extension = extension,
                        source = source,
                        destination = destination
                    )
                    viewModel.updateItem(updatedItem)
                    currentScreen = Screen.Main
                },
                onCancel = {
                    currentScreen = Screen.Main
                },
                onSaveUpdates = { viewModel.onUpdateItemToEdit(it) },
                foundExtensions = foundExtensions,
                onSourceSelected = { viewModel.getExtensionsForNewSource(it) }
            )
        }
    }
}

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
