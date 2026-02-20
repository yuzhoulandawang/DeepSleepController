package com.example.deepsleep

import com.example.deepsleep.BuildConfig
import android.app.Application
import com.topjohnwu.superuser.Shell

class DeepSleepApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }
}
