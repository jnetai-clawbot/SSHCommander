package com.jnetaol.sshcommander

import android.app.Application
import com.jnetaol.sshcommander.logger.DebugLogger

class SSHCommanderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugLogger.init(this)
        DebugLogger.i("SSHCommanderApp", "App started", "SC-APP-001", mapOf("version" to "1.0.0"))
    }

    override fun onTerminate() {
        DebugLogger.i("SSHCommanderApp", "Terminating", "SC-APP-002")
        DebugLogger.shutdown()
        super.onTerminate()
    }
}
