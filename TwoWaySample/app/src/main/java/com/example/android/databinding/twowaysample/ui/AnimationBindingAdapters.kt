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

package com.example.android.databinding.twowaysample.ui

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.databinding.BindingAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.android.databinding.twowaysample.R

/**
 * A collection of [BindingAdapter]s used to create animations in the app
 */
object AnimationBindingAdapters {

    private const val VERTICAL_BIAS_ANIMATION_DURATION = 900L

    private const val BG_COLOR_ANIMATION_DURATION = 500L

    /**
     * Controls a background color animation.
     *
     * @param view one of the timers (work/rest)
     * @param timerRunning whether the app timer is running
     * @param activeStage whether this particular timer (work/rest) is active
     */
    @BindingAdapter(value=["animateBackground", "animateBackgroundStage"], requireAll = true)
    @JvmStatic fun animateBackground(view: View, timerRunning: Boolean, activeStage: Boolean) {
        // If the timer is not running, don't animate and set the default color.
        if (!timerRunning) {
            view.setBackgroundColor(
                    ContextCompat.getColor(view.context, R.color.disabledInputColor))
            // This tag prevents a glitch going from reset to started.
            view.setTag(R.id.hasBeenAnimated, false)
            return
        }

        // activeStage controls whether this particular timer (work or rest) is active.
        if (activeStage) {
            // Start animation
            animateBgColor(view, true)
            // This tag prevents a glitch going from paused to started.
            view.setTag(R.id.hasBeenAnimated, true)
        } else {
            // Prevent "end" animation if animation never started
            val hasItBeenAnimated = view.getTag(R.id.hasBeenAnimated) as Boolean?
            if (hasItBeenAnimated == true) {  // this means false if null
                // End animation
                animateBgColor(view, false)
                view.setTag(R.id.hasBeenAnimated, false)
            }
        }
    }

    /**
     * Controls an animation that moves a view up and down.
     *
     * @param view one of the timers (work/rest)
     * @param timerRunning whether the app timer is running
     * @param activeStage whether this particular timer (work/rest) is active
     */
    @BindingAdapter(value=["animateVerticalBias", "animateVerticalBiasStage"],
            requireAll = true)
    @JvmStatic fun animateVerticalBias(view: View, timerRunning: Boolean, activeStage: Boolean) {
        // Change the vertical bias of the View depending on the current state
        when {
            timerRunning && activeStage -> animateVerticalBias(view, 0.6f) // Workout
            timerRunning && !activeStage -> animateVerticalBias(view, 0.4f) // Rest
            else -> animateVerticalBias(view, 0.5f) // Idle
        }
    }

    private fun animateBgColor(view: View, tint: Boolean) {
        val colorRes = ContextCompat.getColor(view.context, R.color.colorPrimaryLight)
        val color2Res = ContextCompat.getColor(view.context, R.color.disabledInputColor)
        val animator = if (tint)
            ObjectAnimator.ofObject(view,
                    "backgroundColor",
                    ArgbEvaluator(),
                    color2Res,
                    colorRes)
        else
            ObjectAnimator.ofObject(view,
                    "backgroundColor",
                    ArgbEvaluator(),
                    colorRes,
                    color2Res)
        animator.duration = BG_COLOR_ANIMATION_DURATION
        animator.start()
    }

    private fun animateVerticalBias(view: View, position: Float) {
        val layoutParams: ConstraintLayout.LayoutParams =
                view.layoutParams as ConstraintLayout.LayoutParams
        val animator = ValueAnimator.ofFloat(layoutParams.verticalBias, position)
        animator.addUpdateListener { animation ->
            val newParams: ConstraintLayout.LayoutParams =
                    view.layoutParams as ConstraintLayout.LayoutParams
            val animatedValue = animation.animatedValue as Float
            newParams.verticalBias = animatedValue
            view.requestLayout()
        }
        animator.interpolator = DecelerateInterpolator()
        animator.duration = VERTICAL_BIAS_ANIMATION_DURATION
        animator.start()
    }
}
