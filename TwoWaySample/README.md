Android Data Binding Advanced Sample
=============================================

This sample showcases the following features
of the [Data Binding library](https://developer.android.com/topic/libraries/data-binding/index.html)
with an app that shows a workout timer.

 * Two-way Data Binding
 * Alternatives to Two-way Data Binding
 * Binding adapters with multiple parameters
 * Animations with Binding Adapters
 * Binding converters and inverse converters
 * Data Binding with ViewModels and Kotlin
 * No UI framework calls in activity
 * Testing

Features
--------

![Screenshot](https://github.com/googlesamples/android-databinding/blob/master/TwoWaySample/screenshots/screenshot2way.png)

## Two-way Data Binding

Two-way data binding is used twice in this sample: with a simple case (the toggle start/pause
button) and with a more complex feature (the number of sets input).

### Simple two-way case

In the layout, two-way is indicated with the `@={}` syntax:

```xml
<ToggleButton
    android:checked="@={viewmodel.timerRunning}" />
```

Note the difference with one-way syntax which doesn't have the equals sign (`@{}`).

This layout expression binds the `checked` attribute to `timerRunning` in the ViewModel.
This means that if the property in the ViewModel changes (for example, when the timer finishing),
the button will automatically show the new status.

At the same time, if a user action modifies the `checked` attribute on the button, the ViewModel
will receive this signal, dispatching the event accordingly.

Two Way Data Binding requires the ViewModel property to be decorated with a [`@Bindable`](https://developer.android.com/reference/android/databinding/Bindable.html) annotation:

```kotlin
var timerRunning: Boolean
    @Bindable get() {
        return state == TimerStates.STARTED
    }
    set(value) {
        // These methods take care of calling notifyPropertyChanged()
        if (value) startButtonClicked() else pauseButtonClicked()
    }
```

**Getter**: When the `state` property changes in the ViewModel, `notifyPropertyChanged(BR.timerRunning)` must be
called to indicate that the UI should be updated with the new value obtained in the getter.

**Setter**: User actions invoke the setter. The corresponding methods will only call `notifyPropertyChanged`
when the state changes, to avoid infinite loops.

### Complex two-way case

The text attribute of an `EditText` is much more complicated to manage than the checked/unchecked
nature of a toggle button. On top of that, the requirement for this input view is to show the text
in a particular format (`Sets: %d/%d`) so the two-way syntax is similar but it includes a converter:

```xml
<EditText
    numberOfSets="@={NumberOfSetsConverters.setArrayToString(viewmodel.numberOfSets)}" />
```

`EditText` doesn't have a `numberOfSets` attribute so this implies there's a corresponding binding adapter.

In `NumberOfSetsBindingAdapters.kt`:

```kotlin
@BindingAdapter("numberOfSets")
@JvmStatic fun setNumberOfSets(view: EditText, value: String) {
    view.setText(value)
}
```

This sets the value from the ViewModel in the view.

To complete the two-way syntax "@={}" handling, it's also required to define a Binding Adapter
for a corresponding synthetic attribute with the "AttrChanged" suffix:

```kotlin
@BindingAdapter(value = ["numberOfSetsAttrChanged"])
@JvmStatic fun setListener(view: EditText, listener: InverseBindingListener?) {
    view.onFocusChangeListener = View.OnFocusChangeListener { focusedView, hasFocus ->
        val textView = focusedView as TextView
        if (hasFocus) {
            // Delete contents of the EditText if the focus entered.
            textView.text = ""
        } else {
            // If the focus left, update the listener
            listener?.onChange()
        }
    }
}
```

In this adapter you normally set the listeners that will detect changes in the view. Note that it
includes an InverseBindingListener which needs to be called when we want to tell the data binding
system that there's been a change. This, in turn, triggers calls to the InverseBindingAdapter:

```kotlin
@InverseBindingAdapter(attribute = "numberOfSets")
@JvmStatic fun getNumberOfSets(editText: EditText): String {
    return editText.text.toString()
}
```

See `NumberOfSetsBindingAdapters.kt` for alternatives and an example on how to use converters
with two-way data binding.

## Alternatives two-way data binding

Two-way data binding is an advanced feature that can be complicated. Instead of using the library's
component to automate and remove boilerplate, beginners can opt for a more verbose but easier to
understand approach, using one-way data binding.

The `EditText` that manages the initial timer value has the following attributes:

```xml
<EditText
    android:text="@{Converter.fromTenthsToSeconds(viewmodel.timePerWorkSet)}"
    clearOnFocusAndDispatch="@{() -> viewmodel.timePerWorkSetChanged(setWorkTime.getText())}"
    />
```

Similarly to the previous section, the backing property in the ViewModel needs to be converted
before displaying and changes are sent to the ViewModel using a custom listener.

See `BindingAdapters.kt` for the multiple binding adapters applied in this view and `Converter.kt`
for the logic that converts between formats.

## Binding adapters with multiple parameters

The progress bars in the sample need to be updated whenever either `progress` or the `max` property
changes. The Binding Adapter for this case looks like:

```kotlin
@BindingAdapter(value=["android:max", "android:progress"], requireAll = true)
@JvmStatic fun updateProgress(progressBar: ProgressBar, max: Int, progress: Int) ...

```

See `BindingAdapters.kt` for this example and `AnimationBindingAdapters.kt` for more.

## Animations with binding adapters

Animators are also elements that can be bound to data and they usually involve verbose code for
setup and execution. Data Binding lets you move this code out of the activities and fragments to
a more convenient and isolated location: a binding adapter.

For animations, two binding adapters are created in `AnimationBindingAdapters.kt`. They control
the background color and some Constraint Layout parameters, directly bound to properties in the
ViewModel.

## Binding converters and inverse converters

Binding converters allow you to convert data to a format required by the binding adapter.

See `Converter.kt` for one-way converters and `NumberOfSetsBindingAdapters.kt` for two-way
converters.

## Data Binding with ViewModels and Kotlin

ViewModels are a perfect fit for data binding because they expose data and state to the view and
they survive configuration changes such as rotations.

Common mistakes when using Kotlin with the Data Binding Library include:
 * Forgetting the `@JvmStatic` annotation in Binding Adapters inside an object or class.
Alternatively you can place the functions in the top level of a file so it generates a public static
method.
 * Annotation parameters syntax

## No UI framework calls in activity

One of the important features of data binding is that is frees activities and fragments from making
the UI calls, moving them to the layouts and Binding Adapters. However, not all
framework calls need to be moved.
In this sample, the activity is responsible for ViewModel creation, binding and
managing the Shared Preferences, but there are no UI calls.

This sample uses a relatively complex `IntervalTimerViewModel` that is exposing the data, receiving
events and holding state for a relatively complex screen. There are multiple advantages to this:
it makes very clear where a piece of code belongs to, it prevents activities from holding state,
and it generates very reusable code (binding adapters).

## Testing

There are no special considerations necessary regarding testing and the Data Binding Library.
There is a UI test class that checks part of the interaction and a unit test class that
verifies some logic in the ViewModel.

License
--------

Copyright 2018 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
