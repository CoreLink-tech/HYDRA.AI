package com.zenlink.hydra

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Minimal on-device crash recorder. When any thread dies from an uncaught exception, the stack
 * trace is written to a private file. [MainActivity] shows it on the next launch so a crash can be
 * reported without adb or Developer options. Native (SIGSEGV) crashes are not captured here.
 */
object CrashLog {
    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
                val report = buildString {
                    appendLine("HYDRA crash report")
                    appendLine("Time: ${java.util.Date()}")
                    appendLine("Thread: ${thread.name}")
                    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                    appendLine("ABIs: ${Build.SUPPORTED_ABIS.joinToString()}")
                    appendLine("App: ${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME}")
                    appendLine()
                    append(trace)
                }
                file(app).writeText(report)
            }
            previous?.uncaughtException(thread, error)
        }
    }

    /** Returns the saved report, or null when the last run did not crash. */
    fun read(context: Context): String? =
        file(context.applicationContext).takeIf { it.exists() }?.readText()

    fun clear(context: Context) {
        file(context.applicationContext).delete()
    }

    private fun file(context: Context) = File(context.filesDir, FILE_NAME)
}
