package com.example.fileorganizer.ui.viewmodel

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.example.fileorganizer.TaskRecord
import com.example.fileorganizer.TaskRecord.Companion.EMPTY_ITEM
import com.example.fileorganizer.data.repository.Repository
import com.example.fileorganizer.model.EmptyContentException
import com.example.fileorganizer.model.UiState
import com.example.fileorganizer.model.MissingFieldException
import com.example.fileorganizer.model.UITaskRecord
import com.example.fileorganizer.service.FileMover
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

class MainViewModel(private val repository: Repository, private val fileMover: FileMover) :
    ViewModel() {


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
       _state.value = UiState.Data(_tasks.map { it.toUITaskRecord() },exception )
println(exception.message)
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

    fun openAddDialog(){ _isAddDialogpOpen.update { true }}
    fun closeAddDialog(){ _isAddDialogpOpen.update { false }}
    fun openEditDialog(){ _isEditDialogpOpen.update { true }}
    fun closeEditDialog(){ _isEditDialogpOpen.update { false }}

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
                    _tasks = t as MutableList<TaskRecord>
                    _state.value = UiState.Data(t.map { it.toUITaskRecord() })

            }

        }
    }

    //        deleteAll()
    fun getTaskById(id: Int): TaskRecord {
        var task: TaskRecord = TaskRecord.EMPTY_ITEM
        viewModelScope.launch(IO) {
            task = repository.getTaskbyId(id) ?: return@launch
        }

        return task

    }

    fun addNewItemWith(extension: String, source: String, destination: String) {
        viewModelScope.launch(IO + coroutineExceptionHandler) {
           if (extension.isEmpty() or source.isEmpty() or destination.isEmpty()) {
               closeEditDialog()
               openAddDialog()
               throw MissingFieldException("Please, verify all inputs are filled.")
           }
            else {

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
    }

    fun updateItem(itemToBeUpdated: UITaskRecord) {

        viewModelScope.launch(IO + coroutineExceptionHandler) {
            val old = getTaskById(itemToBeUpdated.id)
            println("updating item ${itemToBeUpdated.id} from  $old to ${itemToBeUpdated}")
            if (itemToBeUpdated.extension.isEmpty() or itemToBeUpdated.source.isEmpty() or itemToBeUpdated.destination.isEmpty()) {
                closeAddDialog()
                openEditDialog()
                throw MissingFieldException("Please, verify all inputs are filled.")
            }
            else {
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

                if (items.isNotEmpty()){
                    delay(DELAY_TIME)

                items.forEach { task ->
                    println("VIEWMODEL : processing with ext ${task.extension}")
                    fileMover.moveFilesByType(
                        task.source,
                        task.destination,
                        task.extension.lowercase().trim()
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
                    _state.value = UiState.Data(_tasks.map { it.toUITaskRecord() },null )

                }else {

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
