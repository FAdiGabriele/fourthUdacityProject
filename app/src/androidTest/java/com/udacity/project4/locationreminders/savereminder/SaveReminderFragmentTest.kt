package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.mockito.Mockito
import org.mockito.Mockito.mock


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest //UI Testing
class SaveReminderFragmentTest : KoinTest {


    @get:Rule
    var activityTestRule: ActivityTestRule<RemindersActivity> = ActivityTestRule(RemindersActivity::class.java)


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

}