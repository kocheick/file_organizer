package com.example.fileorganizer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fileorganizer.data.database.TaskDao
import com.example.fileorganizer.data.repository.Repository
import com.example.fileorganizer.data.repository.RepositoryImpl
import com.example.fileorganizer.data.database.TaskDatabase
import com.example.fileorganizer.service.FileMover
import com.example.fileorganizer.ui.viewmodel.MainViewModel

class App : Application() {
    companion object {

        private lateinit var instance: App

        lateinit var database: TaskDatabase

        lateinit var dao: TaskDao

        lateinit var repository: Repository

        lateinit var fileMover : FileMover

        val vmFactory by lazy {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(repository = repository, fileMover = fileMover) as T
                }
            }
        }

        lateinit var vm: MainViewModel

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        fileMover = FileMover().apply {
            initService(this@App)
        }

        database = TaskDatabase.getInstance(instance)

        dao = database.taskDao()

        repository = RepositoryImpl(dao)

        vm = vmFactory.create(MainViewModel::class.java)
    }
}