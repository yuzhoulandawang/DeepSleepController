package com.example.deepsleep.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.deepsleep.model.LogEntry
import com.example.deepsleep.root.RootCommander
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogRepository {
    
    private val logDir = "/data/local/tmp/deep_sleep_logs"
    private val logPath = "$logDir/main.log"
    private val mutex = Mutex()
    
    suspend fun readLogs(): List<LogEntry> = withContext(Dispatchers.IO) {
        val content = RootCommander.readFile(logPath) ?: return@withContext emptyList()
        
        content.lineSequence()
            .filter { it.isNotBlank() }
            .map { line ->
                val regex = "\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\] (.*)".toRegex()
                val match = regex.find(line)
                if (match != null) {
                    LogEntry(
                        timestamp = match.groupValues[1],
                        content = match.groupValues[2]
                    )
                } else {
                    LogEntry(timestamp = "", content = line)
                }
            }
            .toList()
    }
    
    suspend fun appendLog(message: String) = mutex.withLock {
        withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val logLine = "[$timestamp] $message"
            
            RootCommander.mkdir(logDir)
            RootCommander.exec("printf '%s\\n' \"$logLine\" >> $logPath")
            
            rotateLogsIfNeeded()
        }
    }
    
    suspend fun clearLogs(): Boolean = mutex.withLock {
        withContext(Dispatchers.IO) {
            RootCommander.exec("echo '' > $logPath").isSuccess
        }
    }
    
    // 添加 suspend 关键字
    suspend fun getLogSize(): String {
        val result = RootCommander.exec("wc -c $logPath 2>/dev/null")
        val bytes = result.out.firstOrNull()?.trim()?.split(" ")?.firstOrNull()?.toLongOrNull() ?: 0
        
        return when {
            bytes > 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            bytes > 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
    
    suspend fun createShareableFile(context: Context): Uri? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "logs")
            cacheDir.mkdirs()
            val destFile = File(cacheDir, "deep_sleep_logs_${System.currentTimeMillis()}.txt")
            
            RootCommander.exec("cp $logPath ${destFile.absolutePath} 2>/dev/null")
            RootCommander.exec("chmod 644 ${destFile.absolutePath} 2>/dev/null")
            
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destFile)
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun rotateLogsIfNeeded() {
        if (!RootCommander.fileExists(logPath)) return
        val sizeResult = RootCommander.exec("wc -c $logPath")
        val bytes = sizeResult.out.firstOrNull()?.trim()?.split(" ")?.firstOrNull()?.toLongOrNull() ?: 0
        
        if (bytes > 2 * 1024 * 1024) {
            RootCommander.exec("tail -n 1000 $logPath > ${logPath}.tmp && mv ${logPath}.tmp $logPath")
        }
    }
}