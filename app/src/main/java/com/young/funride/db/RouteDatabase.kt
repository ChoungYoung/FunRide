package com.young.funride.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.young.funride.entity.RouteRecord

@Database(
    entities = [RouteRecord::class],
    version = 1
)
abstract class RouteDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile
        private var INSTANCE: RouteDatabase? = null

        fun getInstance(context: Context): RouteDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }

        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                RouteDatabase::class.java, "ride.db")
                .build()

    }
}