package com.example.fileorganizer.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.fileorganizer.TaskRecord
import com.example.fileorganizer.TaskRecord.Companion.EMPTY_ITEM
import com.example.fileorganizer.data.repository.Repository
import com.example.fileorganizer.model.MainState
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


    private var _tasks: MutableList<TaskRecord> = mutableListOf()

    private val _state: MutableStateFlow<MainState> = MutableStateFlow(MainState.Loading)
    val mainState = _state.asStateFlow()

//    private val _hasError: MutableStateFlow<Boolean> = MutableStateFlow(false)
//    val hasError = _hasError.asStateFlow()
//
//    private val _errorMessage: MutableStateFlow<String> = MutableStateFlow("")
//    val errorMessage = _errorMessage.asStateFlow()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
//        _hasError.value = true
       _state.value = MainState.Data(_tasks.map { it.toUITaskRecord() },exception )

    }


    private var _itemToEdit: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToEdit = _itemToEdit.asStateFlow()
    private var _itemToRemove: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToRemove = _itemToRemove.asStateFlow()
    private var _itemToAdd: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToAdd = _itemToAdd.asStateFlow()

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

    fun dismissError() {
        _state.update {
            (it as MainState.Data).copy(exception = null)
        }
//        _hasError.value = false
//        _errorMessage.value = ""
//        initTasks()
    }

    fun onUpdateItemToEdit(item: UITaskRecord?) {
        _itemToEdit.update { item }
    }

    fun onUpdateItemToAdd(item: UITaskRecord?) {
        _itemToAdd.update { item }
    }

    fun onUpdateItemToRemove(item: UITaskRecord?) {
        _itemToRemove.value = item
    }

    private fun initTasks() {
        _state.value = MainState.Loading
        viewModelScope.launch(Dispatchers.Unconfined + coroutineExceptionHandler) {
            //  for (i in  samples) addTask(i)
            delay(3_000)
//            throw Exception("BOOM MISTAKE")


            repository.getTasks().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(), mutableListOf()
            ).collect { t ->
                if (t.isNotEmpty()){
                    _tasks = t as MutableList<TaskRecord>
                    _state.value = MainState.Data(t.map { it.toUITaskRecord() })
                }
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
            repository.addTask(
                EMPTY_ITEM.copy(
                    extension = extension,
                    source = source,
                    destination = destination,
                    isActive = true
                )
            )
        }
    }

    fun updateItem(itemToBeUpdated: UITaskRecord) {

        viewModelScope.launch(IO + coroutineExceptionHandler) {
            val old = getTaskById(itemToBeUpdated.id)
            println("updating item ${itemToBeUpdated.id} from  $old to ${itemToBeUpdated}")

            repository.updateTask(itemToBeUpdated.toTaskRecord())

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
        _state.value = MainState.Loading
        viewModelScope.launch(IO + coroutineExceptionHandler) {
throw Exception("BOOOON")
            _tasks.filter { it.isActive }.forEach { task ->

                fileMover.moveFiles(task.source, task.destination, task.extension.lowercase().trim())
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
