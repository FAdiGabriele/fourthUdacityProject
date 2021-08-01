package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before

class RepositoryTest {

    //fake data
    private val reminderDTO0 = ReminderDTO("titolo", "descrizione", "luogo", 1.0, 1.0, "0")
    private val reminderDTO1 = ReminderDTO("titolo", "descrizione", "luogo", 1.0, 1.0, "1")
    private val reminderDTO2 = ReminderDTO("titolo", "descrizione", "luogo", 1.0, 1.0, "2")
    private val reminderDTO3 = ReminderDTO("titolo", "descrizione", "luogo", 1.0, 1.0, "3")
    private val reminderList = listOf(reminderDTO0, reminderDTO1, reminderDTO2, reminderDTO3)

    private lateinit var fakeLocalDataSource: FakeDataSource

    //class under test
    private lateinit var repositoryToTest: RemindersLocalRepositoryTest


    @Before
    fun createRepository() {
        fakeLocalDataSource = FakeDataSource(reminderList.toMutableList())

        repositoryToTest = RemindersLocalRepositoryTest(fakeLocalDataSource)

    }
}