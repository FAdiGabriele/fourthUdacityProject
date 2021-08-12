package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.CommonViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito
import org.mockito.Mockito.mock


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest //UI Testing
class SaveReminderFragmentTest : KoinTest {


    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @get:Rule
    var activityTestRule: ActivityTestRule<RemindersActivity> = ActivityTestRule(RemindersActivity::class.java)


    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            single {
                SaveReminderViewModel(
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
    fun iFYouNotFillImportantField_ShowError(){

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.DifferentTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText(R.string.err_enter_title)).inRoot(withDecorView(not(activityTestRule.activity.window.decorView))).check(matches(isDisplayed()))
    }

    @Test
    fun ifYouFillImportantField_NavigateToReminderListFragment(){

        val reminderToSave = ReminderDataItem("TITLE1", "description", "LOCATION1", 1.0, 1.0, "id1")


        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.DifferentTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)

            //faking insert of value from SelectLocationFragment
            it._viewModel.reminderSelectedLocationStr.value = reminderToSave.location
            it._viewModel.latitude.value = reminderToSave.latitude
            it._viewModel.longitude.value = reminderToSave.longitude
        }

        onView(withId(R.id.reminderTitle)).perform(replaceText(reminderToSave.title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(reminderToSave.description))

        onView(withId(R.id.saveReminder)).perform(click())

        Mockito.verify(navController).popBackStack()
    }



}