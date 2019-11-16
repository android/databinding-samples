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


import androidx.databinding.Bindable
import androidx.databinding.ObservableInt
import com.example.android.databinding.twowaysample.BR
import com.example.android.databinding.twowaysample.util.ObservableViewModel
import com.example.android.databinding.twowaysample.util.Timer
import com.example.android.databinding.twowaysample.util.cleanSecondsString
import java.util.*
import kotlin.math.round

const val INITIAL_SECONDS_PER_WORK_SET = 5 // Seconds
const val INITIAL_SECONDS_PER_REST_SET = 2 // Seconds
const val INITIAL_NUMBER_OF_SETS = 5

/**
 * A ViewModel that contains the logic to control a work/rest interval timer.
 *
 * It uses different ways to expose data for the view:
 *  * Observable Fields
 *  * Bindable properties
 *  * Methods for user actions
 *
 * Data Binding features:
 *
 *  <b>One-way data binding</b>
 *
 *  See [workTimeLeft] and [restTimeLeft]. They are updated from the ViewModel and the UI is
 *  refreshed automatically
 *
 *  One-way also applies to user actions. See [workTimeIncrease] and how a lambda expression in
 *  the layout is built:
 *
 *    `android:onClick="@{() -> viewmodel.workTimeIncrease()}"`
 *
 *  <b>Two-way data binding</b>
 *
 *  * Simple: See `timerRunning`. It toggles between start and pause with a single line on the
 *  layout:
 *
 *    `android:checked="@={viewmodel.timerRunning}"`
 *
 *  * Custom: [numberOfSets] uses the most advanced form of 2-way Data Binding.
 *  See [com.example.android.databinding.twowaysample.ui.NumberOfSetsBindingAdapters].
 */
class IntervalTimerViewModel(private val timer: Timer) : ObservableViewModel() {

    /* Observable fields. When their values change (set method is called) they send updates to
    the UI automatically. */
    val timePerWorkSet = ObservableInt(INITIAL_SECONDS_PER_WORK_SET * 10) // tenths
    val timePerRestSet = ObservableInt(INITIAL_SECONDS_PER_REST_SET * 10) // tenths
    val workTimeLeft = ObservableInt(timePerWorkSet.get()) // tenths
    val restTimeLeft = ObservableInt(timePerRestSet.get()) // tenths

    /**
     * Used to indicate to the UI that the timer is running and to receive start/pause commands.
     *
     * A @Bindable property is a more flexible way to create an observable field. Use it with
     * two-way data binding. When the property changes in the ViewModel,
     * `notifyPropertyChanged(BR.timerRunning)` must be called so the system fetches the new value
     * with the getter and notifies the observers.
     *
     * User actions come through the setter, using two-way data binding.
     */
    var timerRunning: Boolean
        @Bindable get() {
            return state == TimerStates.STARTED
        }
        set(value) {
            // These methods take care of calling notifyPropertyChanged()
            if (value) startButtonClicked() else pauseButtonClicked()
        }

    /* The number of sets is handled using a custom two-way data binding approach.
    The EditText bound to this property displays a string ("Sets: 3/5") which is different from
    the number the user will input (5).

    The property exposes a getter and the view changes the value via the setter. Using @Bindable
    creates a Data Binding property but it's not automatically updated, you need to call
    `notifyPropertyChanged(BR.numberOfSets)` when it changes.
    */

    private var numberOfSetsTotal = INITIAL_NUMBER_OF_SETS
    private var numberOfSetsElapsed = 0
    var numberOfSets: Array<Int> = emptyArray()
        @Bindable get() {
            return arrayOf(numberOfSetsElapsed, numberOfSetsTotal)
        }
    set(value: Array<Int>) {
        // Only the second Int is being set
        val newTotal = value[1]
        if (newTotal == numberOfSets[1]) return // Break loop if there's no change
        // Only update if it doesn't affect the current exercise
        if (newTotal != 0 && newTotal > numberOfSetsElapsed) {
            field = value
            numberOfSetsTotal = newTotal
        }
        // Even if the input is empty, force a refresh of the view
        notifyPropertyChanged(BR.numberOfSets)
    }

    /**
     * Used to control some animations.
     */
    val inWorkingStage: Boolean
        @Bindable get() {
            return stage == StartedStages.WORKING
        }

    /**
     * Called from the UI, parses a new user-entered value.
     */
    fun timePerRestSetChanged(newValue: CharSequence) {
        try {
            timePerRestSet.set(cleanSecondsString(newValue.toString()))
        } catch (e: NumberFormatException) {
            return
        }
        if (!isRestTimeAndRunning()) {
            restTimeLeft.set(timePerRestSet.get())
        }
    }

    /**
     * Called from the UI, parses a new user-entered value.
     */
    fun timePerWorkSetChanged(newValue: CharSequence) {
        try {
            timePerWorkSet.set(cleanSecondsString(newValue.toString()))
        } catch (e: NumberFormatException) {
            return
        }
        if (!timerRunning) {
            workTimeLeft.set(timePerWorkSet.get())
        }
    }

    /**
     * Methods bound to buttons that increase/decrease times and number of sets.
     */

    fun restTimeIncrease() = timePerSetIncrease(timePerRestSet, 1)

    fun workTimeIncrease() = timePerSetIncrease(timePerWorkSet, 1)

    fun setsIncrease() {
        numberOfSetsTotal += 1
        notifyPropertyChanged(BR.numberOfSets)
    }

    fun restTimeDecrease() = timePerSetIncrease(timePerRestSet, -1)

    fun workTimeDecrease() = timePerSetIncrease(timePerWorkSet, -1, 10)

    fun setsDecrease() { if (numberOfSetsTotal > numberOfSetsElapsed + 1) {
            numberOfSetsTotal -= 1
            notifyPropertyChanged(BR.numberOfSets)
        }
    }

    /**
     * Resets timers and state. Called from the UI.
     */
    fun stopButtonClicked() {
        resetTimers()
        numberOfSetsElapsed = 0
        state = TimerStates.STOPPED
        stage = StartedStages.WORKING // Reset for the next set
        timer.reset()

        notifyPropertyChanged(BR.timerRunning)
        notifyPropertyChanged(BR.inWorkingStage)
        notifyPropertyChanged(BR.numberOfSets)
    }

    private var state = TimerStates.STOPPED
    private var stage = StartedStages.WORKING

    /**
     * Increases or decreases the work or rest time by a set value, depending on the sign of
     * the parameter.
     *
     * @param timePerSet the value holder to be updated
     * @param sign  1 to increase, -1 to decrease.
     */
    private fun timePerSetIncrease(timePerSet: ObservableInt, sign: Int = 1, min: Int = 0) {
        if (timePerSet.get() < 10 && sign < 0) return
        // Increase the time in chunks
        roundTimeIncrease(timePerSet, sign, min)
        if (state == TimerStates.STOPPED) {
            // If stopped, update the timers right away
            workTimeLeft.set(timePerWorkSet.get())
            restTimeLeft.set(timePerRestSet.get())
        } else {
            // If running or paused, the timers need to be calculated
            updateCountdowns()
        }
    }

    /**
     * True if the work time has passed and we're currently resting.
     */
    private fun isRestTimeAndRunning(): Boolean {
        return (state == TimerStates.PAUSED || state == TimerStates.STARTED)
                && workTimeLeft.get() == 0
    }

    /**
     * Make increasing and decreasing times a bit nicer by adding chunks.
     */
    private fun roundTimeIncrease(timePerSet: ObservableInt, sign: Int, min: Int) {
        val currentValue = timePerSet.get()
        val newValue =
        when {
        // <10 Seconds - increase 1
            currentValue < 100 -> timePerSet.get() + sign * 10
        // >10 seconds, 5-second increase
            currentValue < 600 -> (round(currentValue / 50.0) * 50 + (50 * sign)).toInt()
        // >60 seconds, 10-second increase
            else -> (round(currentValue / 100.0) * 100 + (100 * sign)).toInt()
        }
        timePerSet.set(newValue.coerceAtLeast(min))
    }

    /**
     * Start the timer!
     */
    private fun startButtonClicked() {
        when (state) {
            TimerStates.PAUSED -> {
                pausedToStarted()
            }
            TimerStates.STOPPED -> {
                stoppedToStarted()
            }
            TimerStates.STARTED -> {
                // Do nothing
            }
        }

        val task = object : TimerTask() {
            override fun run() {
                if (state == TimerStates.STARTED) {
                    updateCountdowns()
                }
            }
        }

        // Schedule timer every 100ms to update the counters.
        timer.start(task)
    }

    /**
     * Pause the timer
     */
    private fun pauseButtonClicked() {
        if (state == TimerStates.STARTED) {
            startedToPaused()
        }
        notifyPropertyChanged(BR.timerRunning)
    }

    /* STOPPED -> STARTED */
    private fun stoppedToStarted() {
        // Set the start time
        timer.resetStartTime()
        state = TimerStates.STARTED
        stage = StartedStages.WORKING

        notifyPropertyChanged(BR.inWorkingStage)
        notifyPropertyChanged(BR.timerRunning)
    }

    /* PAUSED -> STARTED */
    private fun pausedToStarted() {
        // We're measuring the time we're paused, so just add it to the start time
        timer.updatePausedTime()
        state = TimerStates.STARTED

        notifyPropertyChanged(BR.timerRunning)
    }

    /* STARTED -> PAUSED */
    private fun startedToPaused() {
        state = TimerStates.PAUSED
        timer.resetPauseTime()
    }

    private fun updateCountdowns() {
        if (state == TimerStates.STOPPED) {
            resetTimers()
            return
        }

        val elapsed = if (state == TimerStates.PAUSED) {
            timer.getPausedTime()
        } else {
            timer.getElapsedTime()
        }

        if (stage == StartedStages.RESTING) {
            updateRestCountdowns(elapsed)
        } else {
            // work
            updateWorkCountdowns(elapsed)
        }
    }

    private fun updateWorkCountdowns(elapsed: Long) {
        stage = StartedStages.WORKING
        val newTimeLeft = timePerWorkSet.get() - (elapsed / 100).toInt()
        if (newTimeLeft <= 0) {
            workoutFinished()
        }
        workTimeLeft.set(newTimeLeft.coerceAtLeast(0))
    }

    private fun updateRestCountdowns(elapsed: Long) {
        // Calculate the countdown time with the start time
        val newRestTimeLeft = timePerRestSet.get() - (elapsed / 100).toInt()
        restTimeLeft.set(newRestTimeLeft.coerceAtLeast(0))

        if (newRestTimeLeft <= 0) { // Rest time is up
            numberOfSetsElapsed += 1
            resetTimers()
            if (numberOfSetsElapsed >= numberOfSetsTotal) { // End
                timerFinished()
            } else { // End of set
                setFinished()
            }
        }
    }

    /* WORKING -> RESTING */
    private fun workoutFinished() {
        timer.resetStartTime()
        stage = StartedStages.RESTING
        notifyPropertyChanged(BR.inWorkingStage)
    }

    /* RESTING -> WORKING */
    private fun setFinished() {
        timer.resetStartTime()
        stage = StartedStages.WORKING

        notifyPropertyChanged(BR.inWorkingStage)
        notifyPropertyChanged(BR.numberOfSets)
    }

    /* RESTING -> STOPPED */
    private fun timerFinished() {
        state = TimerStates.STOPPED
        stage = StartedStages.WORKING // Reset for the next set
        timer.reset()
        notifyPropertyChanged(BR.timerRunning)
        numberOfSetsElapsed = 0

        notifyPropertyChanged(BR.inWorkingStage)
        notifyPropertyChanged(BR.numberOfSets)
    }

    private fun resetTimers() {
        // Reset counters
        workTimeLeft.set(timePerWorkSet.get())

        // Set the start time
        restTimeLeft.set(timePerRestSet.get())
    }
}

/**
 * Extensions to allow += and -= on an ObservableInt.
 */
private operator fun ObservableInt.plusAssign(value: Int) {
    set(get() + value)
}

private operator fun ObservableInt.minusAssign(amount: Int) {
    plusAssign(- amount)
}

enum class TimerStates {STOPPED, STARTED, PAUSED}

enum class StartedStages {WORKING, RESTING}
