package com.example.deepsleep.model

data class AppSettings(
    val debounceInterval: Int = 3,
    val suppressEnabled: Boolean = true,
    val suppressMode: String = "conservative",
    val suppressInterval: Int = 60,
    val suppressOomValue: Int = 800,
    val backgroundOptimizationEnabled: Boolean = true,
    val motionBackup: String = "enabled"
)
