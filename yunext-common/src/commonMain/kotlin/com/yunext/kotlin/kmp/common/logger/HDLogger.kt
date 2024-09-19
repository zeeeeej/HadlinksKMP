package com.yunext.kotlin.kmp.common.logger


interface XLog {
    fun d(msg: String)

    fun i(msg: String)

    fun w(msg: String)

    fun e(msg: String)
}

enum class XLogType{
    D,I,W,E;
}

class HDLog(
    private val globalTag: String,
    private val debug: Boolean,
    private val pre: String.(XLogType) -> String = {this},
) : XLog {


    override fun d(msg: String) {
        if (!debug) return
        HDLogger.d(globalTag, pre(msg, XLogType.D))
    }

    override fun i(msg: String) {
        if (!debug) return
        HDLogger.i(globalTag, pre(msg, XLogType.I))
    }

    override fun w(msg: String) {
        if (!debug) return
        HDLogger.w(globalTag, pre(msg, XLogType.W))
    }

    override fun e(msg: String) {
        if (!debug) return
        HDLogger.e(globalTag, pre(msg, XLogType.E))
    }


}

expect interface HDLogger {
    var debug: Boolean
    fun d(tag: String, msg: String)
    fun i(tag: String, msg: String)
    fun w(tag: String, msg: String)
    fun e(tag: String, msg: String)

    companion object : HDLogger {

        override var debug: Boolean 

        override fun d(tag: String, msg: String)

        override fun i(tag: String, msg: String)

        override fun w(tag: String, msg: String)

        override fun e(tag: String, msg: String)

    }
}