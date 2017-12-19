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

package com.example.android.databinding.basicsample.data

import org.junit.Assert.assertEquals
import org.junit.Test


class ProfileObservableViewModelTest {

    private val viewmodel = ProfileObservableViewModel()

    @Test
    fun popularityIsStarAfter10Likes() {
        callOnLikeTimes(9)
        assertEquals(viewmodel.getPopularity(), Popularity.POPULAR)
        callOnLikeTimes(1)
        assertEquals(viewmodel.getPopularity(), Popularity.STAR)
    }

    @Test
    fun popularityIsPopularAfter5Likes() {
        callOnLikeTimes(4)
        assertEquals(viewmodel.getPopularity(), Popularity.NORMAL)
        callOnLikeTimes(1)
        assertEquals(viewmodel.getPopularity(), Popularity.POPULAR)
    }

    private fun callOnLikeTimes(times: Int) {
        (0 until times).forEach {
            viewmodel.onLike()
        }
    }
}