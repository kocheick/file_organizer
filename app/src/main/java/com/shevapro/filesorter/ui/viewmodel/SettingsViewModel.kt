package com.shevapro.filesorter.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for application settings.
 */
class SettingsViewModel(
    private val app: Application
) : AndroidViewModel(app) {

    // Define settings state
    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled = _darkModeEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _autoSortEnabled = MutableStateFlow(false)
    val autoSortEnabled = _autoSortEnabled.asStateFlow()

    private val _defaultSourceDirectory = MutableStateFlow<String?>(null)
    val defaultSourceDirectory = _defaultSourceDirectory.asStateFlow()

    private val _defaultDestinationDirectory = MutableStateFlow<String?>(null)
    val defaultDestinationDirectory = _defaultDestinationDirectory.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Loads settings from SharedPreferences.
     */
    private fun loadSettings() {
        val sharedPreferences = app.getSharedPreferences("file_organizer_settings", Application.MODE_PRIVATE)
        
        _darkModeEnabled.value = sharedPreferences.getBoolean("dark_mode_enabled", false)
        _notificationsEnabled.value = sharedPreferences.getBoolean("notifications_enabled", true)
        _autoSortEnabled.value = sharedPreferences.getBoolean("auto_sort_enabled", false)
        _defaultSourceDirectory.value = sharedPreferences.getString("default_source_directory", null)
        _defaultDestinationDirectory.value = sharedPreferences.getString("default_destination_directory", null)
    }

    /**
     * Saves settings to SharedPreferences.
     */
    private fun saveSettings() {
        val sharedPreferences = app.getSharedPreferences("file_organizer_settings", Application.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        
        editor.putBoolean("dark_mode_enabled", _darkModeEnabled.value)
        editor.putBoolean("notifications_enabled", _notificationsEnabled.value)
        editor.putBoolean("auto_sort_enabled", _autoSortEnabled.value)
        _defaultSourceDirectory.value?.let { editor.putString("default_source_directory", it) }
        _defaultDestinationDirectory.value?.let { editor.putString("default_destination_directory", it) }
        
        editor.apply()
    }

    /**
     * Toggles dark mode.
     */
    fun toggleDarkMode() {
        _darkModeEnabled.update { !it }
        viewModelScope.launch {
            saveSettings()
        }
    }

    /**
     * Toggles notifications.
     */
    fun toggleNotifications() {
        _notificationsEnabled.update { !it }
        viewModelScope.launch {
            saveSettings()
        }
    }

    /**
     * Toggles auto sort.
     */
    fun toggleAutoSort() {
        _autoSortEnabled.update { !it }
        viewModelScope.launch {
            saveSettings()
        }
    }

    /**
     * Sets the default source directory.
     *
     * @param directory The directory path
     */
    fun setDefaultSourceDirectory(directory: String?) {
        _defaultSourceDirectory.value = directory
        viewModelScope.launch {
            saveSettings()
        }
    }

    /**
     * Sets the default destination directory.
     *
     * @param directory The directory path
     */
    fun setDefaultDestinationDirectory(directory: String?) {
        _defaultDestinationDirectory.value = directory
        viewModelScope.launch {
            saveSettings()
        }
    }

    /**
     * Resets all settings to default values.
     */
    fun resetSettings() {
        _darkModeEnabled.value = false
        _notificationsEnabled.value = true
        _autoSortEnabled.value = false
        _defaultSourceDirectory.value = null
        _defaultDestinationDirectory.value = null
        
        viewModelScope.launch {
            saveSettings()
        }
    }
}