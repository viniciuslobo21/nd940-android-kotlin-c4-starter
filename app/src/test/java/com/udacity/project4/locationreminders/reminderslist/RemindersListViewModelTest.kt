package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.RemindersMockList
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Use a fake repository to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource
    // Subject under test
    private lateinit var viewModel: RemindersListViewModel

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        stopKoin()
        val applicationMock = Mockito.mock(Application::class.java)
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(applicationMock, dataSource)
    }

    @Test
    fun loadReminders_AndVerify_ShowLoading() = mainCoroutineRule.runBlockingTest {

        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_AndVerify_Success() = mainCoroutineRule.runBlockingTest {

        dataSource.setReturnError(false)
        RemindersMockList.list.forEach {reminderDTO ->
            dataSource.saveReminder(reminderDTO)
        }

        viewModel.loadReminders()

        assertThat(viewModel.remindersList.getOrAwaitValue().size, `is`(RemindersMockList.list.size))
    }

    @Test
    fun loadReminders_AndVerify_Error() = mainCoroutineRule.runBlockingTest {

        dataSource.setReturnError(true)

        viewModel.loadReminders()

        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Error get all reminders"))
    }

}