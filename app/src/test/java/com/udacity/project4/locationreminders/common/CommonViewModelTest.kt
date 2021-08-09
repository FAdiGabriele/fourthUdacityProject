package com.udacity.project4.locationreminders.common

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.CommonViewModel
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeReminderRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
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

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class CommonViewModelTest : AutoCloseKoinTest() {

    // Use a fake repository to be injected into the viewmodel
    lateinit var reminderRepository: FakeReminderRepository


    // Subject under test
    private val commonViewModel: CommonViewModel by inject()

    private val module : Module =  module {
        single{
            CommonViewModel(
                reminderRepository
            )
        }
    }


    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupReminderRepository() {
        // Initialise the repository with some fake reminders.
        reminderRepository = FakeReminderRepository()
        reminderRepository.addSomeFakeData()

        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(module)
        }
    }


    @Test
    fun removeReminderData() {

        commonViewModel.removeReminderData(
                ReminderDataItem.getFromReminderDTO(reminderRepository.reminderList[1])
        )

        val newRemindersListSize = reminderRepository.reminderList.size

        MatcherAssert.assertThat(newRemindersListSize, IsEqual(1))
    }

    @Test
    fun createGeoFencingRequest(){

        val newReminderDataItem = ReminderDataItem("casual_title", "casual_description", "casual_location", 2.0, 2.0, "casual_id")

        commonViewModel.createGeoFenceRequest(newReminderDataItem)

       MatcherAssert.assertThat(commonViewModel.geoFencerObserver.value?.geofences?.size, IsEqual(1))
    }

}