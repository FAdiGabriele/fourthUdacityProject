package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.util.atPosition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest //UI Testing
class ReminderListFragmentTest {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private lateinit var dataSource: ReminderDataSource

    @Before
    fun initDb() {
        dataSource = FakeDataSource()
    }

    @Test
    fun clickAddTaskButton_navigateToAddEditFragment() = runBlockingTest {
        dataSource.saveReminder(ReminderDTO("TITLE1", "DESCRIPTION1", "LOCATION1", 1.0,1.0,"id1"))
        dataSource.saveReminder(ReminderDTO("TITLE2", "DESCRIPTION2", "LOCATION2", 1.0,1.0,"id2"))

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

    @Test
    fun papope() = runBlockingTest {
        dataSource.saveReminder(ReminderDTO("TITLE1", "DESCRIPTION1", "LOCATION1", 1.0,1.0,"id1"))
        dataSource.saveReminder(ReminderDTO("TITLE2", "DESCRIPTION2", "LOCATION2", 1.0,1.0,"id2"))

        // GIVEN - On the list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.DifferentTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderssRecyclerView)).check(ViewAssertions.matches(atPosition(0, withText("TITLE1"))))


    }

}