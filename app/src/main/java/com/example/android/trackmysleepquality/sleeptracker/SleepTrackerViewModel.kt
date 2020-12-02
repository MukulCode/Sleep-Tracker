/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import android.view.animation.Transformation
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.map
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {



    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var tonight = MutableLiveData<SleepNight?>()
    private val nights = database.getAllNights()

    val nightString = Transformations.map(nights){ nights ->
        formatNights(nights, application.resources)
    }


    //if tonight is null then start should be visible
    val startButtonVisible = Transformations.map(tonight){
        null == it
    }

    //if tonight has a value then stop button should be visible
    val stopButtonVisible = Transformations.map(tonight){
        null != it
    }

    //if there are nights, then only clear button should be visible
    val clearButtonVisible = Transformations.map(nights){
        it?.isNotEmpty()
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality : LiveData<SleepNight>
        get() = _navigateToSleepQuality

    fun doneNavigation() {
        _navigateToSleepQuality.value = null
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        //This will launch the coroutine without disturbing the mainthread into context defined by the scope
        uiScope.launch {
            tonight.value = getTonightFromDatabse()
        }
    }

    //We want to make sure that it does not block and returns sleepNight or null
    private suspend fun getTonightFromDatabse(): SleepNight? {
        return withContext(Dispatchers.IO){
            var night = database.getTonight()
            //start time and end time are the same then we know that we are continuing with existing night
            if(night?.endTimeMilli != night?.startTimeMilli){
                night = null
            }
            night
        }
    }


    //Click Handler for start tracking
    fun onStartTracking() {
        //we are doing this in ui scope because we need it to update the UI
        uiScope.launch {
            val newNight = SleepNight()

            insert(newNight)
            tonight.value = getTonightFromDatabse()
        }
    }

    private suspend fun insert(night: SleepNight){
        withContext(Dispatchers.IO){
            database.insert(night)
        }
    }

//Format to write functions
//    fun someWorkNeedsToBeDone{
//        uiScope.launch {
//            suspendFunction()
//        }
//    }
//
//    private suspend fun suspendFunction() {
//        withContext(Dispatchers.IO){
//            longRunningWork()
//        }
//    }





    //Click Handler for stop tracking
    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight){
        withContext(Dispatchers.IO){
            database.update(night)
        }
    }


    //clickHandler for clear button
    fun onClear(){
        uiScope.launch {
            clear()
            tonight.value = null
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO){
            database.clear()
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

