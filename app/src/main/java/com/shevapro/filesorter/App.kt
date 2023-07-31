package com.shevapro.filesorter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.shevapro.filesorter.data.database.TaskDao
import com.shevapro.filesorter.data.database.TaskDatabase
import com.shevapro.filesorter.data.repository.Repository
import com.shevapro.filesorter.data.repository.RepositoryImpl
import com.shevapro.filesorter.service.FileMover
import com.shevapro.filesorter.ui.viewmodel.MainViewModel

class App : Application() {
    companion object {

        private lateinit var instance: App

        lateinit var database: TaskDatabase

        lateinit var dao: TaskDao

        lateinit var repository: Repository

        lateinit var fileMover: FileMover

        val vmFactory by lazy {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(
                        app = instance,
                        repository = repository,
                        fileMover = fileMover
                    ) as T
                }
            }
        }

        lateinit var vm: MainViewModel

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        fileMover = FileMover.getInstance()

        database = TaskDatabase.getInstance(instance)

        dao = database.taskDao()

        repository = RepositoryImpl(dao)

        vm = vmFactory.create(MainViewModel::class.java)
    }
}