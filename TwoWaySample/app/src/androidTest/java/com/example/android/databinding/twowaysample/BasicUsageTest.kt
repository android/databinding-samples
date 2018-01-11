/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.databinding.twowaysample

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.pressImeActionButton
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.example.android.databinding.twowaysample.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * UI tests that check that the two-way data binding inputs are working.
 *
 * For simplicity, only this feature is tested. A real app would inject a test timer and cover
 * more cases.
 */
@RunWith(AndroidJUnit4::class)
class BasicUsageTest {

    @get:Rule
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun work_increments() {
        // Set an initial work time
        onView(withId(R.id.setWorkTime)).perform(typeText("5"), pressImeActionButton())

        // Increment it
        onView(withId(R.id.workplus)).perform(click())

        // Check that it was incremented and formatted correctly
        onView(withId(R.id.setWorkTime)).check(matches(withText("6.0")))
    }

    @Test
    fun work_decrements() {
        // Set an initial work time
        onView(withId(R.id.setWorkTime)).perform(typeText("5"), pressImeActionButton())

        // Decrement it
        onView(withId(R.id.workminus)).perform(click())

        // Check that it was incremented and formatted correctly
        onView(withId(R.id.setWorkTime)).check(matches(withText("4.0")))
    }

    @Test
    fun set_increments() {
        // Set an initial number of sets
        val initialSets = 5
        onView(withId(R.id.numberOfSets))
                .perform(typeText(initialSets.toString()), pressImeActionButton())

        // Increment it
        onView(withId(R.id.setsIncrease)).perform(click())

        // Check that it was incremented and formatted correctly
        val setsFormat = InstrumentationRegistry.getTargetContext().resources.getString(
                R.string.sets_format)
        onView(withId(R.id.numberOfSets))
                .check(matches(withText(String.format(setsFormat, 1, initialSets + 1))))
    }
}
