package com.example.deepsleep.model

import java.util.UUID

enum class WhitelistType {
    SUPPRESS,
    BACKGROUND
}

data class WhitelistItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val note: String = "",
    val type: WhitelistType
)
