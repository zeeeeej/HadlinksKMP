package com.yunext.kotlin.kmp.ble.slave

interface SlaveEventHandler {
    fun handle(slave: PlatformSlave, event: PlatformSlaveEvent)
}



