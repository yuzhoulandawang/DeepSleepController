package com.example.deepsleep.root

object BackgroundOptimizer {
    
    private val DEFAULT_BG_WHITELIST = listOf(
        "com.android.deskclock",
        "com.android.contacts",
        "com.android.dialer",
        "com.android.inputmethod.latin",
        "com.google.android.inputmethod.latin",
        "com.android.providers.downloads",
        "com.google.android.apps.messaging",
        "com.whatsapp",
        "com.tencent.mm",
        "com.tencent.mobileqq",
        "com.eg.android.AlipayGphone"
    )
    
    suspend fun optimizeAll(context: android.content.Context, customWhitelist: List<String> = emptyList()) {
        val whitelist = DEFAULT_BG_WHITELIST + customWhitelist
        
        val packages = RootCommander.exec(
            "pm list packages -3 | sed 's/package://g'"
        ).out
        
        val commands = mutableListOf<String>()
        
        for (packageName in packages) {
            if (whitelist.contains(packageName)) continue
            
            commands.add("appops set $packageName RUN_ANY_IN_BACKGROUND deny 2>/dev/null || true")
            commands.add("appops set $packageName WAKE_LOCK ignore 2>/dev/null || true")
            commands.add("pm set-app-standby-bucket $packageName rare 2>/dev/null || true")
        }
        
        commands.chunked(50).forEach { batch ->
            RootCommander.execBatch(batch)
        }
    }
    
    suspend fun restoreApp(packageName: String) {
        RootCommander.exec(
            "appops set $packageName RUN_ANY_IN_BACKGROUND default",
            "appops set $packageName WAKE_LOCK default",
            "pm set-app-standby-bucket $packageName active"
        )
    }
    
    suspend fun restoreAll() {
        val packages = RootCommander.exec(
            "pm list packages -3 | sed 's/package://g'"
        ).out
        
        val commands = packages.flatMap { packageName ->
            listOf(
                "appops set $packageName RUN_ANY_IN_BACKGROUND default 2>/dev/null || true",
                "appops set $packageName WAKE_LOCK default 2>/dev/null || true",
                "pm set-app-standby-bucket $packageName active 2>/dev/null || true"
            )
        }
        
        commands.chunked(50).forEach { batch ->
            RootCommander.execBatch(batch)
        }
    }
}
