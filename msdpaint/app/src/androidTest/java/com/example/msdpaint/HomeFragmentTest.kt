package com.example.msdpaint

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.msdpaint.fragments.HomeFragment
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeFragmentTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    // Nav
    private lateinit var navController: TestNavHostController

    @Before
    fun setup(){

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Launch HomeFragment in the test
        rule.activityRule.scenario.onActivity { activity ->
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.homeFragment)  // Start at HomeFragment

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, HomeFragment())
                .commitNow()

            // Set the NavController for the fragment
            Navigation.setViewNavController(
                activity.findViewById(R.id.fragmentContainerView),
                navController
            )
        }
    }

    @Test
    fun splashScreenDisplayed()
    {
        // Splash screen populates
        rule.onNodeWithContentDescription("splash screen image").assertIsDisplayed()

        // Advance clock and check splash screen is not displayed
        rule.mainClock.advanceTimeBy(2500)
        rule.onNodeWithContentDescription("splash screen image").assertDoesNotExist()

        // Transition to home fragment
        onView(withId(R.id.composeView)).check(matches(isDisplayed()))
    }

    @Test
    fun composableDisplayed()
    {
        // Advance clock to get past splash screen
        rule.mainClock.advanceTimeBy(2500)

        // Draw button is displayed
        rule.onNodeWithText("New Doodle").assertExists()

        // Icon is displayed
        rule.onNodeWithContentDescription("home screen logo").assertIsDisplayed()

        // Home Fragment is displayed
        onView(withId(R.id.composeView)).check(matches(isDisplayed()))
        navController.currentDestination?.id == R.id.homeFragment
    }
}