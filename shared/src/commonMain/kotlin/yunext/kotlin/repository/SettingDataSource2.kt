package yunext.kotlin.repository

import com.yunext.kotlin.kmp.ble.core.NotifyDescriptorUUID
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.core.bluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.bluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.bluetoothGattService
import com.yunext.kotlin.kmp.ble.slave.SlaveSetting
import com.yunext.kotlin.kmp.ble.util.display
import yunext.kotlin.domain.His
import com.yunext.kotlin.kmp.ble.util.domain.his.Sig
import yunext.kotlin.domain.base
import yunext.kotlin.domain.uuid
import yunext.kotlin.domain.uuid16bit
import com.yunext.kotlin.kmp.common.logger.HDLogger.Companion.d
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
object SettingDataSource2 {

    private val serviceCreator: (String, Array<PlatformBluetoothGattCharacteristic>) -> PlatformBluetoothGattService =
        { uuid, chs ->
            bluetoothGattService(
                Uuid.parse(uuid), PlatformBluetoothGattService.ServiceType.Primary,
                emptyArray(), chs
            )
        }

    private val descriptorCreator = { uuid: String, payload: ByteArray ->
        bluetoothGattDescriptor(
            Uuid.parse(uuid),
            arrayOf(
                PlatformBluetoothGattDescriptor.Permission.PermissionRead,
                PlatformBluetoothGattDescriptor.Permission.PermissionWrite
            ), payload
        )
    }

    private val characteristicCreator: (String, Array<PlatformBluetoothGattDescriptor>) -> PlatformBluetoothGattCharacteristic =
        { uuid, descriptor ->
            bluetoothGattCharacteristic(
                Uuid.parse(uuid),
                arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write
                ),
                arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                ),
                descriptor,
                byteArrayOf()
            )
        }

    fun createWaterSetting(name: String = "water", tsl: String = ""): SlaveSetting {
        return object : SlaveSetting {

            @OptIn(ExperimentalStdlibApi::class)
            override val deviceName: String =
                "${name}_${Random.Default.nextBytes(4).toHexString()}"

            override val broadcastService: PlatformBluetoothGattService = serviceCreator.invoke(
                His.BaseService.WaterDispenserService.uuid, emptyArray()
            )
            override val services: Array<PlatformBluetoothGattService> =
                waterDispenserProfileServices()
            override val broadcastTimeout: Long = 60_000

        }

    }


    private fun waterDispenserProfileServices(): Array<PlatformBluetoothGattService> {
        fun deviceInfoService(): Array<PlatformBluetoothGattCharacteristic> {
            val mac =
                bluetoothGattCharacteristic(
                    Uuid.parse(His.uuidOf("A001", His.BaseService.DeviceInfoService)),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Permission.Read,
                    ),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Property.Read,
                        PlatformBluetoothGattCharacteristic.Property.Notify,
                    ),
                    arrayOf(
                        descriptorCreator(
                            Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                            byteArrayOf(0x00)
                        ),
                        descriptorCreator(
                            Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                            "device mac".encodeToByteArray()
                        ),
                    ),
                    byteArrayOf()
                )
            val sn =
                bluetoothGattCharacteristic(
                    Uuid.parse(His.uuidOf("A002", His.BaseService.DeviceInfoService)),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Permission.Read,
                    ),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Property.Read,
                        PlatformBluetoothGattCharacteristic.Property.Notify,
                    ),
                    arrayOf(
                        descriptorCreator(
                            Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                            byteArrayOf(0x00)
                        ),
                        descriptorCreator(
                            Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                            "sn".encodeToByteArray()
                        ),
                    ),
                    byteArrayOf()
                )
            val version =
                bluetoothGattCharacteristic(
                    Uuid.parse(His.uuidOf("A003", His.BaseService.DeviceInfoService)),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Permission.Read,
                    ),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Property.Read,
                        PlatformBluetoothGattCharacteristic.Property.Notify,
                    ),
                    arrayOf(
                        descriptorCreator(
                            Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                            byteArrayOf(0x00)
                        ),
                        descriptorCreator(
                            Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                            "固件版本".encodeToByteArray()
                        ),
                    ),
                    byteArrayOf()
                )
            val protocolVersion =
                bluetoothGattCharacteristic(
                    Uuid.parse(His.uuidOf("A004", His.BaseService.DeviceInfoService)),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Permission.Read,
                    ),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Property.Read,
                        PlatformBluetoothGattCharacteristic.Property.Notify,
                    ),
                    arrayOf(
                        descriptorCreator(
                            Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                            byteArrayOf(0x00)
                        ),
                        descriptorCreator(
                            Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                            "协议版本".encodeToByteArray()
                        ),
                    ),
                    byteArrayOf()
                )
            return arrayOf(mac, sn, version, protocolVersion)
        }

        fun filterCharacteristics(): Array<PlatformBluetoothGattCharacteristic> {
            val filterLife1 =
                bluetoothGattCharacteristic(
                    uuid = Uuid.parse(
                        His.uuidOf(
                            His.filterService16bit(
                                1,
                                His.FilterServiceCharacteristic.FilterLifeN
                            ), His.BaseService.FilterService
                        )
                    ),

                    permissions = arrayOf(
                        PlatformBluetoothGattCharacteristic.Permission.Read,
                        PlatformBluetoothGattCharacteristic.Permission.Write,
                    ),
                    properties = arrayOf(
                        PlatformBluetoothGattCharacteristic.Property.Read,
                        PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                        PlatformBluetoothGattCharacteristic.Property.Notify,
                    ),
                    descriptors = arrayOf(
                        descriptorCreator(
                            Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                            byteArrayOf(0b11)
                        ),
                        descriptorCreator(
                            Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                            byteArrayOf(0x01,0x02,0x03,0x04)
                        ),
                        descriptorCreator(
                            His.uuidOf(
                                His.TslCharacteristicDescriptor.Base.uuid16bit,
                                His.BaseService.FilterService
                            ),  byteArrayOf(0x0a,0x0b,0x0c,0x0d)/*His.TslCharacteristicDescriptor.base(
                                read = true,
                                write = false,
                                required = true,
                                tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                                identifier = "filterLife1",
                                name = "滤芯1总寿命",
                                desc = "滤芯1总寿命",
                            )*/
                        ),
                        descriptorCreator(
                            His.uuidOf(
                                His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                                His.BaseService.FilterService
                            ),
                            His.TslCharacteristicDescriptor.intProperty(
                                min = 0,
                                max = 100,
                                unit = His.Unit.CE00
                            )
                        ),
                    ),
                    value = byteArrayOf(0x01)
                )

            val filterLife2 = bluetoothGattCharacteristic(
                uuid = Uuid.parse(
                    His.uuidOf(
                        His.filterService16bit(
                            2,
                            His.FilterServiceCharacteristic.FilterLifeN
                        ), His.BaseService.FilterService
                    )
                ),
                permissions = arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write,
                ),
                properties = arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                descriptors = arrayOf(
                    descriptorCreator(
                        Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        byteArrayOf(0x00)
                    ),
                    descriptorCreator(
                        Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                        "滤芯2总寿命".encodeToByteArray()
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.Base.uuid16bit,
                            His.BaseService.FilterService
                        ), His.TslCharacteristicDescriptor.base(
                            read = true,
                            write = false,
                            required = true,
                            tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                            identifier = "filterLife2",
                            name = "滤芯2总寿命",
                            desc = "滤芯2总寿命",
                        )
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                            His.BaseService.FilterService
                        ),
                        His.TslCharacteristicDescriptor.intProperty(
                            min = 0,
                            max = 100,
                            unit = His.Unit.CE00
                        )
                    ),
                ),
                value = byteArrayOf()
            )

            val filterPercent1 = bluetoothGattCharacteristic(
                uuid = Uuid.parse(
                    His.uuidOf(
                        His.filterService16bit(
                            1,
                            His.FilterServiceCharacteristic.FilterPercentN
                        ), His.BaseService.FilterService
                    )
                ),
                permissions = arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write,
                ),
                properties = arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                descriptors = arrayOf(
                    descriptorCreator(
                        Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        byteArrayOf(0x00)
                    ),
                    descriptorCreator(
                        Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                        "滤芯1百分比".encodeToByteArray()
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.Base.uuid16bit,
                            His.BaseService.FilterService
                        ), His.TslCharacteristicDescriptor.base(
                            read = true,
                            write = false,
                            required = true,
                            tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                            identifier = "filterPercent1",
                            name = "滤芯1百分比",
                            desc = "滤芯1百分比",
                        )
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                            His.BaseService.FilterService
                        ),
                        His.TslCharacteristicDescriptor.intProperty(
                            min = 0,
                            max = 100,
                            unit = His.Unit.CE00
                        )
                    ),
                ),
                value = byteArrayOf()
            )

            val filterPercent2 = bluetoothGattCharacteristic(
                uuid = Uuid.parse(
                    His.uuidOf(
                        His.filterService16bit(
                            2,
                            His.FilterServiceCharacteristic.FilterPercentN
                        ), His.BaseService.FilterService
                    )
                ),
                permissions = arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write,
                ),
                properties = arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                descriptors = arrayOf(
                    descriptorCreator(
                        Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        byteArrayOf(0x00)
                    ),
                    descriptorCreator(
                        Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                        "滤芯2百分比".encodeToByteArray()
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.Base.uuid16bit,
                            His.BaseService.FilterService
                        ), His.TslCharacteristicDescriptor.base(
                            read = true,
                            write = false,
                            required = true,
                            tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                            identifier = "filterPercent2",
                            name = "滤芯2百分比",
                            desc = "滤芯2百分比",
                        )
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                            His.BaseService.FilterService
                        ),
                        His.TslCharacteristicDescriptor.intProperty(
                            min = 0,
                            max = 100,
                            unit = His.Unit.CE00
                        )
                    ),
                ),
                value = byteArrayOf()
            )

            val filterExpWater1 = bluetoothGattCharacteristic(
                uuid = Uuid.parse(
                    His.uuidOf(
                        His.filterService16bit(
                            1,
                            His.FilterServiceCharacteristic.FilterExpWaterN
                        ), His.BaseService.FilterService
                    )
                ),
                permissions = arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write,
                ),
                properties = arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                descriptors = arrayOf(
                    descriptorCreator(
                        Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        byteArrayOf(0x00)
                    ),
                    descriptorCreator(
                        Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                        "滤芯1预计水量".encodeToByteArray()
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.Base.uuid16bit,
                            His.BaseService.FilterService
                        ), His.TslCharacteristicDescriptor.base(
                            read = true,
                            write = false,
                            required = true,
                            tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                            identifier = "filterExpWater1",
                            name = "滤芯1预计水量",
                            desc = "滤芯1预计水量",
                        )
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                            His.BaseService.FilterService
                        ),
                        His.TslCharacteristicDescriptor.intProperty(
                            min = 0,
                            max = 0xffff,
                            unit = His.Unit.CE01
                        )
                    ),

                    ),
                value = byteArrayOf()
            )

            val filterExpWater2 = bluetoothGattCharacteristic(
                uuid = Uuid.parse(
                    His.uuidOf(
                        His.filterService16bit(
                            2,
                            His.FilterServiceCharacteristic.FilterExpWaterN
                        ), His.BaseService.FilterService
                    )
                ),
                permissions = arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write,
                ),
                properties = arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                descriptors = arrayOf(
                    descriptorCreator(
                        Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        byteArrayOf(0x00)
                    ),
                    descriptorCreator(
                        Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                        "滤芯2预计水量".encodeToByteArray()
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.Base.uuid16bit,
                            His.BaseService.FilterService
                        ), His.TslCharacteristicDescriptor.base(
                            read = true,
                            write = false,
                            required = true,
                            tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                            identifier = "filterExpWater2",
                            name = "预计2额定水量",
                            desc = "预计2额定水量",
                        )
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                            His.BaseService.FilterService
                        ),
                        His.TslCharacteristicDescriptor.intProperty(
                            min = 0,
                            max = 0xffff,
                            unit = His.Unit.CE01
                        )
                    ),
                ),
                value = byteArrayOf()
            )

            val filterRatWater1 = bluetoothGattCharacteristic(
                uuid = Uuid.parse(
                    His.uuidOf(
                        His.filterService16bit(
                            1,
                            His.FilterServiceCharacteristic.FilterRatWaterN
                        ), His.BaseService.FilterService
                    )
                ),
                permissions = arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write,
                ),
                properties = arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                descriptors = arrayOf(
                    descriptorCreator(
                        Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        byteArrayOf(0x00)
                    ),
                    descriptorCreator(
                        Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                        "滤芯1额定水量".encodeToByteArray()
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.Base.uuid16bit,
                            His.BaseService.FilterService
                        ), His.TslCharacteristicDescriptor.base(
                            read = true,
                            write = false,
                            required = true,
                            tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                            identifier = "filterRatWater1",
                            name = "滤芯1额定水量",
                            desc = "滤芯1额定水量",
                        )
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                            His.BaseService.FilterService
                        ),
                        His.TslCharacteristicDescriptor.intProperty(
                            min = 0,
                            max = 0xffff,
                            unit = His.Unit.CE01
                        )
                    ),
                ),
                value = byteArrayOf()
            )

            val filterRatWater2 = bluetoothGattCharacteristic(
                uuid = Uuid.parse(
                    His.uuidOf(
                        His.filterService16bit(
                            2,
                            His.FilterServiceCharacteristic.FilterRatWaterN
                        ), His.BaseService.FilterService
                    )
                ),
                permissions = arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write,
                ),
                properties = arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                descriptors = arrayOf(
                    descriptorCreator(
                        Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        byteArrayOf(0x00)
                    ),
                    descriptorCreator(
                        Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                        "滤芯2额定水量".encodeToByteArray()
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.Base.uuid16bit,
                            His.BaseService.FilterService
                        ), His.TslCharacteristicDescriptor.base(
                            read = true,
                            write = false,
                            required = true,
                            tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                            identifier = "filterRatWater2",
                            name = "滤芯2额定水量",
                            desc = "滤芯2额定水量",
                        )
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                            His.BaseService.FilterService
                        ),
                        His.TslCharacteristicDescriptor.intProperty(
                            min = 0,
                            max = 0xffff,
                            unit = His.Unit.CE01
                        )
                    ),
                ),
                value = byteArrayOf()
            )

            val filterError = bluetoothGattCharacteristic(
                uuid = Uuid.parse(
                    His.uuidOf(
                        His.FilterServiceCharacteristic.FilterError.base,
                        His.BaseService.FilterService
                    )
                ),
                permissions = arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write,
                ),
                properties = arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                descriptors = arrayOf(
                    descriptorCreator(
                        Sig.UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        byteArrayOf(0x00)
                    ),
                    descriptorCreator(
                        Sig.UUID_CHARACTERISTIC_USER_DESCRIPTION,
                        "filterError异常".encodeToByteArray()
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.Base.uuid16bit,
                            His.BaseService.FilterService
                        ), His.TslCharacteristicDescriptor.base(
                            read = true,
                            write = false,
                            required = true,
                            tslCharacteristicDescriptor = His.TslCharacteristicDescriptor.Base,
                            identifier = "filterError",
                            name = "filterError异常",
                            desc = "filterError异常",
                        )
                    ),
                    descriptorCreator(
                        His.uuidOf(
                            His.TslCharacteristicDescriptor.IntProperty.uuid16bit,
                            His.BaseService.FilterService
                        ),
                        His.TslCharacteristicDescriptor.intProperty(
                            min = 0,
                            max = 0xffff,
                            unit = His.Unit.CE00
                        )
                    ),
                ),
                value = byteArrayOf()
            )

            return arrayOf(
                filterLife1,
                filterLife2,
                filterPercent1,
                filterPercent2,
                filterExpWater1,
                filterExpWater2,
                filterRatWater1,
                filterRatWater2, filterError
            )
        }

        fun waterDispatcherCharacteristics(): Array<PlatformBluetoothGattCharacteristic> {
            return emptyArray()
        }

        val deviceInfoService =
            serviceCreator(
                His.BaseService.DeviceInfoService.uuid,
                deviceInfoService()
            )
        val waterDispatcherService =
            serviceCreator(
                His.BaseService.WaterDispenserService.uuid,
                waterDispatcherCharacteristics()
            )
        val filterService =
            serviceCreator(
                His.BaseService.FilterService.uuid,
                filterCharacteristics()
            )
        return arrayOf(deviceInfoService, filterService)
    }

    fun angelService(): PlatformBluetoothGattService {


        val serviceCreator: (String, Array<PlatformBluetoothGattCharacteristic>) -> PlatformBluetoothGattService =
            { uuid, chs ->
                bluetoothGattService(
                    Uuid.parse(uuid), PlatformBluetoothGattService.ServiceType.Primary,
                    emptyArray(), chs
                )
            }
        val write =

            bluetoothGattCharacteristic(
                Uuid.parse("616e6765-6c62-6c65-6e6f-746964796368"),
                arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write
                ),
                arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                ),
                arrayOf(descriptorCreator(NotifyDescriptorUUID, byteArrayOf())),
                byteArrayOf()
            )

        val notify =
            bluetoothGattCharacteristic(
                Uuid.parse("616e6765-6c62-6c65-7365-6e6463686172"),
                arrayOf(
                    PlatformBluetoothGattCharacteristic.Permission.Read,
                    PlatformBluetoothGattCharacteristic.Permission.Write
                ),
                arrayOf(
                    PlatformBluetoothGattCharacteristic.Property.Read,
                    PlatformBluetoothGattCharacteristic.Property.Notify,
                ),
                arrayOf(descriptorCreator(NotifyDescriptorUUID, byteArrayOf())),
                byteArrayOf()
            )
        return serviceCreator("616e6765-6c62-6c70-6573-657276696365", arrayOf(write, notify))
    }

    fun generateServiceRandom(): PlatformBluetoothGattService {
        val descriptorCreator = {
            bluetoothGattDescriptor(
                Uuid.random(),
                arrayOf(
                    PlatformBluetoothGattDescriptor.Permission.PermissionRead,
                    PlatformBluetoothGattDescriptor.Permission.PermissionWrite
                ), byteArrayOf()
            )
        }
        val characteristicCreator: (Array<PlatformBluetoothGattDescriptor>) -> PlatformBluetoothGattCharacteristic =
            {
                bluetoothGattCharacteristic(
                    Uuid.random(),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Permission.Read,
                        PlatformBluetoothGattCharacteristic.Permission.Write
                    ),
                    arrayOf(
                        PlatformBluetoothGattCharacteristic.Property.Notify,
                        PlatformBluetoothGattCharacteristic.Property.Read,
                        PlatformBluetoothGattCharacteristic.Property.WriteNoResponse,
                        PlatformBluetoothGattCharacteristic.Property.Write
                    ),
                    it,
                    byteArrayOf()
                )
            }
        val serviceCreator: (Array<PlatformBluetoothGattCharacteristic>) -> PlatformBluetoothGattService =
            {
                bluetoothGattService(
                    Uuid.random(), PlatformBluetoothGattService.ServiceType.Primary,
                    emptyArray(), it
                )
            }

        return serviceCreator((0..10).map {
            characteristicCreator(arrayOf(descriptorCreator()))
        }.toTypedArray())
    }

    fun generateServicesRandom(): List<PlatformBluetoothGattService> {
        val services = (0..5).map {
            generateServiceRandom()
        }
        return services.apply {
            d("[BLE]", this.display)
        }
    }


    val setting = object : SlaveSetting {
        @OptIn(ExperimentalStdlibApi::class)
//        override val deviceName: String = "angel_${Random.Default.nextBytes(3).toHexString()}"
//        override val deviceName: String = "B#QY#ZR2P2570#20C590"
        override val deviceName: String = "angel_84C2E4030202"

        //        override val deviceName: String = "B#QY#${Random.Default.nextBytes(4).toHexString()}#${
//            Random.Default.nextBytes(3).toHexString()
//        }"
        override val broadcastService: PlatformBluetoothGattService = angelService()
        override val services: Array<PlatformBluetoothGattService> =
//            generateServices().toTypedArray()
            arrayOf(angelService())
        override val broadcastTimeout: Long = 60_000

    }
}