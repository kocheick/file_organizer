package com.shevapro.filesorter.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.shevapro.filesorter.model.UITaskRecord
import com.shevapro.filesorter.model.UiState
import com.shevapro.filesorter.ui.screens.MainScreen
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addNewTask_opensAddTaskDialog() {
        // Given
        val viewModel = mockk<MainViewModel>(relaxed = true)
        val uiState = UiState(
            isLoading = false,
            tasks = emptyList(),
            showAddDialog = false,
            showEditDialog = false,
            showDeleteDialog = false,
            showExecuteDialog = false,
            showStatsDialog = false,
            showErrorDialog = false,
            errorMessage = "",
            currentTask = null
        )

        every { viewModel.uiState } returns MutableStateFlow(uiState)

        // When
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel)
        }

        // Then
        // Verify that the add button is displayed
        composeTestRule.onNodeWithContentDescription("Add new task").assertIsDisplayed()

        // Click the add button
        composeTestRule.onNodeWithContentDescription("Add new task").performClick()

        // Verify that the addTask method was called on the viewModel
        verify { viewModel.showAddDialog() }
    }

    @Test
    fun executeTasksButton_isDisplayedWhenTasksExist() {
        // Given
        val viewModel = mockk<MainViewModel>(relaxed = true)
        val tasks = listOf(
            UITaskRecord(
                extension = "pdf",
                source = "source/path",
                destination = "destination/path",
                isActive = true,
                errorMessage = null,
                id = 1
            )
        )

        val uiState = UiState(
            isLoading = false,
            tasks = tasks,
            showAddDialog = false,
            showEditDialog = false,
            showDeleteDialog = false,
            showExecuteDialog = false,
            showStatsDialog = false,
            showErrorDialog = false,
            errorMessage = "",
            currentTask = null
        )

        every { viewModel.uiState } returns MutableStateFlow(uiState)

        // When
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel)
        }

        // Then
        // Verify that the execute button is displayed
        composeTestRule.onNodeWithContentDescription("Execute tasks").assertIsDisplayed()

        // Click the execute button
        composeTestRule.onNodeWithContentDescription("Execute tasks").performClick()

        // Verify that the showExecuteDialog method was called on the viewModel
        verify { viewModel.showExecuteDialog() }
    }

    @Test
    fun taskList_displaysTaskItems() {
        // Given
        val viewModel = mockk<MainViewModel>(relaxed = true)
        val tasks = listOf(
            UITaskRecord(
                extension = "pdf",
                source = "source/path",
                destination = "destination/path",
                isActive = true,
                errorMessage = null,
                id = 1
            ),
            UITaskRecord(
                extension = "jpg",
                source = "source/path2",
                destination = "destination/path2",
                isActive = false,
                errorMessage = null,
                id = 2
            )
        )

        val uiState = UiState(
            isLoading = false,
            tasks = tasks,
            showAddDialog = false,
            showEditDialog = false,
            showDeleteDialog = false,
            showExecuteDialog = false,
            showStatsDialog = false,
            showErrorDialog = false,
            errorMessage = "",
            currentTask = null
        )

        every { viewModel.uiState } returns MutableStateFlow(uiState)

        // When
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel)
        }

        // Then
        // Verify that the task items are displayed
        composeTestRule.onNodeWithText("PDF").assertIsDisplayed()
        composeTestRule.onNodeWithText("JPG").assertIsDisplayed()
    }
}