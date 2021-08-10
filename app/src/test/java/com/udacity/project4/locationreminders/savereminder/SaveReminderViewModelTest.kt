package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeReminderRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest : AutoCloseKoinTest() {


    // Use a fake repository to be injected into the viewmodel
    private lateinit var reminderRepository: FakeReminderRepository

    // Subject under test
    private val saveReminderViewModel: SaveReminderViewModel by inject()

    private val module : Module =  module {
        single{
            SaveReminderViewModel(
                get(),
                reminderRepository
            )
        }
    }


    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupReminderRepository() {
        // Initialise the repository with no reminders.
        reminderRepository = FakeReminderRepository()

        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(module)
        }
    }

    @Test
    fun addNewReminder_reminderSaved() {

        val newReminder = ReminderDataItem("titolo0", "descrizione0", "luogo0", 1.0, 1.0, "0")

        saveReminderViewModel.saveReminder(newReminder)

        val newSize = saveReminderViewModel.checkSize()

        MatcherAssert.assertThat(newSize, IsEqual(1))
    }

}