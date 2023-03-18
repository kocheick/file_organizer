package com.example.fileorganizer.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.fileorganizer.TaskOrder
import com.example.fileorganizer.data.repository.Repository
import com.example.fileorganizer.service.FileMover
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository, private val fileMover: FileMover) :
    ViewModel() {


    lateinit var tasks: LiveData<List<TaskOrder>>


    init {
        viewModelScope.launch {
            //  for (i in  samples) addTask(i)

            tasks = repository.getTasks()

        }
//        deleteAll()
    }


    fun addTask(taskOrder: TaskOrder) {
        viewModelScope.launch {
            repository.addTask(taskOrder)
        }
    }

    fun deleteAll() = viewModelScope.launch { repository.deleteAll() }

    @RequiresApi(Build.VERSION_CODES.R)
    fun executeMove() {

        viewModelScope.launch {
            try {
                tasks.value?.forEach { task ->
                    fileMover.moveFile(task.source,task.destination,task.extension)
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


}