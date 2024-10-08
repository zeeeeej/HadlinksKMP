package com.yunext.kotlin.kmp.ble.util

import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
internal fun Uuid.toUUID(): UUID = this.toLongs { mostSignificantBits, leastSignificantBits ->
    UUID(mostSignificantBits, leastSignificantBits)
}

@OptIn(ExperimentalUuidApi::class)
internal fun UUID.toUuid(): Uuid = this.toKotlinUuid()