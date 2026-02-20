package com.example.deepsleep.root

object WaltOptimizer {
    
    private val DAILY_PARAMS = mapOf(
        "up_rate_limit_us" to "1000",
        "down_rate_limit_us" to "500",
        "hispeed_load" to "85",
        "target_loads" to "80"
    )
    
    private val STANDBY_PARAMS = mapOf(
        "up_rate_limit_us" to "5000",
        "down_rate_limit_us" to "0",
        "hispeed_load" to "95",
        "target_loads" to "90"
    )
    
    private val DEFAULT_PARAMS = mapOf(
        "up_rate_limit_us" to "0",
        "down_rate_limit_us" to "0",
        "hispeed_load" to "90",
        "target_loads" to "90"
    )
    
    suspend fun applyDaily() {
        applyWaltParams(DAILY_PARAMS)
        applySchedParams("daily")
    }
    
    suspend fun applyStandby() {
        applyWaltParams(STANDBY_PARAMS)
        applySchedParams("standby")
    }
    
    suspend fun restoreDefault() {
        applyWaltParams(DEFAULT_PARAMS)
        applySchedParams("default")
    }
    
    private suspend fun applyWaltParams(params: Map<String, String>) {
        val commands = mutableListOf<String>()
        
        val policies = RootCommander.exec(
            "ls -d /sys/devices/system/cpu/cpufreq/policy* 2>/dev/null"
        ).out
        
        for (policy in policies) {
            val waltDir = "$policy/walt"
            for ((key, value) in params) {
                commands.add("printf '%s' \"$value\" > $waltDir/$key 2>/dev/null || true")
            }
        }
        
        RootCommander.execBatch(commands)
    }
    
    private suspend fun applySchedParams(mode: String) {
        val commands = when (mode) {
            "daily" -> listOf(
                "echo '30 30' > /proc/sys/kernel/sched_upmigrate 2>/dev/null || true",
                "echo '20 20' > /proc/sys/kernel/sched_downmigrate 2>/dev/null || true",
                "echo 25 > /proc/sys/kernel/sched_group_upmigrate 2>/dev/null || true",
                "echo 15 > /proc/sys/kernel/sched_group_downmigrate 2>/dev/null || true",
                "echo 128 > /proc/sys/kernel/sched_util_clamp_min 2>/dev/null || true",
                "echo 1024 > /proc/sys/kernel/sched_util_clamp_max 2>/dev/null || true"
            )
            "standby" -> listOf(
                "echo '50 50' > /proc/sys/kernel/sched_upmigrate 2>/dev/null || true",
                "echo '40 40' > /proc/sys/kernel/sched_downmigrate 2>/dev/null || true",
                "echo 40 > /proc/sys/kernel/sched_group_upmigrate 2>/dev/null || true",
                "echo 30 > /proc/sys/kernel/sched_group_downmigrate 2>/dev/null || true",
                "echo 128 > /proc/sys/kernel/sched_util_clamp_min 2>/dev/null || true",
                "echo 768 > /proc/sys/kernel/sched_util_clamp_max 2>/dev/null || true"
            )
            "default" -> listOf(
                "echo '30 30' > /proc/sys/kernel/sched_upmigrate 2>/dev/null || true",
                "echo '20 20' > /proc/sys/kernel/sched_downmigrate 2>/dev/null || true",
                "echo 25 > /proc/sys/kernel/sched_group_upmigrate 2>/dev/null || true",
                "echo 15 > /proc/sys/kernel/sched_group_downmigrate 2>/dev/null || true",
                "echo 0 > /proc/sys/kernel/sched_util_clamp_min 2>/dev/null || true",
                "echo 1024 > /proc/sys/kernel/sched_util_clamp_max 2>/dev/null || true"
            )
            else -> emptyList()
        }
        
        RootCommander.execBatch(commands)
    }
    
    suspend fun applyGlobalOptimizations() {
        val commands = listOf(
            "echo '0 0 0 0' > /proc/sys/kernel/printk 2>/dev/null || true",
            "echo 0 > /proc/sys/kernel/panic 2>/dev/null || true",
            "echo 0 > /proc/sys/kernel/panic_on_oops 2>/dev/null || true",
            "echo 60 > /proc/sys/vm/swappiness 2>/dev/null || true",
            "echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null || true",
            "echo 15 > /proc/sys/vm/dirty_ratio 2>/dev/null || true",
            "echo 5 > /proc/sys/vm/dirty_background_ratio 2>/dev/null || true",
            "echo 1000 > /proc/sys/vm/dirty_expire_centisecs 2>/dev/null || true",
            "echo 3000 > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null || true",
            "echo 0 > /proc/sys/vm/page-cluster 2>/dev/null || true",
            "for f in /sys/block/*/queue/scheduler; do echo noop > \\$f 2>/dev/null || echo deadline > \\$f 2>/dev/null || true; done",
            "for f in /sys/block/*/queue/read_ahead_kb; do echo 128 > \\$f 2>/dev/null || true; done"
        )
        
        RootCommander.execBatch(commands)
    }
}
