package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.CommonViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            single {
                CommonViewModel(
                    get() as ReminderDataSource
                )

            }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun createOneReminder() {
        // GIVEN  the activity reminder
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // WHEN click on Fab
        Espresso.onView(withId(R.id.addReminderFAB)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //THEN It should open the SaveReminderFragment,else the test fail

        //WHEN we insert data
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("NEW TITLE"))
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("NEW DESCRIPTION"))

        //WHEN we click on Location
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())


        //THEN It should open the SelectLocationFragment,else the test fail

        //THEN checking if is visible map on Layout
        Espresso.onView(withId(R.id.map)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //WHEN we click on map
        Espresso.onView(withId(R.id.map)).perform(ViewActions.click())

        //Here we wait for map loading and update the UI
        Thread.sleep(1000)
        //THEN It should appears a button, else the test fail
        Espresso.onView(withId(R.id.button_confirm)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //WHEN click on button
        Espresso.onView(withId(R.id.button_confirm)).perform(ViewActions.click())

        //THEN It should open the SaveReminderFragment,else the test fail
        //THEN we check if SaveReminderFragment store the other values
        Espresso.onView(ViewMatchers.withText("NEW TITLE"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("NEW DESCRIPTION"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //WHEN we click on FAB
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        //THEN It should open the ReminderListFragment,else the test fail
        //THEN we check if the new reminder is created,  else the test fail
        Espresso.onView(ViewMatchers.withText("NEW TITLE"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("NEW DESCRIPTION"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }

}
