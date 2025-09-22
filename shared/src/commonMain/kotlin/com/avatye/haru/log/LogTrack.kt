package com.avatye.haru.log

import com.caffeine.common.sdk.log.LogTracer
import com.caffeine.common.sdk.log.LoggerSettings

object LogTrack {
    fun initializeLog(moduleName: String?, allowLog: Boolean) = LoggerSettings.initialize(moduleName, allowLog)

    fun i(tag: String? = null, trace: () -> String) = LogTracer.i(tag, trace)
    fun d(tag: String? = null, trace: () -> String) = LogTracer.d(tag, trace)
    fun v(tag: String? = null, trace: () -> String) = LogTracer.v(tag, trace)
    fun w(tag: String? = null, trace: () -> String) = LogTracer.w(tag, trace)
    fun e(tag: String? = null, trace: () -> String) = LogTracer.e(tag, trace)

    fun i(trace: () -> String) = LogTracer.i(null, trace)
    fun d(trace: () -> String) = LogTracer.d(null, trace)
    fun v(trace: () -> String) = LogTracer.v(null, trace)
    fun w(trace: () -> String) = LogTracer.w(null, trace)
    fun e(trace: () -> String) = LogTracer.e(null, trace)
}
