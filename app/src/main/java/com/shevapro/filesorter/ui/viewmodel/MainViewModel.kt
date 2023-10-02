package com.shevapro.filesorter.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.shevapro.filesorter.Utility
import com.shevapro.filesorter.Utility.AVERAGE_MANUAL_FILE_MOVE_PER_SECOND
import com.shevapro.filesorter.data.repository.Repository
import com.shevapro.filesorter.model.AppExceptions
import com.shevapro.filesorter.model.AppStatistic
import com.shevapro.filesorter.model.MostUsed
import com.shevapro.filesorter.model.PermissionExceptionForUri
import com.shevapro.filesorter.model.TaskRecord
import com.shevapro.filesorter.model.TaskRecord.Companion.EMPTY_ITEM
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.service.FileMover
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
import kotlin.math.roundToInt

class MainViewModel(
    private val app: Application,
    private val repository: Repository,
    private val fileMover: FileMover,
) :
    AndroidViewModel(app) {


    private val DELAY_TIME: Long = 2800
    private var     _tasks: MutableList<TaskRecord> = mutableListOf()
    private val uiTasks: List<UITaskRecord> get() = _tasks.map { it.toUITaskRecord() }

    private val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val mainState = _state.asStateFlow()

//    private val _hasError: MutableStateFlow<Boolean> = MutableStateFlow(false)
//    val hasError = _hasError.asStateFlow()
//
//    private val _errorMessage: MutableStateFlow<String> = MutableStateFlow("")
//    val errorMessage = _errorMessage.asStateFlow()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
//        _hasError.value = true


        _state.value = UiState.Data(
            uiTasks,
            AppExceptions.UnknownError(exception.message ?: "An error occured while processing..")
        )


    }

    private fun askPermissionForUri(uri: Uri?) {

    }


    private var _itemToEdit: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToEdit = _itemToEdit.asStateFlow()
    private var _itemToRemove: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToRemove get() =  _itemToRemove.asStateFlow()
    private var _itemToAdd: MutableStateFlow<UITaskRecord?> = MutableStateFlow(null)
    val itemToAdd get() =  _itemToAdd.asStateFlow()

    private var _isAddDialogpOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAddDialogpOpen get() =  _isAddDialogpOpen.asStateFlow()

    private var _isEditDialogpOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isEditDialogpOpen = _isEditDialogpOpen
        .asStateFlow()

    private var _appStats: MutableStateFlow<AppStatistic> = MutableStateFlow(AppStatistic())

    val appStats get() =  _appStats.asStateFlow()

    private var _foundExtensions: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val foundExtensions get() =  _foundExtensions.asStateFlow()



    init {
        wtihBlankSamples()
        initMainScreen()

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

    private fun initMainScreen() {
        _state.value = UiState.Loading
        viewModelScope.launch(Dispatchers.Unconfined + coroutineExceptionHandler) {
            //  for (i in  samples) addTask(i)
//            throw Exception("BOOM MISTAKE")


            repository.getTasks().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(), mutableListOf()
            ).collect { t ->
                _tasks = t.toMutableList()
                _state.value = UiState.Data(uiTasks)


            }
        }

        viewModelScope.launch {
//            fileMover.resetStats()
            fileMover.getStats().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(), mutableListOf()
            ).collect { list ->
                if (list.isNotEmpty()) {
                    val numberOfFilesMoved = list.sumOf { it.numberOfFileMoved }
                    val mostMovedFileByType: String =
                        list.groupBy { it.extension }.mapValues { it.value.size }
                            .maxBy { it.value }.key
                    val topSource: String = list.groupBy { it.source }.mapValues { it.value.size }
                        .maxBy { it.value }.key
                    val topDestination: String =
                        list.groupBy { it.target }.mapValues { it.value.size }
                            .maxBy { it.value }.key

                    val approxTimeSaved =
                        (numberOfFilesMoved * AVERAGE_MANUAL_FILE_MOVE_PER_SECOND).roundToInt()
                    val item = AppStatistic(
                        numberOfFilesMoved,
                        MostUsed(
                            Utility.formatUriToUIString(
                                Uri.decode(topSource)
                            ),
                            Utility.formatUriToUIString(Uri.decode(topDestination)),
                            mostMovedFileByType
                        ), timeSavedInMinutes = approxTimeSaved
                    )

                    _appStats.value = item
                }
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
        when {
            extension.isEmpty() -> {
                _state.value = UiState.Data(
                    uiTasks,
                    AppExceptions.MissingFieldException("Please, verify type input is not empty.")
                )
            }

            source.isEmpty() -> {
                _state.value = UiState.Data(
                    uiTasks,
                    AppExceptions.MissingFieldException("Please, verify a source folder has been selected.")
                )

            }

            destination.isEmpty() -> {
                _state.value = UiState.Data(
                    uiTasks,
                    AppExceptions.MissingFieldException("Please, verify a destination folder has been selected.")
                )

            }

            else -> {
                closeAddDialog()
                viewModelScope.launch(IO + coroutineExceptionHandler) {


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


    }

    fun updateItem(itemToBeUpdated: UITaskRecord) {

        when {
            itemToBeUpdated.extension.isEmpty() -> {
                _state.value = UiState.Data(
                    uiTasks,
                    AppExceptions.MissingFieldException("Please, verify type input is not empty.")
                )
            }

            itemToBeUpdated.source.isEmpty() -> {
                _state.value = UiState.Data(
                    uiTasks,
                    AppExceptions.MissingFieldException("Please, verify a source folder has been selected.")
                )

            }

            itemToBeUpdated.destination.isEmpty() -> {
                _state.value = UiState.Data(
                    uiTasks,
                    AppExceptions.MissingFieldException("Please, verify a destination folder has been selected.")
                )

            }

            else -> {

                viewModelScope.launch(IO + coroutineExceptionHandler) {
                    val old = getTaskById(itemToBeUpdated.id)
                    println("updating item ${itemToBeUpdated.id} from  $old to ${itemToBeUpdated}")
                    closeEditDialog()
                    repository.updateTask(itemToBeUpdated.toTaskRecord())


                }
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
    fun sortFiles() {
        _state.value = UiState.Loading

        val items = _tasks.filter { it.isActive && it.errorMessage.isNullOrEmpty() }
        println("VIEWMODEL : action processing ${items.size} items")

        if (items.isNotEmpty()) {
            viewModelScope.launch(IO + coroutineExceptionHandler) {
                delay(DELAY_TIME)

                items.forEach { task ->
                    println("VIEWMODEL : processing with ext ${task.extension}")
                    try {
                        fileMover.moveFilesByType(
                            task.source,
                            task.destination,
                            task.extension.lowercase().trim(), context = app,
                            { progress -> _state.value = UiState.Processing(progress) },

                            )
                    } catch (e: PermissionExceptionForUri) {
                        viewModelScope.launch { repository.updateTask(task.copy(errorMessage = e.message)) }
                        sortFiles()
                    }


//                    fileMover.moveFilesWithExtension(app,
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

                    _state.value = UiState.Data(uiTasks, null)
                }
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

    fun dissmissErrorMessageForTask(id: Int) =
        viewModelScope.launch(IO + coroutineExceptionHandler) {
            val item = _tasks.first { it.id == id }.copy(errorMessage = null)
            _itemToEdit.value = item.toUITaskRecord()
            repository.updateTask(item)
        }

    fun getExtensionsForNewSource(folder:String) {
        val extensions = fileMover.getFilesExtensionsForFolder(folder, app)
        _foundExtensions.value = extensions
    }
    fun getExtensionsForPreviousSource(folder:String): List<String> = fileMover.getFilesExtensionsForFolder(folder, app)





}
