package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeReminderRepository
import com.udacity.project4.locationreminders.data.ReminderDataSource
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
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : AutoCloseKoinTest() {

    // Use a fake repository to be injected into the viewmodel
    lateinit var reminderRepository: FakeReminderRepository

    // Subject under test
    private val remindersViewModel: RemindersListViewModel by inject()

    private val module : Module =  module {
        single{
            RemindersListViewModel(
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
    fun getReminderList() {

        remindersViewModel.loadReminders()

        MatcherAssert.assertThat(remindersViewModel.remindersList.value, IsEqual(convertRepositoryTest()))
    }

    fun convertRepositoryTest() : List<ReminderDataItem>{
        return listOf(
                ReminderDataItem.getFromReminderDTO(reminderRepository.reminderList[0]),
                ReminderDataItem.getFromReminderDTO(reminderRepository.reminderList[1]))
    }
}