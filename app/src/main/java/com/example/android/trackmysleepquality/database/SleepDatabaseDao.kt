

package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SleepDatabaseDao{
    @Insert
    fun insert(night: SleepNight)

    @Update
    fun update(night: SleepNight)


    //Return thr rows where nightId is equal to the key(we will pass this in kotlin function)
    // function will return the object of class SleepNight
    @Query("SELECT * FROM daily_sleep_quality_table WHERE nightId = :key")
    fun get(key: Long):SleepNight?


    //This will clear the table without knowing about its content
    @Query("DELETE FROM daily_sleep_quality_table")
    fun clear()

    //Get all nights, this will return the Live Data wrapped list of all sleepNight Rows
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights():LiveData<List<SleepNight>>

    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC Limit 1")
    fun getTonight():SleepNight?
}
