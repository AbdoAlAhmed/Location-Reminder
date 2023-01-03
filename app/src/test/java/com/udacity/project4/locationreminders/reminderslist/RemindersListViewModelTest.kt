package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineRule = CoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeRepo: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeRepo = FakeDataSource()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeRepo)
    }

    @Test
    fun loadReminders_loading() = coroutineRule.runBlockingTest {
        coroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true)
        )
        coroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminders_showNoData() = coroutineRule.runBlockingTest {
        fakeRepo.shouldReturnError = true
        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(), CoreMatchers.`is`("Fail to load data")
        )
    }


}