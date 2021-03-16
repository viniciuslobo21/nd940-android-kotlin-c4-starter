package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersMockList
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.RecyclerViewItemCountAssertion
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun setUp() {
        stopKoin()

        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            modules(listOf(myModule))
        }

        initRepository()

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }


    private fun initRepository() {
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun reminderListScreen_click_fabButton_verify_navigateToSaveReminder() {
        // GIVEN - on the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the save screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun reminderListScreen_verify_noData() {
        // GIVEN - on the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // THEN - Verify that have some texts on the screen
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderssRecyclerView)).check(RecyclerViewItemCountAssertion(0))
    }

    @Test
    fun reminderListScreen_verify_item_is_on_screen() {
        runBlocking {
            // GIVEN - on the reminder list screen
            val reminder = RemindersMockList.list[0]
            repository.saveReminder(reminder)

            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)

            // WHEN - Click on the first item
            onView(withId(R.id.reminderssRecyclerView)).check(RecyclerViewItemCountAssertion(1))

            // THEN - Verify if title matchs
            onView(withText(reminder.title)).check(matches(isDisplayed()))
        }
    }
}