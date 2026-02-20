package com.example.deepsleep.data

import com.example.deepsleep.model.Statistics
import com.example.deepsleep.root.RootCommander
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class StatsRepository {
    
    private val statsPath = "/data/local/tmp/deep_sleep_logs/stats.txt"
    private val statsMutex = Mutex()
    
    suspend fun loadStats(): Statistics = withContext(Dispatchers.IO) {
        val content = RootCommander.readFile(statsPath)
        
        if (content == null) {
            return@withContext Statistics()
        }
        
        val map = content.lineSequence()
            .filter { it.contains("=") }
            .associate { 
                val parts = it.split("=", limit = 2)
                parts[0].trim() to parts[1].trim()
            }
        
        Statistics(
            totalEnterCount = map["TOTAL_ENTER_COUNT"]?.toIntOrNull() ?: 0,
            totalEnterSuccess = map["TOTAL_ENTER_SUCCESS"]?.toIntOrNull() ?: 0,
            totalExitCount = map["TOTAL_EXIT_COUNT"]?.toIntOrNull() ?: 0,
            totalExitSuccess = map["TOTAL_EXIT_SUCCESS"]?.toIntOrNull() ?: 0,
            totalAutoExitCount = map["TOTAL_AUTO_EXIT_COUNT"]?.toIntOrNull() ?: 0,
            totalAutoExitRecover = map["TOTAL_AUTO_EXIT_RECOVER"]?.toIntOrNull() ?: 0,
            totalMaintenanceCount = map["TOTAL_MAINTENANCE_COUNT"]?.toIntOrNull() ?: 0,
            totalStateChangeCount = map["TOTAL_STATE_CHANGE_COUNT"]?.toIntOrNull() ?: 0,
            serviceStartTime = map["SERVICE_START_TIME"]?.toLongOrNull() ?: 0
        )
    }
    
    suspend fun saveStats(stats: Statistics) = statsMutex.withLock {
        withContext(Dispatchers.IO) {
            val content = buildString {
                appendLine("TOTAL_ENTER_COUNT=${stats.totalEnterCount}")
                appendLine("TOTAL_ENTER_SUCCESS=${stats.totalEnterSuccess}")
                appendLine("TOTAL_EXIT_COUNT=${stats.totalExitCount}")
                appendLine("TOTAL_EXIT_SUCCESS=${stats.totalExitSuccess}")
                appendLine("TOTAL_AUTO_EXIT_COUNT=${stats.totalAutoExitCount}")
                appendLine("TOTAL_AUTO_EXIT_RECOVER=${stats.totalAutoExitRecover}")
                appendLine("TOTAL_MAINTENANCE_COUNT=${stats.totalMaintenanceCount}")
                appendLine("TOTAL_STATE_CHANGE_COUNT=${stats.totalStateChangeCount}")
                appendLine("SERVICE_START_TIME=${stats.serviceStartTime}")
            }
            
            RootCommander.mkdir("/data/local/tmp/deep_sleep_logs")
            RootCommander.exec("printf '%s\\n' \"$content\" > $statsPath")
        }
    }
    
    suspend fun recordEnterAttempt() = statsMutex.withLock {
        val current = loadStats()
        saveStats(current.copy(totalEnterCount = current.totalEnterCount + 1))
    }
    
    suspend fun recordEnterSuccess() = statsMutex.withLock {
        val current = loadStats()
        saveStats(current.copy(totalEnterSuccess = current.totalEnterSuccess + 1))
    }
    
    suspend fun recordExitAttempt() = statsMutex.withLock {
        val current = loadStats()
        saveStats(current.copy(totalExitCount = current.totalExitCount + 1))
    }
    
    suspend fun recordExitSuccess() = statsMutex.withLock {
        val current = loadStats()
        saveStats(current.copy(totalExitSuccess = current.totalExitSuccess + 1))
    }
    
    suspend fun recordAutoExit() = statsMutex.withLock {
        val current = loadStats()
        saveStats(current.copy(totalAutoExitCount = current.totalAutoExitCount + 1))
    }
    
    suspend fun recordAutoExitRecovered() = statsMutex.withLock {
        val current = loadStats()
        saveStats(current.copy(totalAutoExitRecover = current.totalAutoExitRecover + 1))
    }
    
    suspend fun recordMaintenance() = statsMutex.withLock {
        val current = loadStats()
        saveStats(current.copy(totalMaintenanceCount = current.totalMaintenanceCount + 1))
    }
    
    suspend fun recordStateChange() = statsMutex.withLock {
        val current = loadStats()
        saveStats(current.copy(totalStateChangeCount = current.totalStateChangeCount + 1))
    }
    
    suspend fun resetStats() = statsMutex.withLock {
        withContext(Dispatchers.IO) {
            RootCommander.exec("rm -f $statsPath")
        }
    }
}