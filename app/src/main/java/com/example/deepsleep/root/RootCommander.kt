package com.example.deepsleep.root

import com.example.deepsleep.BuildConfig
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootCommander {

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }

    // 确保 Shell 已连接，如果未连接则尝试连接
    private suspend fun ensureShell(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 主动请求 root 授权（触发 Magisk 弹窗）
    suspend fun requestRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        if (!ensureShell()) return@withContext false
        Shell.cmd("id").exec().isSuccess
    }

    suspend fun exec(command: String): Shell.Result = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd(command).exec()
    }

    suspend fun exec(vararg commands: String): Shell.Result = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd(*commands).exec()
    }

    suspend fun execBatch(commands: List<String>): Shell.Result = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd(*commands.toTypedArray()).exec()
    }

    suspend fun safeWrite(path: String, value: String): Boolean = withContext(Dispatchers.IO) {
        ensureShell()
        val result = Shell.cmd("printf '%s' \"$value\" > $path").exec()
        result.isSuccess
    }

    suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        ensureShell()
        val result = Shell.cmd("cat $path 2>/dev/null").exec()
        if (result.isSuccess) result.out.joinToString("\n") else null
    }

    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd("[ -f $path ]").exec().isSuccess
    }

    suspend fun mkdir(path: String): Boolean = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd("mkdir -p $path").exec().isSuccess
    }
}