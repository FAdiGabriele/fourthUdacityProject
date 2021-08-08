package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.isA
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DefaultReminderRepositoryTest {

    //fake data
    private val reminderDTO0 = ReminderDTO("title0", "description0", "place0", 1.0, 1.0, "0")
    private val reminderDTO1 = ReminderDTO("title1", "description1", "place1", 1.0, 1.0, "1")
    private val reminderDTO2 = ReminderDTO("title2", "description2", "place2", 1.0, 1.0, "2")
    private val reminderDTO3 = ReminderDTO("title3", "description3", "place3", 1.0, 1.0, "3")
    private val reminderList = listOf(reminderDTO0, reminderDTO1, reminderDTO2, reminderDTO3)

    private lateinit var fakeLocalDataSource: FakeDataSource

    //class under test
    private lateinit var repositoryToTest: DefaultReminderRepository


    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun createRepository() {
        fakeLocalDataSource = FakeDataSource(reminderList.toMutableList())

        repositoryToTest = DefaultReminderRepository(fakeLocalDataSource,Dispatchers.Unconfined)

    }

    @ExperimentalCoroutinesApi
    @Test
    fun getReminder_requestsAllReminders() = mainCoroutineRule.runBlockingTest {

        val reminders = repositoryToTest.getReminders() as Result.Success<List<ReminderDTO>>

        //
        MatcherAssert.assertThat(reminders.data, IsEqual(reminderList))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getTitleReminderFromID_requestsTitleFromID() = mainCoroutineRule.runBlockingTest {

        val reminder = repositoryToTest.getReminderById("0") as Result.Success<ReminderDTO>

        MatcherAssert.assertThat(reminder.data, IsEqual(reminderDTO0))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveReminder_isSuccessifulInsertedInDB() = mainCoroutineRule.runBlockingTest{

        val reminderDTO4 = ReminderDTO("titolo4", "descrizione4", "luogo4", 1.0, 1.0, "4")

        repositoryToTest.saveReminder(reminderDTO4)

        MatcherAssert.assertThat(reminderDTO4, IsEqual(fakeLocalDataSource.reminderList?.get(4)))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun deleteReminder_isSuccessifulDeletedInDB() = mainCoroutineRule.runBlockingTest{

        repositoryToTest.deleteReminder(reminderDTO3)

        MatcherAssert.assertThat(fakeLocalDataSource.reminderList?.size, CoreMatchers.`is`(3))
    }
}