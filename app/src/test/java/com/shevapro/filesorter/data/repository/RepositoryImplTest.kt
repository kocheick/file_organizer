package com.shevapro.filesorter.data.repository

import com.shevapro.filesorter.data.database.TaskDao
import com.shevapro.filesorter.model.TaskRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RepositoryImplTest {

    private lateinit var taskDao: TaskDao
    private lateinit var repository: RepositoryImpl

    @Before
    fun setup() {
        taskDao = mockk(relaxed = true)
        repository = RepositoryImpl(taskDao)
    }

    @Test
    fun `addTask should call insert on taskDao`() = runTest {
        // Given
        val taskRecord = TaskRecord("pdf", "source/uri", "destination/uri", true, false, null, com.shevapro.filesorter.model.ScheduleType.ONCE, 1)

        // When
        repository.addTask(taskRecord)

        // Then
        coVerify { taskDao.insert(taskRecord) }
    }

    @Test
    fun `updateTask should call update on taskDao`() = runTest {
        // Given
        val taskRecord = TaskRecord("pdf", "source/uri", "destination/uri", true, false, null, com.shevapro.filesorter.model.ScheduleType.ONCE, 1)

        // When
        repository.updateTask(taskRecord)

        // Then
        coVerify { taskDao.update(taskRecord) }
    }

    @Test
    fun `deleteTask should call delete on taskDao`() = runTest {
        // Given
        val taskRecord = TaskRecord("pdf", "source/uri", "destination/uri", true, false, null, com.shevapro.filesorter.model.ScheduleType.ONCE, 1)

        // When
        repository.deleteTask(taskRecord)

        // Then
        coVerify { taskDao.delete(taskRecord) }
    }

    @Test
    fun `getTask should call getById on taskDao`() {
        // Given
        val taskRecord = TaskRecord("pdf", "source/uri", "destination/uri", true, false, null, com.shevapro.filesorter.model.ScheduleType.ONCE, 1)
        every { taskDao.getById(1) } returns taskRecord

        // When
        val result = repository.getTask(taskRecord)

        // Then
        verify { taskDao.getById(1) }
        assertEquals(taskRecord, result)
    }

    @Test
    fun `getTaskById should call getById on taskDao`() = runTest {
        // Given
        val taskId = 1
        val taskRecord = TaskRecord("pdf", "source/uri", "destination/uri", true, false, null, com.shevapro.filesorter.model.ScheduleType.ONCE, taskId)
        every { taskDao.getById(taskId) } returns taskRecord

        // When
        val result = repository.getTaskbyId(taskId)

        // Then
        verify { taskDao.getById(taskId) }
        assertEquals(taskRecord, result)
    }

    @Test
    fun `getTasks should call getAll on taskDao`() = runTest {
        // Given
        val tasksList = listOf(
            TaskRecord("pdf", "source/uri1", "destination/uri1", true, false, null, com.shevapro.filesorter.model.ScheduleType.ONCE, 1),
            TaskRecord("jpg", "source/uri2", "destination/uri2", false, false, null, com.shevapro.filesorter.model.ScheduleType.ONCE, 2)
        )
        val tasksFlow = flowOf(tasksList)
        every { taskDao.getAll() } returns tasksFlow

        // When
        val result = repository.getTasks()

        // Then
        verify { taskDao.getAll() }
        assertEquals(tasksFlow, result)
    }

    @Test
    fun `deleteAll should call deleteAllTodos on taskDao`() = runTest {
        // When
        repository.deleteAll()

        // Then
        coVerify { taskDao.deleteAllTodos() }
    }
}
