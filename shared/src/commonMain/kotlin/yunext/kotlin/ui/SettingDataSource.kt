package yunext.kotlin.ui

import com.yunext.kotlin.kmp.ble.core.NotifyDescriptorUUID
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.core.bluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.bluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.bluetoothGattService
import com.yunext.kotlin.kmp.ble.slave.SlaveSetting
import com.yunext.kotlin.kmp.ble.util.display
import com.yunext.kotlin.kmp.common.logger.HDLogger.Companion.d
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
object SettingDataSource {

    fun angelService(): PlatformBluetoothGattService {
        val descriptorCreator = { uuid: String ->
            bluetoothGattDescriptor(
                Uuid.parse(uuid),
                arrayOf(
                    PlatformBluetoothGattDescriptor.Permission.PermissionRead,
                    PlatformBluetoothGattDescriptor.Permission.PermissionWrite
                ), byteArrayOf()
            )
        }
        val characteristicCreator: (String, Array<PlatformBluetoothGattDescriptor>) -> PlatformBluetoothGattCharacteristic =
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
                arrayOf(descriptorCreator(NotifyDescriptorUUID)),
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
                arrayOf(descriptorCreator(NotifyDescriptorUUID)),
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
        override val deviceName: String = "angel_${Random.Default.nextBytes(3).toHexString()}"
        override val broadcastService: PlatformBluetoothGattService = angelService()
        override val services: Array<PlatformBluetoothGattService> =
//            generateServices().toTypedArray()
            arrayOf(angelService())
        override val broadcastTimeout: Long = 60_000

    }
}