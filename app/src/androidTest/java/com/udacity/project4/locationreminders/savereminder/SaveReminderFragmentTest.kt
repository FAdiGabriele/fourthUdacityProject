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
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.util.PermissionOptions
import com.udacity.project4.util.isLocationEnabled
import com.udacity.project4.util.grantPermissionsIfRequested
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito
import org.mockito.Mockito.mock


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest //UI Testing
class SaveReminderFragmentTest : AutoCloseKoinTest() {


    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        repository = FakeDataSource()

        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {

//            viewModel { RemindersListViewModel(appContext, repository) }

            single { SaveReminderViewModel(appContext, repository) }
//            single { CommonViewModel(appContext, repository) }


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
        val scenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle(),
            R.style.DifferentTheme
        )

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
        val scenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle(),
            R.style.DifferentTheme
        )

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
        val scenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle(),
            R.style.DifferentTheme
        )

        val navController = mock(NavController::class.java)
        lateinit var fragment : SaveReminderFragment
        //WHEN  we insert the data
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
            fragment = it
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
        grantPermissionsIfRequested(PermissionOptions.FOREGROUND_AND_BACKGROUND)

        if(isLocationEnabled(fragment.requireContext())){
            Thread.sleep(1000)
            Mockito.verify(navController).popBackStack()
        }

    }

    @Test
    fun ifYouFillImportantField_ShowToast(){

        //GIVEN data to insert
        val reminderToSave = ReminderDataItem("TITLE1", "description", "LOCATION1", 1.0, 1.0, "id1")


        //GIVEN the SaveReminderFragment
        val scenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle(),
            R.style.DifferentTheme
        )

        val navController = mock(NavController::class.java)
        lateinit var fragment : SaveReminderFragment
        //WHEN  we insert the data
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
            fragment = it
            //faking insert of value from SelectLocationFragment
            it._viewModel.reminderSelectedLocationStr.value = reminderToSave.location
            it._viewModel.latitude.value = reminderToSave.latitude
            it._viewModel.longitude.value = reminderToSave.longitude

        }

        onView(withId(R.id.reminderTitle)).perform(replaceText(reminderToSave.title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(reminderToSave.description))

        //WHEN click on Fab
        onView(withId(R.id.saveReminder)).perform(click())

        //WHEN we grant permission if requested
        grantPermissionsIfRequested(PermissionOptions.FOREGROUND_AND_BACKGROUND)


        if(isLocationEnabled(fragment.requireContext())){
            //THEN appears a Toast that warn that geofence is entered
            onView(withText(R.string.geofence_entered))
                .inRoot(withDecorView(not(fragment.requireActivity().window.decorView)))
                .check(matches(isDisplayed()))

            Thread.sleep(4000)

            //THEN appears a Toast that warn that reminder is saved
            onView(withText(R.string.reminder_saved))
                .inRoot(withDecorView(not(fragment.requireActivity().window.decorView)))
                .check(matches(isDisplayed()))
        } else {
            //THEN appears a Toast that warn that geofence is NOT added
            onView(withText(R.string.geofences_not_added))
                .inRoot(withDecorView(not(fragment.requireActivity().window.decorView)))
                .check(matches(isDisplayed()))
        }
    }



}
