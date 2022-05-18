package com.example.fileorganizer.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.*
import com.example.fileorganizer.TaskOrder
import com.example.fileorganizer.data.repository.Repository
import com.example.fileorganizer.samples
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {


    lateinit var tasks: LiveData<List<TaskOrder>>


    init {
        viewModelScope.launch {
             //  for (i in  samples) addTask(i)

            tasks = repository.getTasks()

        }
        deleteAll()
    }


    fun addTask(taskOrder: TaskOrder) {
        viewModelScope.launch {
            repository.addTask(taskOrder)
        }
    }

    fun deleteAll() = viewModelScope.launch { repository.deleteAll() }


}