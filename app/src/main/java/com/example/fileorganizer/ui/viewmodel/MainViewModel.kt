package com.example.fileorganizer.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.example.fileorganizer.TaskRecord
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
        initTasks()

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

        fun addTask(taskRecord: TaskRecord) {
            viewModelScope.launch(IO) {
                repository.addTask(taskRecord)
            }

//        initTasks()
        }

        fun deleteAll() = viewModelScope.launch { repository.deleteAll() }

        @RequiresApi(Build.VERSION_CODES.R)
        fun processTasks() {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    _tasks.forEach { task ->

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
                } catch (e: Exception) {
                    println("${e.message}")
                }
            }
        }

        fun updateItem(itemToBeUpdated: UITaskRecord) {

            try {
                viewModelScope.launch(Dispatchers.IO) {
                    val old = getTaskById(itemToBeUpdated.id)
                    println("updating item ${itemToBeUpdated.id} from  $old to ${itemToBeUpdated}")

                    repository.updateTask(itemToBeUpdated.toTaskRecord())

                }
//            initTasks()
            } catch (e: Exception) {
                println(e.message)
            }

        }


    }
