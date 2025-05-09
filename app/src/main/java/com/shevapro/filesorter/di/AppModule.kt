package com.shevapro.filesorter.di

import com.shevapro.filesorter.data.database.TaskDatabase
import com.shevapro.filesorter.data.repository.Repository
import com.shevapro.filesorter.data.repository.RepositoryImpl
import com.shevapro.filesorter.data.repository.RuleRepository
import com.shevapro.filesorter.service.AdService
import com.shevapro.filesorter.service.ErrorHandlingService
import com.shevapro.filesorter.service.FileOperationService
import com.shevapro.filesorter.service.FileMover
import com.shevapro.filesorter.service.GlobalErrorHandler
import com.shevapro.filesorter.service.StatsService
import com.shevapro.filesorter.service.ValidationService
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import com.shevapro.filesorter.ui.viewmodel.RuleViewModel
import com.shevapro.filesorter.ui.viewmodel.SettingsViewModel
import com.shevapro.filesorter.ui.viewmodel.StatsViewModel
import com.shevapro.filesorter.ui.viewmodel.TaskViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for database and repositories
 */
val databaseModule = module {
    single { TaskDatabase.getInstance(androidContext()) }
    single { get<TaskDatabase>().taskDao() }
    single { get<TaskDatabase>().appStats() }
    single { get<TaskDatabase>().ruleDao() }
}

/**
 * Koin module for repositories
 */
val repositoryModule = module {
    single { RuleRepository(get()) }
    single<Repository> { RepositoryImpl(get(), get()) }
}

/**
 * Koin module for services
 */
val serviceModule = module {
    single { StatsService(get()) }
    single { ValidationService() }
    single { ErrorHandlingService(androidContext()) }
    single { GlobalErrorHandler(androidContext()) }
    single { FileOperationService(get()) }
    single { FileMover.getInstance(get(), androidContext()) }
    single { AdService(androidContext()) }
}

/**
 * Koin module for ViewModels
 */
val viewModelModule = module {
    viewModel { TaskViewModel(androidContext() as android.app.Application, get()) }
    viewModel { StatsViewModel(androidContext() as android.app.Application, get()) }
    viewModel { SettingsViewModel(androidContext() as android.app.Application) }
    viewModel { MainViewModel(androidContext() as android.app.Application, get(), get(), get()) }
    viewModel { RuleViewModel(androidContext() as android.app.Application, get()) }
}

/**
 * List of all Koin modules
 */
val appModules = listOf(
    databaseModule,
    repositoryModule,
    serviceModule,
    viewModelModule
)
