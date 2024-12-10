//package com.example.msdpaint
//
//import android.graphics.Bitmap
//import android.graphics.Color
//import android.util.Log
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.Navigation
//import androidx.navigation.testing.TestNavHostController
//import androidx.test.core.app.ActivityScenario
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions.click
//import androidx.test.espresso.action.ViewActions.doubleClick
//import androidx.test.espresso.action.ViewActions.swipeLeft
//import androidx.test.espresso.action.ViewActions.swipeRight
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
//import androidx.test.espresso.matcher.ViewMatchers.withId
//import androidx.test.espresso.matcher.ViewMatchers.withText
//import androidx.test.ext.junit.rules.ActivityScenarioRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.filters.LargeTest
//import com.example.msdpaint.fragments.StudioFragment
//import com.example.msdpaint.viewmodels.StudioViewModel
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//@LargeTest
//class SecondFragmentTest {
//
//    private lateinit var scenario: ActivityScenario<MainActivity>
//    private lateinit var viewModel: StudioViewModel
//    private lateinit var navController: TestNavHostController
//
//    @get:Rule
//    val activityRule = ActivityScenarioRule(MainActivity::class.java)
//
//    @Before
//    fun setup() {
//        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
//        scenario = activityRule.scenario
//        scenario.onActivity { activity ->
//
//            navController.setGraph(R.navigation.nav_graph)
//            navController.setCurrentDestination(R.id.studioFragment)
//            // Route to Studio Fragment
//            activity.supportFragmentManager.beginTransaction()
//                .replace(R.id.fragmentContainerView, StudioFragment())
//                .commitNow()
//
//            Navigation.setViewNavController(
//                activity.findViewById(R.id.fragmentContainerView),
//                navController
//            )
//            // Retrieve the StudioViewModel from the activity scope
//            viewModel = ViewModelProvider(activity)[StudioViewModel::class.java]
//        }
//    }
//
//    @Test
//    fun interactionWithViews() {
//        // Move the fragment to the RESUMED state
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
////        // Test interaction with the shape button
////        onView(withId(R.id.shapeButton)).perform(click())
////        onView(withText("PEN")).check(matches(isDisplayed()))
////        onView(withText("Circle")).check(matches(isDisplayed()))
////        onView(withText("Rectangle")).check(matches(isDisplayed()))
////
////        // Change shape to Circle and check ViewModel
////        onView(withText("Circle")).perform(doubleClick())
////        assertEquals("CIRCLE", viewModel.brushShape.value)
////
////        // Change shape to Rectangle
////        onView(withId(R.id.shapeButton)).perform(click())
////        onView(withText("Rectangle")).perform(doubleClick())
//////        Log.d("SecondFragmentTest", "After Rectangle: ${viewModel.getBrushShape()}")
////        assertEquals("RECT", viewModel.brushShape.value)
////
////        // Change shape to Pen
////        onView(withId(R.id.shapeButton)).perform(click())
////        onView(withText("PEN")).perform(doubleClick())
//////        Log.d("SecondFragmentTest", "After Pen: ${viewModel.getBrushShape()}")
////        assertEquals("PATH", viewModel.brushShape.value)
//
//        // Test interaction with the color button
//        onView(withId(R.id.colorButton)).perform(click())
//        onView(withText("Color")).check(matches(isDisplayed()))
//        onView(withText("CANCEL")).check(matches(isDisplayed()))
//        onView(withText("OK")).check(matches(isDisplayed()))
//        onView(withText("CANCEL")).perform(click())
//
//        // Simulate interactions with the seekBar and the canvas (paper)
//        onView(withId(R.id.brushSizeButton)).perform(click())
////        onView(withText("Brush Size")).check(matches(isDisplayed()))
////        onView(withText("CANCEL")).check(matches(isDisplayed()))
////        onView(withText("OK")).check(matches(isDisplayed()))
//        onView(withId(R.id.seekBarVertical)).check(matches(isDisplayed()))
//        onView(withId(R.id.seekBarVertical)).perform(click())
//
//        // Max out brush thickness
//        onView(withId(R.id.seekBarVertical)).perform(swipeRight())
//        onView(withText("OK")).perform(click())
//        onView(withId(R.id.paper)).perform(swipeRight())
//
//        assertEquals(150f, StudioViewModel.BRUSH_SIZE_MAX)
//
//        // Change brush thickness to lowest amount
//        onView(withId(R.id.brushSizeButton)).perform(click())
//        onView(withId(R.id.seekBarVertical)).perform(swipeLeft())
//        onView(withText("OK")).perform(click())
//        onView(withId(R.id.paper)).perform(swipeLeft())
//        assertEquals(150f, StudioViewModel.BRUSH_SIZE_MIN)
//        assertEquals(Color.WHITE, viewModel.brushColor.value)
//
//        // Check if something has been drawn on the canvas (Paper)
//        bitmapIsDrawnOn(viewModel.bitmap.value!!)
//    }
//
//    /**
//     * Helper to check if bitmap has been drawn on
//     */
//    private fun bitmapIsDrawnOn(bitmap: Bitmap): Boolean {
//        for (x in 0 until bitmap.width) {
//            for (y in 0 until bitmap.height) {
//                if (bitmap.getPixel(x, y) != Color.WHITE) {
//                    return true
//                }
//            }
//        }
//        return false
//    }
//}