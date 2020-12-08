

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase(){

    abstract val sleepDatabaseDao: SleepDatabaseDao

    companion object{

        //INSTANCE will keep a reference to the database once it is created, it is null now
        @Volatile
        private var INSTANCE : SleepDatabase? = null

        fun getInstance(context: Context) : SleepDatabase{
            synchronized(this){
                //wrapping the code here in this block means only one thread at a time
                // can enter this block of code.
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database"
                    ).fallbackToDestructiveMigration()
                            .build()

                    INSTANCE = instance
                }
                return instance

            }
        }
    }

}