package com.yunext.kotlin.kmp.context

val hdContext: HDContext = HDContext()

fun registerHDContext(block: HDContext.() -> Unit) {
    try {
        hdContext.block()
    } catch (e: Throwable) {
        throw HDContextException(message = null, cause = e)
    }
}