package com.shevapro.filesorter.ui.viewmodel

import android.app.Application
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shevapro.filesorter.data.repository.Repository
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.AggregatedTaskStats
import com.shevapro.filesorter.model.PermissionExceptionForUri
import com.shevapro.filesorter.model.TaskStats
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.service.FileMover
import com.shevapro.filesorter.service.AdService
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
    private val fileMover: FileMover,
    private val adService: AdService
) : AndroidViewModel(app) {

    // Specialized ViewModels
    private val taskViewModel = TaskViewModel(app, repository)
    private val statsViewModel = StatsViewModel(app, fileMover)
    private val settingsViewModel = SettingsViewModel(app)

    // State management
    private val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState.data(emptyList()))
    val mainState = _state.asStateFlow()

    // Ad-related state
    private val _shouldShowInterstitial = MutableStateFlow(false)
    val shouldShowInterstitial = _shouldShowInterstitial.asStateFlow()

    init {
        // Initialize the state with data from the TaskViewModel
        viewModelScope.launch {
            taskViewModel.mainState.collect { state ->
                _state.value = state
            }
        }

        // Ensure preset tasks are created
        viewModelScope.launch {
            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
            repository.createPresetTasks(downloadsPath)
        }

        // Preload interstitial ad
        adService.preloadInterstitialAd()
    }

    // Extensions for file selection
    private var _foundExtensions: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val foundExtensions get() = _foundExtensions.asStateFlow()

    // Exception handling
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _state.value = UiState.data(
            taskViewModel.mainState.value.let {
                if (it.isData) it.records else emptyList()
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
        println("Dismissing error")
        // Update our state with no error
        _state.value = UiState.data(
            taskViewModel.mainState.value.let {
                if (it.isData) it.records else emptyList()
            },
            null // Clear the error
        )
        // Also call the taskViewModel's dismiss error to ensure consistency
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
     * Checks if there are any active tasks that can be processed.
     *
     * @return true if there are active tasks, false otherwise
     */
    fun hasActiveTasksToProcess(): Boolean {
        return taskViewModel.mainState.value.let { state ->
            if (state.isData) {
                state.records.any { task -> task.isActive && task.errorMessage.isNullOrEmpty() }
            } else {
                false
            }
        }
    }

    /**
     * Shows an error message when there are no active tasks.
     */
    fun showNoActiveTasksError() {
        _state.value = UiState.data(
            taskViewModel.mainState.value.let { state ->
                if (state.isData) state.records else emptyList()
            },
            AppExceptions.EmptyContentException("No active tasks to process. Please add or activate tasks to proceed.")
        )
    }

    /**
     * Sorts files according to active tasks.
     */
    fun sortFiles() {
        val items: List<UITaskRecord> = taskViewModel.mainState.value.let { state ->
            if (state.isData) {
                state.records.filter { task -> task.isActive && task.errorMessage.isNullOrEmpty() }
            } else {
                emptyList<UITaskRecord>()
            }
        }

        // Handle case where there are no active tasks to process
        if (items.isEmpty()) {
            showNoActiveTasksError()
            return
        }

        if (items.size > 1) {
            _state.value = UiState.processingMultipleTasks(
                AggregatedTaskStats(
                    currentFileName = "Starting...",
                    totalTasks = items.size
                )
            )
        } else {
            _state.value = UiState.processing(TaskStats(currentFileName = "Starting..."))
        }

        println("VIEWMODEL : action processing ${items.size} items")

        if (items.isNotEmpty()) {
            viewModelScope.launch(IO + coroutineExceptionHandler) {
                var aggregatedStats: AggregatedTaskStats? = if (items.size > 1) {
                    AggregatedTaskStats(
                        currentFileName = "Starting...",
                        totalTasks = items.size,
                        startTime = System.currentTimeMillis()
                    )
                } else null

                items.forEachIndexed { index, task ->
                    println("VIEWMODEL : processing with ext ${task.extension}")

                    if (aggregatedStats != null) {
                        aggregatedStats = aggregatedStats!!.copy(
                            currentTask = index + 1,
                            currentFileExtension = task.extension,
                            currentSourceFolder = task.source,
                            currentDestinationFolder = task.destination
                        )
                        _state.value = UiState.processingMultipleTasks(aggregatedStats!!)
                    }

                    try {
                        if (aggregatedStats != null) {
                            fileMover.moveFilesByType(
                                task.source,
                                task.destination,
                                task.extension.lowercase().trim(),
                                context = app,
                                { progress ->
                                    // Make a local copy to ensure null safety
                                    val currentAggregatedStats = aggregatedStats ?: return@moveFilesByType

                                    // Check if there are any files to move at all
                                    val hasFilesToMove = progress.totalFiles > 0

                                    // Only update stats if there are files to process
                                    if (hasFilesToMove) {
                                        // Calculate the updated stats - everything in this block is safe
                                        val updatedFiles = progress.totalFiles
                                        val updatedMoved = progress.numberOfFilesMoved
                                        println("File count update - Task: ${index + 1}/${items.size}, Files moved: $updatedMoved/$updatedFiles")
                                        val updatedTransferred = currentAggregatedStats.totalBytesTransferred +
                                                (progress.totalBytesTransferred - currentAggregatedStats.totalBytesTransferred)
                                                    .coerceAtLeast(0)

                                        // Create a new stats object with all the updated values
                                        val newStats = currentAggregatedStats.copy(
                                            totalFiles = updatedFiles.coerceAtLeast(1),
                                            numberOfFilesMoved = updatedMoved.coerceAtLeast(0),
                                            currentFileName = progress.currentFileName,
                                            currentFileSize = progress.currentFileSize,
                                            currentBytesTransferred = progress.currentBytesTransferred,
                                            totalBytesTransferred = updatedTransferred,
                                            totalBytes = currentAggregatedStats.totalBytes + progress.totalBytes
                                        )
                                        // Update our aggregatedStats reference
                                        aggregatedStats = newStats
                                        // Update the UI state
                                        _state.value = UiState.processingMultipleTasks(newStats)
                                    } else {
                                        // This task has no files to move, update task count but keep other stats the same
                                        val newStats = currentAggregatedStats.copy(
                                            currentTask = index + 1,
                                            currentFileExtension = task.extension,
                                            currentSourceFolder = task.source,
                                            currentDestinationFolder = task.destination
                                        )
                                        // Update our aggregatedStats reference
                                        aggregatedStats = newStats
                                        // Update the UI state
                                        _state.value = UiState.processingMultipleTasks(newStats)
                                    }
                                }
                            )

                            // Update completed extensions list after task is complete
                            val currentAggregatedStats = aggregatedStats
                            if (currentAggregatedStats != null) {
                                // Get the final progress stats for this task from FileMover
                                val taskFileCount =
                                    fileMover.getTaskFileCount(task.source, task.extension.lowercase().trim(), app)

                                // Only add to completedExtensions if files were actually moved
                                val updatedExtensions = if (taskFileCount > 0) {
                                    currentAggregatedStats.completedExtensions + task.extension
                                } else {
                                    currentAggregatedStats.completedExtensions
                                }

                                println("Task ${index + 1} completed - Extension: ${task.extension}, Files found: $taskFileCount")

                                // Create a new stats object that marks this task as complete
                                val completedTaskStats = currentAggregatedStats.copy(
                                    currentTask = (index + 1).coerceAtMost(items.size),
                                    completedExtensions = updatedExtensions,
                                    totalFiles = currentAggregatedStats.totalFiles,
                                    numberOfFilesMoved = currentAggregatedStats.numberOfFilesMoved
                                )
                                println("Task ${index + 1}/${items.size} complete - Files: ${completedTaskStats.numberOfFilesMoved}/${completedTaskStats.totalFiles}")
                                // Update both our reference and the UI
                                aggregatedStats = completedTaskStats
                                _state.value = UiState.processingMultipleTasks(completedTaskStats)
                            }
                        } else {
                            fileMover.moveFilesByType(
                                task.source,
                                task.destination,
                                task.extension.lowercase().trim(),
                                context = app,
                                { progress -> _state.value = UiState.processing(progress) }
                            )
                        }
                    } catch (e: Exception) {
                        viewModelScope.launch {
                            // Handle specific errors and return to the main screen
                            println("Error processing task: ${e.message}")
                            // Update our state to Data state with the error
                            _state.value = UiState.data(
                                taskViewModel.mainState.value.let { state ->
                                    if (state.isData) state.records else emptyList()
                                },
                                AppExceptions.UnknownError(e.message ?: "An error occurred while processing files.")
                            )

                            // Mark task as having an error
                            taskViewModel.updateTaskWithErrorMessage(task.id, e.message ?: "Error processing files")
                        }
                    }
                }

                val lastStats = if (_state.value.isProcessing) _state.value.stats else null
                val lastAggregatedStats = if (_state.value.isProcessingMultipleTasks) _state.value.aggregatedStats else null

                println("Task processing complete. Current state: ${_state.value::class.simpleName}")
                println("lastStats: $lastStats, lastAggregatedStats: $lastAggregatedStats, aggregatedStats: $aggregatedStats")

                // Force transition to Complete state when processing is finished
                // This ensures we don't get stuck in the Processing state
                if (lastStats != null) {
                    _state.value = UiState.processingComplete(lastStats)
                    println("Setting state to ProcessingComplete")
                } else if (lastAggregatedStats != null) {
                    val movedFiles = lastAggregatedStats.numberOfFilesMoved
                    val completeStats = lastAggregatedStats.copy(
                        numberOfFilesMoved = movedFiles,
                        totalFiles = movedFiles,
                        currentTask = lastAggregatedStats.totalTasks
                    )
                    println("Setting final aggregated stats - Files: ${completeStats.numberOfFilesMoved}/${completeStats.totalFiles}")
                    _state.value = UiState.processingMultipleTasksComplete(completeStats)
                    println("Setting state to ProcessingMultipleTasksComplete from lastAggregatedStats")
                } else if (aggregatedStats != null && items.size > 1) {
                    val movedFiles = aggregatedStats!!.numberOfFilesMoved
                    val finalStats = aggregatedStats!!.copy(
                        currentTask = items.size,
                        totalTasks = items.size,
                        numberOfFilesMoved = movedFiles,
                        totalFiles = movedFiles,
                        totalBytesTransferred = aggregatedStats!!.totalBytes
                    )
                    println("Setting final forced stats - Files: ${finalStats.numberOfFilesMoved}/${finalStats.totalFiles}")
                    _state.value = UiState.processingMultipleTasksComplete(finalStats)
                    println("FORCING transition to ProcessingMultipleTasksComplete")
                } else {
                    // Single task or no stats at all
                    if (items.size == 1) {
                        // Single task case - create a minimal complete stats object
                        val singleTaskStats = TaskStats(
                            currentFileName = "Complete",
                            totalFiles = 1,
                            numberOfFilesMoved = 1
                        )
                        _state.value = UiState.processingComplete(singleTaskStats)
                        println("FORCING transition to ProcessingComplete for single task")
                    } else {
                        // Fall back to data state only if there are no items to process
                        _state.value = UiState.data(
                            if (taskViewModel.mainState.value.isData)
                                taskViewModel.mainState.value.records
                            else
                                emptyList()
                        )
                        println("Falling back to Data state")
                    }
                }

                _shouldShowInterstitial.value = true
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

    /**
     * Called when interstitial ad is closed or failed to show
     */
    fun onInterstitialAdClosed() {
        _shouldShowInterstitial.value = false
    }

    /**
     * Called when the user clicks the "Done" button on the processing complete screen
     * Transitions from ProcessingComplete state back to Data state
     */
    fun onProcessingCompleteAcknowledged() {
        _state.value = UiState.data(
            taskViewModel.mainState.value.let { state ->
                if (state.isData) state.records else emptyList()
            },
            null
        )
    }

    /**
     * Called when the user clicks the "Done" button on the multiple tasks processing complete screen
     * Transitions from ProcessingMultipleTasksComplete state back to Data state
     */
    fun onProcessingMultipleTasksCompleteAcknowledged() {
        _state.value = UiState.data(
            taskViewModel.mainState.value.let { state ->
                if (state.isData) state.records else emptyList()
            },
            null
        )
    }

    /**
     * Forces the current processing state to complete
     * Useful for handling cases where processing gets stuck
     */
    fun forceCompleteProcessing() {
        val currentState = _state.value

        // If we're already in a complete state or data state, do nothing
        if (currentState.isProcessingComplete ||
            currentState.isProcessingMultipleTasksComplete ||
            currentState.isData
        ) {
            return
        }

        println("Forcing processing to complete from state: ${currentState::class.simpleName}")

        // Convert the current state to a Complete state
        if (currentState.isProcessingMultipleTasks) {
            val stats = currentState.aggregatedStats
            if (stats != null) {
                val movedFiles = stats.numberOfFilesMoved
                _state.value = UiState.processingMultipleTasksComplete(
                    stats.copy(
                        currentTask = stats.totalTasks,
                        numberOfFilesMoved = movedFiles,
                        totalFiles = movedFiles // Set totalFiles = movedFiles for 100% visual
                    )
                )
            }
        } else if (currentState.isProcessing) {
            val stats = currentState.stats
            if (stats != null) {
                _state.value = UiState.processingComplete(
                    stats.copy(
                        numberOfFilesMoved = stats.numberOfFilesMoved.coerceAtMost(stats.totalFiles)
                    )
                )
            }
        } else {
            // Fall back to data state
            _state.value = UiState.data(
                taskViewModel.mainState.value.let { state ->
                    if (state.isData) state.records else emptyList()
                }
            )
        }
    }
}