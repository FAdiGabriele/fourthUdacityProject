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
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
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

        repository = FakeDataSource()

        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {

            viewModel { RemindersListViewModel(appContext, repository) }

            viewModel { SaveReminderViewModel(appContext, repository) }
            single { CommonViewModel(repository) }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }

        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }


    }


    @Test
    fun iFYouNotInsertTheName_ShowError(){

        //GIVEN the SaveReminderFragment
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.DifferentTheme)

        //WHEN we do NOT insert the data
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //WHEN click on Fab
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN appears a Snackbar that suggest to fill the mandatory field
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))
//        onView(withText(R.string.err_enter_title)).inRoot(withDecorView(not(activityTestRule.activity.window.decorView))).check(matches(isDisplayed()))
    }

    @Test
    fun iFYouNotInsertTheLocation_ShowError(){

        //GIVEN data to insert
        val titleToInsert = "TITLE1"

        //GIVEN the SaveReminderFragment
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.DifferentTheme)

        //WHEN we do NOT insert the location
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //WHEN we insert the title
        onView(withId(R.id.reminderTitle)).perform(replaceText(titleToInsert))

        //WHEN click on Fab
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN appears a Snackbar that suggest to select a location
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))
//        onView(withText(R.string.err_select_location)).inRoot(withDecorView(not(activityTestRule.activity.window.decorView))).check(matches(isDisplayed()))
    }

    @Test
    fun ifYouFillImportantField_NavigateToReminderListFragment(){

        //GIVEN data to insert
        val reminderToSave = ReminderDataItem("TITLE1", "description", "LOCATION1", 1.0, 1.0, "id1")


        //GIVEN the SaveReminderFragment
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.DifferentTheme)

        val navController = mock(NavController::class.java)

        //WHEN  we insert the data
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)

            //faking insert of value from SelectLocationFragment
            it._viewModel.reminderSelectedLocationStr.value = reminderToSave.location
            it._viewModel.latitude.value = reminderToSave.latitude
            it._viewModel.longitude.value = reminderToSave.longitude
        }

        onView(withId(R.id.reminderTitle)).perform(replaceText(reminderToSave.title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(reminderToSave.description))

        //WHEN click on Fab
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN it navigate back to ReminderListFragment
        Mockito.verify(navController).popBackStack()
    }



}