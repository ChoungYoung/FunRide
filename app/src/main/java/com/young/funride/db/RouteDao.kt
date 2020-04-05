package com.young.funride.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.young.funride.entity.RouteRecord

@Dao
interface RouteDao {
    @Query("SELECT * FROM route_history")
    suspend fun getAll(): List<RouteRecord>

    @Insert
    suspend fun insertRoute(record: RouteRecord)
}