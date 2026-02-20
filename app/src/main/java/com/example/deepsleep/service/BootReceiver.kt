package com.example.deepsleep.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 开机自启（默认启用）
            context.startService(Intent(context, DeepSleepService::class.java).apply {
                action = DeepSleepService.ACTION_START
            })
        }
    }
}
