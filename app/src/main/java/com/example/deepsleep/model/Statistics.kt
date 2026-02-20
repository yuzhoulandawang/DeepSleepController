package com.example.deepsleep.model

data class Statistics(
    val totalEnterCount: Int = 0,
    val totalEnterSuccess: Int = 0,
    val totalExitCount: Int = 0,
    val totalExitSuccess: Int = 0,
    val totalAutoExitCount: Int = 0,
    val totalAutoExitRecover: Int = 0,
    val totalMaintenanceCount: Int = 0,
    val totalStateChangeCount: Int = 0,
    val serviceStartTime: Long = 0
)
