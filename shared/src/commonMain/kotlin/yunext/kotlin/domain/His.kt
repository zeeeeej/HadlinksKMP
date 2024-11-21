package yunext.kotlin.domain

import yunext.kotlin.util.applyValueAtIndex
import yunext.kotlin.util.bitApply0
import yunext.kotlin.util.bitApply1

class His {

    enum class BaseService {
        HIS,
        AuthService,
        OtaService,
        SerialPortService,
        DeviceInfoService,
        WaterDispenserService,
        FilterService
        ;
    }

    enum class TslCharacteristicDescriptor {
        Base,
        IntProperty,
        FloatProperty,
        LongProperty,
        BooleanProperty,
        EnumProperty,
        TextProperty,
        ListProperty,
        StructProperty,
        Service,
        Event,
        ;

        companion object {
            fun base(
                read: Boolean,
                write: Boolean,
                required: Boolean = true,
                tslCharacteristicDescriptor: TslCharacteristicDescriptor,
                identifier: String,
                name: String,
                desc: String,
            ): ByteArray {

                val readWriteAndRequiredByte = 0.toByte().run {
                    val r = if (read) {
                        this.bitApply0(1)
                    } else {
                        this.bitApply1(0)
                    }

                    val r1 = if (write) {
                        r.bitApply0(1)
                    } else {
                        r.bitApply1(1)
                    }

                    if (required) {
                        r1.bitApply0(2)
                    } else {
                        r1.bitApply1(2)
                    }
                }

                val propertyTypeBytes = tslCharacteristicDescriptor.uuid16bitByteArray
                val identifierBytes = identifier.encodeToByteArray()
                val nameBytes = name.encodeToByteArray()
                val descBytes = desc.encodeToByteArray()
                val lengthBytes = byteArrayOf(
                    identifierBytes.size.toByte(),
                    nameBytes.size.toByte(),
                    descBytes.size.toByte()
                )
                return byteArrayOf(readWriteAndRequiredByte) + propertyTypeBytes +
                        lengthBytes + identifierBytes + nameBytes + descBytes

            }

            fun intProperty(min: Int, max: Int, unit: Unit): ByteArray {
                val minBytes = min.toByte()
                val maxBytes = max.toByte()
                val unitBytes = unit.uuid16bitByteArray
                return byteArrayOf(minBytes, maxBytes) + unitBytes
            }
        }
    }

    enum class Unit {
        CE00, CE01, CE02, CE03, CE04, CE05, CE06, CE07, CE08, CE09, CE0A, CE0B, CE0C
        ;
    }

    enum class Desc {
        DE01,
        ;
    }

    enum class FilterServiceCharacteristic {
        FilterLifeN,
        FilterPercentN,
        FilterExpWaterN,
        FilterRatWaterN,
        FilterError
        ;
    }

    companion object {


        fun uuidOf(bit16: String, baseService: BaseService): String {
            return baseService.uuid.replaceRange(4..7, bit16)
        }


        fun filterService16bit(
            index: Int,
            filterServiceCharacteristic: FilterServiceCharacteristic
        ): String {
            require(index in 1..0xF) {
                "index错误:$index"
            }
            @OptIn(ExperimentalStdlibApi::class)
            val result =
                filterServiceCharacteristic.baseInt.applyValueAtIndex(2,index).toHexString().takeLast(4)
            println("uuidOfFilterService16bit index:$index filterServiceCharacteristic:$filterServiceCharacteristic result=$result")
            return result
        }
    }
}

private const val BASE_UUID_HIS = "00000000-0000-1000-6864-79756e657874"
private const val BASE_UUID_WATER_DISPENSER_SERVICE = "00000000-1000-1000-6864-79756e657874"
private const val BASE_UUID_FILTER_SERVICE = "00000000-1001-1000-6864-79756e657874"
private const val BASE_UUID_DEVICE_INFO_SERVICE = "00000000-0005-1000-6864-79756e657874"
private const val BASE_UUID_AUTH_SERVICE = "00000000-0001-1000-6864-79756e657874"
private const val BASE_UUID_OTA_SERVICE = "00000000-0002-1000-6864-79756e657874"
private const val BASE_UUID_SERIAL_PORT_SERVICE = "00000000-0003-1000-6864-79756e657874"

val His.BaseService.uuid: String
    get() = when (this) {
        His.BaseService.HIS -> BASE_UUID_HIS
        His.BaseService.AuthService -> BASE_UUID_AUTH_SERVICE
        His.BaseService.OtaService -> BASE_UUID_OTA_SERVICE
        His.BaseService.SerialPortService -> BASE_UUID_SERIAL_PORT_SERVICE
        His.BaseService.DeviceInfoService -> BASE_UUID_DEVICE_INFO_SERVICE
        His.BaseService.WaterDispenserService -> BASE_UUID_WATER_DISPENSER_SERVICE
        His.BaseService.FilterService -> BASE_UUID_FILTER_SERVICE
    }

val His.Unit.uuid16bit: String
    get() = this.name

@OptIn(ExperimentalStdlibApi::class)
val His.Unit.uuid16bitByteArray: ByteArray
    get() = this.name.hexToByteArray()

val His.Desc.text: String
    get() = when (this) {
        His.Desc.DE01 -> "todo"
    }

val His.Desc.uuid16bit: String
    get() = this.name

val His.TslCharacteristicDescriptor.uuid16bit: String
    get() = when (this) {
        His.TslCharacteristicDescriptor.Base -> "EE01"
        His.TslCharacteristicDescriptor.IntProperty -> "EE02"
        His.TslCharacteristicDescriptor.FloatProperty -> "EE03"
        His.TslCharacteristicDescriptor.LongProperty -> "EE04"
        His.TslCharacteristicDescriptor.BooleanProperty -> "EE05"
        His.TslCharacteristicDescriptor.EnumProperty -> "EE06"
        His.TslCharacteristicDescriptor.TextProperty -> "EE07"
        His.TslCharacteristicDescriptor.ListProperty -> "EE09"
        His.TslCharacteristicDescriptor.StructProperty -> "EE08"
        His.TslCharacteristicDescriptor.Service -> "EE0B"
        His.TslCharacteristicDescriptor.Event -> "EE0A"
    }

val His.TslCharacteristicDescriptor.uuid16bitByteArray: ByteArray
    get() = when (this) {
        His.TslCharacteristicDescriptor.Base -> byteArrayOf(0xEE.toByte(), 0x01)
        His.TslCharacteristicDescriptor.IntProperty -> byteArrayOf(0xEE.toByte(), 0x02)
        His.TslCharacteristicDescriptor.FloatProperty -> byteArrayOf(0xEE.toByte(), 0x03)
        His.TslCharacteristicDescriptor.LongProperty -> byteArrayOf(0xEE.toByte(), 0x04)
        His.TslCharacteristicDescriptor.BooleanProperty -> byteArrayOf(0xEE.toByte(), 0x05)
        His.TslCharacteristicDescriptor.EnumProperty -> byteArrayOf(0xEE.toByte(), 0x06)
        His.TslCharacteristicDescriptor.TextProperty -> byteArrayOf(0xEE.toByte(), 0x07)
        His.TslCharacteristicDescriptor.ListProperty -> byteArrayOf(0xEE.toByte(), 0x09)
        His.TslCharacteristicDescriptor.StructProperty -> byteArrayOf(0xEE.toByte(), 0x08)
        His.TslCharacteristicDescriptor.Service -> byteArrayOf(0xEE.toByte(), 0x0B)
        His.TslCharacteristicDescriptor.Event -> byteArrayOf(0xEE.toByte(), 0x0A)
    }


val His.FilterServiceCharacteristic.base: String
    get() = when (this) {
        His.FilterServiceCharacteristic.FilterLifeN -> "A001"
        His.FilterServiceCharacteristic.FilterPercentN -> "A002"
        His.FilterServiceCharacteristic.FilterExpWaterN -> "A003"
        His.FilterServiceCharacteristic.FilterRatWaterN -> "A004"
        His.FilterServiceCharacteristic.FilterError -> "E001"
    }

val His.FilterServiceCharacteristic.baseInt: Int
    get() = when (this) {
        His.FilterServiceCharacteristic.FilterLifeN -> 0xA001
        His.FilterServiceCharacteristic.FilterPercentN -> 0xA002
        His.FilterServiceCharacteristic.FilterExpWaterN -> 0xA003
        His.FilterServiceCharacteristic.FilterRatWaterN -> 0xA004
        His.FilterServiceCharacteristic.FilterError -> 0xE001
    }
