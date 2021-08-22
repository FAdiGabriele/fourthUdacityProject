package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeReminderRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
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

    private val module: Module = module {
        single {
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
    fun addNewReminder_reminderSaved() = mainCoroutineRule.runBlockingTest {

        val newReminder = ReminderDataItem("casual_title", "casual_description", "casual_location", 1.0, 1.0, "casual_id")

        val oldSize = saveReminderViewModel.checkSize()

        saveReminderViewModel.saveReminder(newReminder)

        val newSize = saveReminderViewModel.checkSize()

        MatcherAssert.assertThat(newSize, IsEqual(oldSize + 1))
    }

    @Test
    fun createGeoFencingRequest() {

        val newReminderDataItem = ReminderDataItem(
            "casual_title",
            "casual_description",
            "casual_location",
            2.0,
            2.0,
            "casual_id"
        )

        saveReminderViewModel.createGeoFenceRequest(newReminderDataItem)

        MatcherAssert.assertThat(saveReminderViewModel.geoFenceToAdd, IsNot(IsNull()))
    }

    @Test
    fun saveReminder_showLoading() {
        val newReminder = ReminderDataItem("titolo0", "descrizione0", "luogo0", 1.0, 1.0, "0")

        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Load the task in the view model.
        saveReminderViewModel.saveReminder(newReminder)

        // Then assert that the progress indicator is shown.
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is hidden.
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(), Is.`is`(false)
        )
    }

    @Test
    fun createGeoFencing_geofenceIsReady() {
        val newReminder = ReminderDataItem("casual_title", "casual_description", "casual_location", 1.0, 1.0, "casual_id")

        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Then assert that the progress indicator is shown.
        MatcherAssert.assertThat(saveReminderViewModel.geoFenceReady.getOrAwaitValue(), Is.`is`(false))

        // Load the task in the view model.
        saveReminderViewModel.createGeoFenceRequest(newReminder)

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is hidden.
        MatcherAssert.assertThat(
            saveReminderViewModel.geoFenceReady.getOrAwaitValue(), Is.`is`(true)
        )
    }

    @Test
    fun  saveReminder_showToast() {

        val newReminder = ReminderDataItem("casual_title", "casual_description", "casual_location", 1.0, 1.0, "casual_id")

        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

//        MatcherAssert.assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),  IsNull())

        saveReminderViewModel.saveReminder(newReminder)

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        MatcherAssert.assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),  Is.`is`("Reminder Saved !"))
    }
}