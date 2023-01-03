package com.udacity.project4

import android.app.Application
import android.os.Bundle
import android.view.View.OnLongClickListener
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
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

    @Test
    fun saveReminder() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        //click on the FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        //enter the title
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))

        //enter the description
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))

        //enter the location
        onView(withId(R.id.selectLocation)).perform(click())

        //click on the map
        onView(withId(R.id.map)).perform(click())

        //click on the save button
        onView(withId(R.id.save_location_btn)).perform(click())


        activityScenario.close()


    }
    @Test
    fun failToSave(){
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // click on the FAB
        onView(withId(R.id.addReminderFAB)).perform(click())
        //enter the title
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        // enter the description
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
//       // click on the layout to remove the keyboard
//        onView(withId(R.id.fragment_save_reminder)).perform(click())
        // dismiss the keyboard
        onView(withId(R.id.fragment_save_reminder)).perform(closeSoftKeyboard())
        // click on the save button
        onView(withId(R.id.saveReminder)).perform(click())
        // check snackbar is displayed
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))
        activityScenario.close()

    }



}