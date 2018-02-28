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
@file:JvmName("Converter")
package com.example.android.databinding.twowaysample.util

import kotlin.math.round

fun fromTenthsToSeconds(tenths: Int) : String {
    return if (tenths < 600) {
        String.format("%.1f", tenths / 10.0)
    } else {
        val minutes = (tenths / 10) / 60
        val seconds = (tenths / 10) % 60
        String.format("%d:%02d", minutes, seconds)
    }
}

fun cleanSecondsString(seconds: String): Int {
    // Remove letters and other characters
    val filteredValue = seconds.replace(Regex("""[^\d:.]"""), "")
    if (filteredValue.isEmpty()) return 0
    val elements: List<Int> = filteredValue.split(":").map { it -> round(it.toDouble()).toInt() }

    var result: Int
    return when {
        elements.size > 2 -> 0
        elements.size > 1 -> {
            result = elements[0] * 60
            result += elements[1]
            result * 10
        }
        else -> elements[0] * 10
    }
}
