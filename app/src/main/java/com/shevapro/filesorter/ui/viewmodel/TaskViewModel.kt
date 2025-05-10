package com.shevapro.filesorter.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shevapro.filesorter.data.repository.Repository
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.ScheduleType
import com.shevapro.filesorter.model.TaskRecord
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for managing tasks
 */
class TaskViewModel(
    application: Application,
    private val taskRepository: Repository
) : AndroidViewModel(application) {

    // Main state for UI
    private val _mainState = MutableStateFlow<UiState>(UiState.data(emptyList()))
    val mainState: StateFlow<UiState> = _mainState.asStateFlow()

    // Task lists
    private val _allTasks = MutableStateFlow<List<UITaskRecord>>(emptyList())
    val allTasks: StateFlow<List<UITaskRecord>> = _allTasks.asStateFlow()

    private val _activeTasks = MutableStateFlow<List<UITaskRecord>>(emptyList())
    val activeTasks: StateFlow<List<UITaskRecord>> = _activeTasks.asStateFlow()

    private val _scheduledTasks = MutableStateFlow<List<UITaskRecord>>(emptyList())
    val scheduledTasks: StateFlow<List<UITaskRecord>> = _scheduledTasks.asStateFlow()

    // Current task being edited
    private val _currentTask = MutableStateFlow<UITaskRecord>(UITaskRecord.EMPTY_OBJECT)
    val currentTask: StateFlow<UITaskRecord> = _currentTask.asStateFlow()

    // Dialog states
    private val _itemToEdit = MutableStateFlow<UITaskRecord?>(null)
    val itemToEdit: StateFlow<UITaskRecord?> = _itemToEdit.asStateFlow()

    private val _itemToRemove = MutableStateFlow<UITaskRecord?>(null)
    val itemToRemove: StateFlow<UITaskRecord?> = _itemToRemove.asStateFlow()

    private val _itemToAdd = MutableStateFlow<UITaskRecord?>(null)
    val itemToAdd: StateFlow<UITaskRecord?> = _itemToAdd.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _isEditDialogOpen = MutableStateFlow(false)
    val isEditDialogOpen: StateFlow<Boolean> = _isEditDialogOpen.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.getTasks().collectLatest { tasks ->
                val uiTasks = tasks.map { it.toUITaskRecord() }
                _allTasks.value = uiTasks
                _mainState.value = UiState.data(uiTasks)
            }
        }

        viewModelScope.launch {
            taskRepository.getActiveTasks().collectLatest { tasks ->
                _activeTasks.value = tasks.map { it.toUITaskRecord() }
            }
        }

        viewModelScope.launch {
            taskRepository.getScheduledTasks().collectLatest { tasks ->
                _scheduledTasks.value = tasks.map { it.toUITaskRecord() }
            }
        }
    }

    /**
     * Dismisses the current error.
     */
    fun dismissError() {
        _mainState.value = UiState.data(_allTasks.value)
    }

    /**
     * Updates the item to edit.
     *
     * @param item The item to edit
     */
    fun onUpdateItemToEdit(item: UITaskRecord?) {
        _itemToEdit.value = item
    }

    /**
     * Updates the item to add.
     *
     * @param item The item to add
     */
    fun onUpdateItemToAdd(item: UITaskRecord?) {
        _itemToAdd.value = item
    }

    /**
     * Updates the item to remove.
     *
     * @param item The item to remove
     */
    fun onUpdateItemToRemove(item: UITaskRecord?) {
        _itemToRemove.value = item
    }

    /**
     * Opens the add dialog.
     */
    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    /**
     * Closes the add dialog.
     */
    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    /**
     * Opens the edit dialog.
     */
    fun openEditDialog() {
        _isEditDialogOpen.value = true
    }

    /**
     * Closes the edit dialog.
     */
    fun closeEditDialog() {
        _isEditDialogOpen.value = false
    }

    fun setCurrentTask(task: UITaskRecord) {
        _currentTask.value = task
    }

    fun toggleTaskActive(task: UITaskRecord) {
        viewModelScope.launch {
            val updatedTask = task.copy(isActive = !task.isActive)
            taskRepository.updateTask(updatedTask.toTaskRecord())
        }
    }

    fun toggleTaskScheduled(task: UITaskRecord) {
        viewModelScope.launch {
            val updatedTask = task.copy(isScheduled = !task.isScheduled)
            taskRepository.updateTask(updatedTask.toTaskRecord())
        }
    }

    fun updateScheduleTime(task: UITaskRecord, time: Date) {
        viewModelScope.launch {
            val updatedTask = task.copy(scheduleTime = time)
            taskRepository.updateTask(updatedTask.toTaskRecord())
        }
    }

    fun updateScheduleType(task: UITaskRecord, type: ScheduleType) {
        viewModelScope.launch {
            val updatedTask = task.copy(scheduleType = type)
            taskRepository.updateTask(updatedTask.toTaskRecord())
        }
    }

    fun updateSchedule(task: UITaskRecord, type: ScheduleType, time: Date) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                scheduleType = type,
                scheduleTime = time,
                isScheduled = true
            )
            taskRepository.updateTask(updatedTask.toTaskRecord())
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            val task = _currentTask.value.toTaskRecord()
            if (task.id == 0) {
                taskRepository.addTask(task)
            } else {
                taskRepository.updateTask(task)
            }
            _currentTask.value = UITaskRecord.EMPTY_OBJECT
        }
    }

    fun deleteTask(task: UITaskRecord) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.toTaskRecord())
        }
    }

    fun resetCurrentTask() {
        _currentTask.value = UITaskRecord.EMPTY_OBJECT
    }

    /**
     * Adds a new task with the given parameters.
     *
     * @param extension The file extension
     * @param source The source directory
     * @param destination The destination directory
     */
    fun addNewItemWith(extension: String, source: String, destination: String) {
        viewModelScope.launch {
            val newTask = UITaskRecord(
                extension = extension,
                source = source,
                destination = destination,
                isActive = true,
                id = 0  // Explicitly set to 0 so Room will use autoGenerate
            )
            taskRepository.addTask(newTask.toTaskRecord())
            _itemToAdd.value = null
            closeAddDialog()
        }
    }

    /**
     * Updates an existing task.
     *
     * @param task The task to update
     */
    fun updateItem(task: UITaskRecord) {
        viewModelScope.launch {
            taskRepository.updateTask(task.toTaskRecord())
            _itemToEdit.value = null
            closeEditDialog()
        }
    }

    /**
     * Removes a task.
     *
     * @param task The task to remove
     */
    fun removeItem(task: UITaskRecord) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.toTaskRecord())
        }
    }

    /**
     * Deletes all tasks.
     */
    fun deleteAll() {
        viewModelScope.launch {
            taskRepository.deleteAll()
        }
    }

    /**
     * Toggles the active state of a task.
     *
     * @param task The task to toggle
     */
    fun toggleStateFor(task: UITaskRecord) {
        viewModelScope.launch {
            val updatedTask = task.copy(isActive = !task.isActive)
            taskRepository.updateTask(updatedTask.toTaskRecord())
        }
    }

    /**
     * Dismisses the error message for a task.
     *
     * @param id The task ID
     */
    fun dismissErrorMessageForTask(id: Int) {
        viewModelScope.launch {
            val tasks = _allTasks.value
            val updatedTasks = tasks.map { 
                if (it.id == id) it.copy(errorMessage = null) else it 
            }
            _allTasks.value = updatedTasks
            _mainState.value = UiState.data(updatedTasks)
        }
    }

    /**
     * Updates a task with an error message.
     *
     * @param id The task ID
     * @param errorMessage The error message
     */
    fun updateTaskWithErrorMessage(id: Int, errorMessage: String) {
        viewModelScope.launch {
            val tasks = _allTasks.value
            val updatedTasks = tasks.map {
                if (it.id == id) it.copy(errorMessage = errorMessage, isActive = false) else it
            }
            _allTasks.value = updatedTasks
            _mainState.value = UiState.data(updatedTasks)
        }
    }
}
