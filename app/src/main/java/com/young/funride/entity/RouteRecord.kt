package com.young.funride.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_history")
data class RouteRecord(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "points") val routePoints: String,
    @ColumnInfo(name = "distance") val distance: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "time") val time: String
)