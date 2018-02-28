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

package com.example.android.databinding.twowaysample.data

import com.example.android.databinding.twowaysample.util.Timer
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

/**
 * Unit tests for [IntervalTimerViewModel].
 *
 * For simplicity, only a feature is tested: The timer is started and paused at a specific time.
 * Then, the tests check that the exposed times are correct.
 */
class IntervalTimerViewModelTest {

    @Test
    fun timePerWork_after100ms() {
        testWorkTimeLeftAfterTicks(10)
    }

    @Test
    fun timePerWork_after200ms() {
        testWorkTimeLeftAfterTicks(20)
    }

    @Test
    fun timePerWork_afterOneWorkCycle() {
        testWorkTimeLeftAfterTicks(INITIAL_SECONDS_PER_WORK_SET * 10)
    }

    @Test
    fun timePerWork_afterMoreThanOneWorkCycle() {
        // Create a ViewModel with a timer that stops after a a complete work set and a bit more.
        val viewModel = IntervalTimerViewModel(TestTimer(INITIAL_SECONDS_PER_WORK_SET * 10 + 10))

        // Start timer
        viewModel.timerRunning = true

        // Check that the timer exposes the correct data
        assertThat(viewModel.workTimeLeft.get(), `is`(0))
        assertThat(viewModel.restTimeLeft.get(), `is`(INITIAL_SECONDS_PER_REST_SET * 10 - 10 + 1))
    }

    @Test
    fun timePerWorkAndRest_afterWorkSet() {
        // Create a ViewModel with a timer that stops after a a complete work set and a bit more.
        val viewModel = IntervalTimerViewModel(TestTimer(INITIAL_SECONDS_PER_WORK_SET * 10))

        // Start timer
        viewModel.timerRunning = true

        // Check that the timer exposes the correct data
        assertThat(viewModel.workTimeLeft.get(), `is`(1))
        assertThat(viewModel.restTimeLeft.get(), `is`(INITIAL_SECONDS_PER_REST_SET * 10))
    }

    private fun testWorkTimeLeftAfterTicks(tenths: Int) {
        // Create a ViewModel with a timer that stops after a time.
        val viewModel = IntervalTimerViewModel(TestTimer(tenths))

        // Start timer
        viewModel.timerRunning = true

        // Check that the timer exposes the correct data
        assertThat(viewModel.workTimeLeft.get(), `is`(INITIAL_SECONDS_PER_WORK_SET * 10 - tenths + 1))
    }
}

/**
 * A timer used for tests that executes a task a number of times. It uses ticks instead of
 * a real clock.
 */
class TestTimer(private val ticks: Int = 5) : Timer {
    private var running = false
    private var elapsedTicks = 0
    private var startTime = 0L
    private var pauseTime = 0L

    override fun start(task: TimerTask) {
        running = true
        while (running && elapsedTicks < ticks) {
            task.run()
            elapsedTicks++
        }
    }

    override fun reset() {
        running = false
    }

    override fun getPausedTime() : Long = pauseTime - startTime

    override fun getElapsedTime() = (elapsedTicks * 100) - startTime

    override fun resetPauseTime() {
        pauseTime - (elapsedTicks * 100)
    }

    override fun resetStartTime() {
        startTime = (elapsedTicks * 100).toLong()
    }

    override fun updatePausedTime() {
        startTime += (elapsedTicks * 100) - pauseTime
    }
}
