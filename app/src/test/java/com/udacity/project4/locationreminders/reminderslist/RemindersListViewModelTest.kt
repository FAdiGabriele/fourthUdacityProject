package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @Test
    fun nullIsNull(){
        val value = null

        val valueString = value.toString()

        Log.e("TEST", "$value = $valueString")
        assertTrue(true)
    }
}