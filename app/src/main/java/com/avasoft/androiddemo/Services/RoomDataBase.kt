package com.avasoft.androiddemo.Services

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.avasoft.androiddemo.BOs.UserBO.UserBO
import com.avasoft.androiddemo.Services.UserService.IUserService

@Database(entities = [UserBO::class], version = 3)
abstract class DemoDatabase : RoomDatabase() {
    abstract fun userDao() : IUserService

    companion object {
        private var INSTANCE : DemoDatabase? = null

        fun getInstance(context : Context) : DemoDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room
                        .databaseBuilder(
                            context.applicationContext,
                            DemoDatabase::class.java,
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