package com.young.funride.entity

data class RoutePoint(
    var id: Int = 0,
    var time: Long = 0,
    var routeLat: Double = 0.0,
    var routeLng: Double = 0.0,
    var speed: Double = 0.0
)