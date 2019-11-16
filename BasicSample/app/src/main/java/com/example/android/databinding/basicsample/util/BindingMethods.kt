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

package com.example.android.databinding.basicsample.util

import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import android.widget.ImageView


/**
 * `app:srcCompat` is an attribute used by the support library to integrate vector drawables. This
 * BindingMethod binds the attribute to the setImageDrawable method in the ImageView class.
 *
 * Binding methods have to be applied to any class in your project. Even an empty one.
 *
 * This is equivalent to:
 * ```
 *
 *   @BindingAdapter("app:srcCompat")
 *   @JvmStatic fun srcCompat(view: ImageView, @DrawableRes drawableId: Int) {
 *       view.setImageResource(drawable)
 *   }
 * ```
 */
@BindingMethods(
        BindingMethod(type = ImageView::class,
                attribute = "app:srcCompat",
                method = "setImageResource"))
class MyBindingMethods
