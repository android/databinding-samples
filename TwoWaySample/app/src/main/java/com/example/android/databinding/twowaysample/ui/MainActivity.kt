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

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableInt
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.android.databinding.twowaysample.BR
import com.example.android.databinding.twowaysample.R
import com.example.android.databinding.twowaysample.data.IntervalTimerViewModel
import com.example.android.databinding.twowaysample.data.IntervalTimerViewModelFactory
import com.example.android.databinding.twowaysample.databinding.IntervalTimerBinding


const val SHARED_PREFS_KEY = "timer"

/**
 * This activity only takes care of binding a ViewModel to the layout. All UI calls are delegated
 * to the Data Binding library or Binding Adapters ([BindingAdapters]).
 *
 * Note that not all calls to the framework are removed, activities are still responsible for non-UI
 * interactions with the framework, like Shared Preferences or Navigation.
 */
class MainActivity : AppCompatActivity() {

    private val intervalTimerViewModel: IntervalTimerViewModel
        by lazy {
            ViewModelProviders.of(this, IntervalTimerViewModelFactory)
                    .get(IntervalTimerViewModel::class.java)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: IntervalTimerBinding = DataBindingUtil.setContentView(
                this, R.layout.interval_timer)
        val viewmodel = intervalTimerViewModel
        binding.viewmodel = viewmodel

        /* Save the user settings whenever they change */
        observeAndSaveTimePerSet(
                viewmodel.timePerWorkSet, R.string.prefs_timePerWorkSet)
        observeAndSaveTimePerSet(
                viewmodel.timePerRestSet, R.string.prefs_timePerRestSet)

        /* Number of sets needs a different  */
        observeAndSaveNumberOfSets(viewmodel)

        if (savedInstanceState == null) {
            /* If this is the first run, restore shared settings */
            restorePreferences(viewmodel)
            observeAndSaveNumberOfSets(viewmodel)
        }
    }

    private fun observeAndSaveTimePerSet(timePerWorkSet: ObservableInt, prefsKey: Int) {
        timePerWorkSet.addOnPropertyChangedCallback(
                object : Observable.OnPropertyChangedCallback() {
            @SuppressLint("CommitPrefEdits")
            override fun onPropertyChanged(observable: Observable?, p1: Int) {
                Log.d("saveTimePerWorkSet", "Saving time-per-set preference")
                val sharedPref =
                        getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE) ?: return
                sharedPref.edit().apply {
                    putInt(getString(prefsKey), (observable as ObservableInt).get())
                    commit()
                }
            }
        })
    }

    private fun restorePreferences(viewModel: IntervalTimerViewModel) {
        val sharedPref =
                getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE) ?: return
        val timePerWorkSetKey = getString(R.string.prefs_timePerWorkSet)
        var wasAnythingRestored = false
        if (sharedPref.contains(timePerWorkSetKey)) {
            viewModel.timePerWorkSet.set(sharedPref.getInt(timePerWorkSetKey, 100))
            wasAnythingRestored = true
        }
        val timePerRestSetKey = getString(R.string.prefs_timePerRestSet)
        if (sharedPref.contains(timePerRestSetKey)) {
            viewModel.timePerRestSet.set(sharedPref.getInt(timePerRestSetKey, 50))
            wasAnythingRestored = true
        }
        val numberOfSetsKey = getString(R.string.prefs_numberOfSets)
        if (sharedPref.contains(numberOfSetsKey)) {
            viewModel.numberOfSets = arrayOf(0, sharedPref.getInt(numberOfSetsKey, 5))
            wasAnythingRestored = true
        }
        if (wasAnythingRestored) Log.d("saveTimePerWorkSet", "Preferences restored")
        viewModel.stopButtonClicked()
    }

    private fun observeAndSaveNumberOfSets(viewModel: IntervalTimerViewModel) {
        viewModel.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            @SuppressLint("CommitPrefEdits")
            override fun onPropertyChanged(observable: Observable?, p1: Int) {
                if (p1 == BR.numberOfSets) {
                    Log.d("saveTimePerWorkSet", "Saving number of sets preference")
                    val sharedPref =
                            getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE) ?: return
                    sharedPref.edit().apply {
                        putInt(getString(R.string.prefs_numberOfSets), viewModel.numberOfSets[1])
                        commit()
                    }
                }
            }
        })
    }
}
