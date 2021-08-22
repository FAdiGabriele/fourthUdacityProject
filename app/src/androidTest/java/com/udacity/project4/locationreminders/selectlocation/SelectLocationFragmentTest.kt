package com.udacity.project4.locationreminders.selectlocation

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.CommonViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import com.udacity.project4.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.only

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest //UI Testing
class SelectLocationFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        repository = FakeDataSource()

        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {

            single { SaveReminderViewModel(appContext, repository) }
            single { CommonViewModel(appContext, repository) }

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
    fun getToastError_IfGPSIsNotEnabled_else_clickOnMap(){

        //GIVEN the  SelectLocationFragment
        val scenario = launchFragmentInContainer<SelectLocationFragment>(
            Bundle(),
            R.style.DifferentTheme
        )

        lateinit var fragment : SelectLocationFragment
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
            fragment = it
        }


        //WHEN if it asks for permission and we don't allow it
            if(grantPermissionsIfRequested(PermissionOptions.NONE)) {

                //THEN A toast appears for remember us that without GPS application can't find user position
                Espresso.onView(ViewMatchers.withText(R.string.location_permission_not_granted))
                    .inRoot(RootMatchers.withDecorView(CoreMatchers.not(fragment.requireActivity().window.decorView)))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            }else{
                if(!isLocationEnabled(fragment.requireActivity())) turnOnPositionIfRequested(LocationsOptions.TURN_ON)

                //else
                //THEN checking if is visible map on Layout
                Espresso.onView(ViewMatchers.withId(R.id.map)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

                //WHEN we click on map
                Espresso.onView(ViewMatchers.withId(R.id.map)).perform(ViewActions.click())
            }

    }
}