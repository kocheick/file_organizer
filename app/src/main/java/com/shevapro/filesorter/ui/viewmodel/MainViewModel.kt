package com.shevapro.filesorter.ui.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shevapro.filesorter.data.repository.Repository
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.PermissionExceptionForUri
import com.shevapro.filesorter.model.TaskStats
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.service.FileMover
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main ViewModel that coordinates between specialized ViewModels.
 * This class delegates to TaskViewModel, StatsViewModel, and SettingsViewModel.
 */
class MainViewModel(
    private val app: Application,
    private val repository: Repository,
    private val fileMover: FileMover
) : AndroidViewModel(app) {

    // Specialized ViewModels
    private val taskViewModel = TaskViewModel(app, repository)
    private val statsViewModel = StatsViewModel(app, fileMover)
    private val settingsViewModel = SettingsViewModel(app)

    // State management
    private val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState.Data(emptyList()))
    val mainState = _state.asStateFlow()

    init {
        // Initialize the state with data from the TaskViewModel
        viewModelScope.launch {
            taskViewModel.mainState.collect { state ->
                _state.value = state
            }
        }
    }

    // Extensions for file selection
    private var _foundExtensions: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val foundExtensions get() = _foundExtensions.asStateFlow()

    // Exception handling
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _state.value = UiState.Data(
            taskViewModel.mainState.value.let {
                if (it is UiState.Data) it.records else emptyList()
            },
            AppExceptions.UnknownError(exception.message ?: "An error occurred while processing..")
        )
    }

    // Delegate properties to TaskViewModel
    val itemToEdit get() = taskViewModel.itemToEdit
    val itemToRemove get() = taskViewModel.itemToRemove
    val itemToAdd get() = taskViewModel.itemToAdd
    val isAddDialogOpen get() = taskViewModel.isAddDialogOpen
    val isEditDialogOpen get() = taskViewModel.isEditDialogOpen

    // Delegate properties to StatsViewModel
    val appStats get() = statsViewModel.appStats

    // Delegate properties to SettingsViewModel
    val darkModeEnabled get() = settingsViewModel.darkModeEnabled
    val notificationsEnabled get() = settingsViewModel.notificationsEnabled
    val autoSortEnabled get() = settingsViewModel.autoSortEnabled
    val defaultSourceDirectory get() = settingsViewModel.defaultSourceDirectory
    val defaultDestinationDirectory get() = settingsViewModel.defaultDestinationDirectory

    /**
     * Dismisses the current error.
     */
    fun dismissError() {
        taskViewModel.dismissError()
    }

    /**
     * Updates the item to edit.
     *
     * @param item The item to edit
     */
    fun onUpdateItemToEdit(item: UITaskRecord?) {
        taskViewModel.onUpdateItemToEdit(item)
    }

    /**
     * Updates the item to add.
     *
     * @param item The item to add
     */
    fun onUpdateItemToAdd(item: UITaskRecord?) {
        taskViewModel.onUpdateItemToAdd(item)
    }

    /**
     * Updates the item to remove.
     *
     * @param item The item to remove
     */
    fun onUpdateItemToRemove(item: UITaskRecord?) {
        taskViewModel.onUpdateItemToRemove(item)
    }

    /**
     * Opens the add dialog.
     */
    fun openAddDialog() {
        taskViewModel.openAddDialog()
    }

    /**
     * Closes the add dialog.
     */
    fun closeAddDialog() {
        taskViewModel.closeAddDialog()
    }

    /**
     * Opens the edit dialog.
     */
    fun openEditDialog() {
        taskViewModel.openEditDialog()
    }

    /**
     * Closes the edit dialog.
     */
    fun closeEditDialog() {
        taskViewModel.closeEditDialog()
    }

    /**
     * Adds a new task with the specified parameters.
     *
     * @param extension The file extension
     * @param source The source directory
     * @param destination The destination directory
     */
    fun addNewItemWith(extension: String, source: String, destination: String) {
        taskViewModel.addNewItemWith(extension, source, destination)
    }

    /**
     * Updates an existing task.
     *
     * @param itemToBeUpdated The task to update
     */
    fun updateItem(itemToBeUpdated: UITaskRecord) {
        taskViewModel.updateItem(itemToBeUpdated)
    }

    /**
     * Removes a task.
     *
     * @param itemToBeDeleted The task to remove
     */
    fun removeItem(itemToBeDeleted: UITaskRecord) {
        taskViewModel.removeItem(itemToBeDeleted)
    }

    /**
     * Deletes all tasks.
     */
    fun deleteAll() {
        taskViewModel.deleteAll()
    }

    /**
     * Toggles the active state of a task.
     *
     * @param itemToBeToggled The task to toggle
     */
    fun toggleStateFor(itemToBeToggled: UITaskRecord) {
        taskViewModel.toggleStateFor(itemToBeToggled)
    }

    /**
     * Dismisses the error message for a task.
     *
     * @param id The task ID
     */
    fun dismissErrorMessageForTask(id: Int) {
        taskViewModel.dismissErrorMessageForTask(id)
    }

    /**
     * Gets file extensions for a new source directory.
     *
     * @param folder The source directory
     */
    fun getExtensionsForNewSource(folder: String) {
        val extensions = fileMover.getFilesExtensionsForFolder(folder, app)
        _foundExtensions.value = extensions
    }

    /**
     * Gets file extensions for a previous source directory.
     *
     * @param folder The source directory
     * @return List of file extensions
     */
    fun getExtensionsForPreviousSource(folder: String): List<String> = 
        fileMover.getFilesExtensionsForFolder(folder, app)

    /**
     * Sorts files according to active tasks.
     */
    fun sortFiles() {
        _state.value = UiState.Loading

        val items: List<UITaskRecord> = taskViewModel.mainState.value.let { state ->
            if (state is UiState.Data) {
                state.records.filter { task -> task.isActive && task.errorMessage.isNullOrEmpty() }
            } else {
                emptyList<UITaskRecord>()
            }
        }

        println("VIEWMODEL : action processing ${items.size} items")

        if (items.isNotEmpty()) {
            viewModelScope.launch(IO + coroutineExceptionHandler) {
                delay(2800) // Delay for UI feedback

                items.forEach { task ->
                    println("VIEWMODEL : processing with ext ${task.extension}")
                    try {
                        fileMover.moveFilesByType(
                            task.source,
                            task.destination,
                            task.extension.lowercase().trim(), 
                            context = app,
                            { progress -> _state.value = UiState.Processing(progress) }
                        )
                    } catch (e: PermissionExceptionForUri) {
                        viewModelScope.launch { 
                            taskViewModel.dismissErrorMessageForTask(task.id)
                        }
                        sortFiles()
                    }

                    _state.value = UiState.Data(
                        taskViewModel.mainState.value.let { state ->
                            if (state is UiState.Data) state.records else emptyList<UITaskRecord>()
                        },
                        null
                    )
                }
            }
        }
    }

    /**
     * Toggles dark mode.
     */
    fun toggleDarkMode() {
        settingsViewModel.toggleDarkMode()
    }

    /**
     * Toggles notifications.
     */
    fun toggleNotifications() {
        settingsViewModel.toggleNotifications()
    }

    /**
     * Toggles auto sort.
     */
    fun toggleAutoSort() {
        settingsViewModel.toggleAutoSort()
    }

    /**
     * Sets the default source directory.
     *
     * @param directory The directory path
     */
    fun setDefaultSourceDirectory(directory: String?) {
        settingsViewModel.setDefaultSourceDirectory(directory)
    }

    /**
     * Sets the default destination directory.
     *
     * @param directory The directory path
     */
    fun setDefaultDestinationDirectory(directory: String?) {
        settingsViewModel.setDefaultDestinationDirectory(directory)
    }

    /**
     * Resets all settings to default values.
     */
    fun resetSettings() {
        settingsViewModel.resetSettings()
    }

    /**
     * Resets statistics.
     */
    fun resetStats() {
        viewModelScope.launch {
            statsViewModel.resetStats()
        }
    }
}
