package com.example.deepsleep.model

enum class DozeState(val displayName: String) {
    ACTIVE("活跃"),
    INACTIVE("准备中"),
    IDLE("深度睡眠"),
    IDLE_MAINTENANCE("维护窗口"),
    UNKNOWN("未知")
}
