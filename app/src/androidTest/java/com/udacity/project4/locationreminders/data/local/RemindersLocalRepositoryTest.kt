package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase


    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    @Test
    fun insertTaskAndGetById() = runBlocking {
        // GIVEN - Insert a task.,
        val reminder = ReminderDTO("title", "description", "location",1.0, 1.0, "id")
        localDataSource.saveReminder(reminder)

        // WHEN - Get the task by id from the database.
        val loaded = localDataSource.getReminder(reminder.id) as Result.Success<ReminderDTO>

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded.data , CoreMatchers.notNullValue())
        assertThat(loaded.data.id, `is`(reminder.id))
        assertThat(loaded.data.title, `is`(reminder.title))
        assertThat(loaded.data.description, `is`(reminder.description))
        assertThat(loaded.data.location, `is`(reminder.location))
        assertThat(loaded.data.latitude, `is`(reminder.latitude))
        assertThat(loaded.data.longitude, `is`(reminder.longitude))

    }

    @Test
    fun updateTaskAndGetById() = runBlocking {
        // GIVEN - Insert a task.
        val reminder = ReminderDTO("title", "description", "location",1.0, 1.0, "unique_id")
        localDataSource.saveReminder(reminder)

        // WHEN - Iserted a Task with the same ID
        val reminder2 = ReminderDTO("title2", "description2","location2",1.0, 1.0, "unique_id")
        localDataSource.saveReminder(reminder2)

        // THEN - The loaded data contains the updated values.
        val loaded = localDataSource.getReminder(reminder.id) as Result.Success<ReminderDTO>
        assertThat<ReminderDTO>(loaded.data , CoreMatchers.notNullValue())
        assertThat(loaded.data.id, `is`(reminder2.id))
        assertThat(loaded.data.title, `is`(reminder2.title))
        assertThat(loaded.data.description, `is`(reminder2.description))
        assertThat(loaded.data.location, `is`(reminder2.location))
        assertThat(loaded.data.latitude, `is`(reminder2.latitude))
        assertThat(loaded.data.longitude, `is`(reminder2.longitude))
    }

}