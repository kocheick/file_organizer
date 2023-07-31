package com.shevapro.filesorter.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.shevapro.filesorter.data.repository.Repository
import com.shevapro.filesorter.model.EmptyContentException
import com.shevapro.filesorter.model.MissingFieldException
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.service.FileMover
import com.shevapro.filesorter.TaskRecord
import com.shevapro.filesorter.TaskRecord.Companion.EMPTY_ITEM
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val app: Application,
    private val repository: Repository,
    private val fileMover: FileMover
) :
    AndroidViewModel(app) {


    private val DELAY_TIME: Long = 2800
    private var _tasks: MutableList<TaskRecord> = mutableListOf()

    private val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val mainState = _state.asStateFlow()

//    private val _hasError: MutableStateFlow<Boolean> = MutableStateFlow(false)
//    val hasError = _hasError.asStateFlow()
//
//    private val _errorMessage: MutableStateFlow<String> = MutableStateFlow("")
//    val errorMessage = _errorMessage.asStateFlow()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
//        _hasError.value = true


        _state.value = UiState.Data(_tasks.map { it.toUITaskRecord() }, exception)
        println(exception.message)


    }

    private fun askPermissionForUri(uri: Uri?) {

    }


    private var _itemToEdit: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToEdit = _itemToEdit.asStateFlow()
    private var _itemToRemove: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToRemove = _itemToRemove.asStateFlow()
    private var _itemToAdd: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToAdd = _itemToAdd.asStateFlow()

    private var _isAddDialogpOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAddDialogpOpen = _isAddDialogpOpen.asStateFlow()

    private var _isEditDialogpOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isEditDialogpOpen = _isEditDialogpOpen
        .asStateFlow()

    init {
        wtihBlankSamples()
        initTasks()

    }

    private fun wtihBlankSamples() {
//        deleteAll()
//        var count = 0
//        repeat(15){
//            viewModelScope.launch{ repository.addTask(TaskRecord.EMPTY_ITEM) }
//        }
    }

    fun openAddDialog() {
        _isAddDialogpOpen.update { true }
    }

    fun closeAddDialog() {
        _isAddDialogpOpen.update { false }
    }

    fun openEditDialog() {
        _isEditDialogpOpen.update { true }
    }

    fun closeEditDialog() {
        _isEditDialogpOpen.update { false }
    }

    fun dismissError() {
        _state.update {
            (it as UiState.Data).copy(exception = null)
        }
//        _hasError.value = false
//        _errorMessage.value = ""
//        initTasks()
    }

    fun onUpdateItemToEdit(item: UITaskRecord?) {
        _itemToEdit.value = item
    }

    fun onUpdateItemToAdd(item: UITaskRecord?) {
        _itemToAdd.value = item
    }

    fun onUpdateItemToRemove(item: UITaskRecord?) {
        _itemToRemove.value = item
    }

    private fun initTasks() {
        _state.value = UiState.Loading
        viewModelScope.launch(Dispatchers.Unconfined + coroutineExceptionHandler) {
            //  for (i in  samples) addTask(i)
            delay(DELAY_TIME)
//            throw Exception("BOOM MISTAKE")


            repository.getTasks().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(), mutableListOf()
            ).collect { t ->
                _tasks = t.toMutableList()
                _state.value = UiState.Data(t.map { it.toUITaskRecord() })

            }

        }
    }

    //        deleteAll()
    fun getTaskById(id: Int): TaskRecord {
        var task: TaskRecord = EMPTY_ITEM
        viewModelScope.launch(IO) {
            task = repository.getTaskbyId(id) ?: return@launch
        }

        return task

    }

    fun addNewItemWith(extension: String, source: String, destination: String) {
        viewModelScope.launch(IO + coroutineExceptionHandler) {

            require(extension.isNotEmpty()) {
                throw MissingFieldException("Please, verify type input is not empty.")
            }
            require(source.isNotEmpty()) {
                throw MissingFieldException("Please, verify a source folder has been selected.")
            }
            require(destination.isNotEmpty()) {
                throw MissingFieldException("Please, verify a destination folder has been selected.")
            }

            closeAddDialog()
            repository.addTask(
                EMPTY_ITEM.copy(
                    extension = extension,
                    source = source,
                    destination = destination,
                    isActive = true
                )
            )
            _itemToAdd.update { null }


        }
    }

    fun updateItem(itemToBeUpdated: UITaskRecord) {

        viewModelScope.launch(IO + coroutineExceptionHandler) {
            val old = getTaskById(itemToBeUpdated.id)
            println("updating item ${itemToBeUpdated.id} from  $old to ${itemToBeUpdated}")
            if (itemToBeUpdated.extension.isEmpty() or itemToBeUpdated.source.isEmpty() or itemToBeUpdated.destination.isEmpty()) {
                closeAddDialog()
                openEditDialog()
                throw MissingFieldException("Please, verify all inputs are filled.")
            } else {
                closeEditDialog()
                repository.updateTask(itemToBeUpdated.toTaskRecord())
            }

        }


    }

    fun removeItem(itemToBeDeleted: UITaskRecord) =
        viewModelScope.launch(IO + coroutineExceptionHandler) {
            repository.deleteTask(itemToBeDeleted.toTaskRecord())
        }

    fun deleteAll() =
        viewModelScope.launch(IO + coroutineExceptionHandler) { repository.deleteAll() }

    @RequiresApi(Build.VERSION_CODES.R)
    fun processTasks() {
        _state.value = UiState.Loading

        viewModelScope.launch(IO + coroutineExceptionHandler) {
            val items = _tasks.filter { it.isActive }
            println("VIEWMODEL : action processing ${items.size} items")

            if (items.isNotEmpty()) {
                delay(DELAY_TIME)

                items.forEach { task ->
                    println("VIEWMODEL : processing with ext ${task.extension}")
                    fileMover.moveFilesByType(
                        task.source,
                        task.destination,
                        task.extension.lowercase().trim(), context = app
                    )

//                    fileMover.moveFilesWithExtension(
//                        Uri.parse(task.source),
//                        task.destination.toUri(),
//                        task.extension.lowercase().trim()
//                    )
//                    fileMover.moveFiles(
//                        task.source,
//                        task.destination,
//                        task.extension.lowercase().trim()
//                    )
//                    val sourceFiles =
//                        withContext(Dispatchers.IO) { (fileMover.getFiles(task.from, task.type)) }
//                    println(sourceFiles)
//
//                    withContext(Dispatchers.IO) {
//                        sourceFiles.forEach {
//                            fileMover.moveFile(it, task.to)
//                        }
//                    }
                }
                _state.value = UiState.Data(_tasks.map { it.toUITaskRecord() }, null)

            } else {

                println("VIEWMODEL : list is empty")
                throw EmptyContentException("No item to be processed.")
            }
        }
//            initTasksΩΩTasks()
    }

    fun toggleStateFor(itemToBeToggled: UITaskRecord) {
        viewModelScope.launch(IO + coroutineExceptionHandler) {
            repository.updateTask(
                itemToBeToggled.toTaskRecord().copy(isActive = !itemToBeToggled.isActive)
            )
        }
    }


}