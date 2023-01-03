package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlocking {
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        repository.saveReminder(reminder)

        val loaded = repository.getReminder(reminder.id)

        assertThat(loaded is Result.Success, `is`(true))
        loaded as Result.Success
        assertThat(loaded.data.id, `is`(reminder.id))
        assertThat(loaded.data.title, `is`(reminder.title))
        assertThat(loaded.data.description, `is`(reminder.description))
        assertThat(loaded.data.location, `is`(reminder.location))
        assertThat(loaded.data.latitude, `is`(reminder.latitude))
        assertThat(loaded.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        repository.saveReminder(reminder)

        repository.deleteAllReminders()

        val loaded = repository.getReminder(reminder.id)

        assertThat(loaded is Result.Error, `is`(true))
        loaded as Result.Error
        assertThat(loaded.message, `is`("Reminder not found!"))
    }

    @Test
    fun getReminders() = runBlocking {
        val reminder1 = ReminderDTO("title1", "description1", "location1", 0.0, 0.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 0.0, 0.0)
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val loaded = repository.getReminders()

        assertThat(loaded is Result.Success, `is`(true))
        loaded as Result.Success
        assertThat(loaded.data.size, `is`(2))
        assertThat(loaded.data[0].id, `is`(reminder1.id))
        assertThat(loaded.data[0].title, `is`(reminder1.title))
        assertThat(loaded.data[0].description, `is`(reminder1.description))
        assertThat(loaded.data[0].location, `is`(reminder1.location))
        assertThat(loaded.data[0].latitude, `is`(reminder1.latitude))
        assertThat(loaded.data[0].longitude, `is`(reminder1.longitude))
        assertThat(loaded.data[1].id, `is`(reminder2.id))
        assertThat(loaded.data[1].title, `is`(reminder2.title))
        assertThat(loaded.data[1].description, `is`(reminder2.description))
        assertThat(loaded.data[1].location, `is`(reminder2.location))
        assertThat(loaded.data[1].latitude, `is`(reminder2.latitude))
        assertThat(loaded.data[1].longitude, `is`(reminder2.longitude))
    }
}