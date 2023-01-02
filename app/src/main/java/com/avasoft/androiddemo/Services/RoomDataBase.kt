package com.avasoft.androiddemo.Services

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Services.UserService.IUserService

@Database(entities = [UserBO::class], version = 1)
abstract class RoomDatabase : RoomDatabase() {
    abstract fun userDao() : IUserService

    companion object {
        private var INSTANCE : RoomDatabase? = null

        fun getInstance(context : Context) : RoomDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room
                        .databaseBuilder(
                            context.applicationContext,
                            RoomDatabase::class.java,
                            "room_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}