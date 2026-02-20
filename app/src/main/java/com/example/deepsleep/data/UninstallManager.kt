package com.example.deepsleep.data

import android.content.Context
import com.example.deepsleep.root.BackgroundOptimizer
import com.example.deepsleep.root.DozeController
import com.example.deepsleep.root.RootCommander
import com.example.deepsleep.root.WaltOptimizer

class UninstallManager(private val context: Context) {
    
    suspend fun performUninstall(): UninstallResult {
        return try {
            // 1. 停止服务
            stopService()
            
            // 2. 恢复 motion
            DozeController.enableMotion()
            
            // 3. 退出深度睡眠
            DozeController.exitDeepSleep()
            
            // 4. 恢复所有应用后台权限
            BackgroundOptimizer.restoreAll()
            
            // 5. 恢复 WALT 参数
            WaltOptimizer.restoreDefault()
            
            // 6. 清理系统属性
            cleanupProperties()
            
            // 7. 删除所有文件
            cleanupFiles()
            
            UninstallResult.Success
        } catch (e: Exception) {
            UninstallResult.Error(e.message ?: "未知错误")
        }
    }
    
    private suspend fun stopService() {
        RootCommander.exec("am stopservice -n ${context.packageName}/.service.DeepSleepService")
    }
    
    private suspend fun cleanupProperties() {
        RootCommander.exec(
            "setprop persist.sys.doze.quick '' 2>/dev/null || true",
            "setprop debug.performance.tuning '' 2>/dev/null || true",
            "setprop persist.deep_sleep.opt_bg '' 2>/dev/null || true"
        )
    }
    
    private suspend fun cleanupFiles() {
        RootCommander.exec(
            "rm -rf /data/local/tmp/deep_sleep_logs",
            "rm -f /data/local/tmp/deep_sleep.pid"
        )
    }
}

sealed class UninstallResult {
    object Success : UninstallResult()
    data class Error(val message: String) : UninstallResult()
}
