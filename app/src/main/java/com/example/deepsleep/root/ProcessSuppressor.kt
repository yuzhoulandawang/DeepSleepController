package com.example.deepsleep.root

object ProcessSuppressor {
    
    suspend fun suppress(oomValue: Int, whitelist: List<String>) {
        val pids = RootCommander.exec(
            "ls /proc | grep -E '^[0-9]+$'"
        ).out
        
        val commands = mutableListOf<String>()
        
        for (pid in pids) {
            val pidNum = pid.toIntOrNull() ?: continue
            if (pidNum == android.os.Process.myPid()) continue
            
            // 使用 UID 过滤系统进程（UID < 10000 为系统进程）
            val status = RootCommander.readFile("/proc/$pid/status") ?: continue
            val uidLine = status.lines().find { it.startsWith("Uid:") } ?: continue
            val uid = uidLine.split("\\t").getOrNull(1)?.toIntOrNull() ?: 0
            if (uid < 10000) continue  // 跳过系统进程
            
            val cmdline = RootCommander.readFile("/proc/$pid/cmdline") ?: ""
            if (isWhitelisted(cmdline, whitelist)) continue
            
            commands.add("echo $oomValue > /proc/$pid/oom_score_adj 2>/dev/null || true")
        }
        
        commands.chunked(100).forEach { batch ->
            RootCommander.execBatch(batch)
        }
    }
    
    private fun isWhitelisted(cmdline: String, whitelist: List<String>): Boolean {
        val name = cmdline.split('\u0000').firstOrNull() ?: ""
        val basename = name.substringAfterLast("/")
        
        return whitelist.any { pattern ->
            name.contains(pattern) || basename.contains(pattern)
        }
    }
}
