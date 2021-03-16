package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.RemindersMockList
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import junit.framework.Assert.assertEquals

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var context: Application


    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        context = ApplicationProvider.getApplicationContext()

        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun saveReminder_checking_loading() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        viewModel.saveReminder(RemindersMockList.reminderItem)

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


    @Test
    fun saveReminder_checking_success() {

        viewModel.saveReminder(RemindersMockList.reminderItem)

        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertEquals(viewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }


    @Test
    fun onClear_checking_success() {

        viewModel.latitude.value = RemindersMockList.reminderItem.latitude
        viewModel.longitude.value = RemindersMockList.reminderItem.longitude
        viewModel.reminderDescription.value = RemindersMockList.reminderItem.description
        viewModel.reminderSelectedLocationStr.value = RemindersMockList.reminderItem.location
        viewModel.reminderTitle.value = RemindersMockList.reminderItem.title

        viewModel.onClear()

        assertThat(viewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertEquals(viewModel.reminderDescription.getOrAwaitValue(), null)
        assertEquals(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), null)
        assertEquals(viewModel.selectedPOI.getOrAwaitValue(), null)
        assertEquals(viewModel.latitude.getOrAwaitValue(), null)
        assertEquals(viewModel.longitude.getOrAwaitValue(), null)
    }

    @Test
    fun validateAndSaveReminder_vchecking_success() {

        val item = RemindersMockList.reminderItem.copy(
            title = "TITLE3",
            description = "DESCRIPTION3",
            latitude = -18.92149970366 ,
            location = "LOCATION3" ,
            longitude = -18.92149970366,
            id = "IE3"
        )


        viewModel.validateAndSaveReminder(item)

        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertEquals(viewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @Test
    fun validateAndSaveReminder_checking_Error() {
        val reminderData = RemindersMockList.reminderItem.copy(
            title = "TITLE3",
            description = "DESCRIPTION3",
            latitude = -18.92149970366 ,
            location = "LOCATION3" ,
            longitude = -18.92149970366,
            id = "IE3"
        )
        reminderData.title = null
        viewModel.validateAndSaveReminder(reminderData)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_checking_titleErro() {
        val reminderData = RemindersMockList.reminderItem
        reminderData.title = null
        viewModel.validateEnteredData(reminderData)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_checking_locationError() {
        val reminderData = RemindersMockList.reminderItem.copy(
            title = "TITLE3",
            description = "DESCRIPTION3",
            latitude = -18.92149970366 ,
            location = "LOCATION3" ,
            longitude = -18.92149970366,
            id = "IE3"
        )
        reminderData.location = null
        viewModel.validateEnteredData(reminderData)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }
}
