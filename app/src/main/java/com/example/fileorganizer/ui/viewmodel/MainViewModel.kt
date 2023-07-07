package com.example.fileorganizer.ui.viewmodel

import android.os.Build
import android.provider.Contacts.Intents.UI
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository, private val fileMover: FileMover) :
    ViewModel() {


    private var _tasks: MutableList<TaskRecord> = mutableListOf()
    val tasks: List<TaskRecord> = _tasks

    private val _state: MutableStateFlow<MainState> = MutableStateFlow(MainState.Loading)

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->

        _state.value = MainState.Error(exception.message ?: "An unknown error happened")

    }
    val mainState = _state.asStateFlow()

    init {
        wtihSamples()
        initTasks()

    }

    private fun wtihSamples() {
        deleteAll()
        var count = 0
        repeat(15){
            viewModelScope.launch{ repository.addTask(TaskRecord.EMPTY_ITEM) }
        }
    }

    fun initTasks() {

        viewModelScope.launch(Dispatchers.Unconfined + coroutineExceptionHandler) {
            //  for (i in  samples) addTask(i)
            delay(3_000)
//            throw Exception("BOOM MISTAKE")

            repository.getTasks().collect { t ->
                _tasks = t as MutableList<TaskRecord>
                _state.value = MainState.Success(t.map { it.toUITaskRecord() })
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

    fun addTask(extension:String, source:String,destination:String) {
        viewModelScope.launch(IO+coroutineExceptionHandler) {
            repository.addTask(EMPTY_ITEM.copy(extension= extension,source= source,destination = destination))
        }

//        initTasks()
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

    fun deleteAll() = viewModelScope.launch { repository.deleteAll() }

    @RequiresApi(Build.VERSION_CODES.R)
    fun processTasks() {
        viewModelScope.launch(IO+coroutineExceptionHandler) {
                _tasks.filter { it.isActive }.forEach { task ->

                    fileMover.moveFiles(task.source, task.destination, task.extension)
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
    }

    fun toggleStateFor(itemToBeToggled: UITaskRecord) {
        viewModelScope.launch(IO + coroutineExceptionHandler) {
            repository.updateTask(itemToBeToggled.toTaskRecord().copy(isActive = !itemToBeToggled.isActive))
        }
    }


}
