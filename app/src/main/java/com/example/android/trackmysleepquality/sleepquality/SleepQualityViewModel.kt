

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Database
import androidx.room.PrimaryKey
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

class SleepQualityViewModel (
        private val sleepNightKey: Long = 0L,
        val database: SleepDatabaseDao) : ViewModel(){

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
    val navigateToSleepTracker : LiveData<Boolean?>
        get() = _navigateToSleepTracker

    fun doneNavigationg() {
        _navigateToSleepTracker.value = null
    }

    fun onSetSleepQuality(quality : Int){
        //launching a coroutine in ui scope
        uiScope.launch {
            //switching to the ui dispatcher
            withContext(Dispatchers.IO){
//                get tonight using the sleepNightKey(we get this using safe args)
                val tonight = database.get(sleepNightKey) ?: return@withContext
                tonight.sleepQuality = quality
                database.update(tonight)
            }
            _navigateToSleepTracker.value = true
        }
    }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
