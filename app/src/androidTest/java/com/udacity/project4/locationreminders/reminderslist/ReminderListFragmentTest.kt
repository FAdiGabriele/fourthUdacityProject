package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.CommonViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.atPosition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest //UI Testing
class ReminderListFragmentTest : KoinTest {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    repository
                )
            }
            single {
                CommonViewModel(
                    repository
                )

            }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        repository = FakeDataSource()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun saveDataInDatabase_dataIsVisible() = runBlockingTest {
        // GIVEN Reminder saved in DB
        repository.saveReminder(ReminderDTO("TITLE1", "DESCRIPTION1", "LOCATION1", 1.0,1.0,"id1"))
        repository.saveReminder(ReminderDTO("TITLE2", "DESCRIPTION2", "LOCATION2", 1.0,1.0,"id2"))

        // WHEN - FragmentList starts
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.DifferentTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // THEN - Verify that we can see almost one reminder
        onView(withId(R.id.reminderssRecyclerView)).check(ViewAssertions.matches(atPosition(0, withText("TITLE1"))))


    }

    @Test
    fun clickAddTaskButton_navigateToAddEditFragment() = runBlockingTest {
        repository.saveReminder(ReminderDTO("TITLE1", "DESCRIPTION1", "LOCATION1", 1.0,1.0,"id1"))
        repository.saveReminder(ReminderDTO("TITLE2", "DESCRIPTION2", "LOCATION2", 1.0,1.0,"id2"))

        // GIVEN - On the list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.DifferentTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on Fab
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the first detail screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }


}