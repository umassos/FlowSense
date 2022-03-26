package edu.umasslass.healthyair.fftmicTraining.source

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.*

/**
 * Class to handle BLE communication with Arduino Nano 33 BLE Sense for groundtruth airflow
 *
 * @author Adam Lechowicz 08/2021
 */

class BLEComm(var mContext: Context) {

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val baseBluetoothUuidPostfix = "0000-1000-8000-00805F9B34FB";

    private var targetDevice: BluetoothDevice? = null

    public var airflow = "0.0"

    // From the previous section:
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }


    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
        properties and property != 0

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("FFTMic", "Successfully connected to $deviceAddress")
                    Handler(Looper.getMainLooper()).post {
                        gatt?.discoverServices()
                    }
                    // TODO: Store a reference to BluetoothGatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("FFTMic", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("FFTMic", "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.v("FFTMic", "Discovered ${services.size} services for ${device.address}")
                val characteristic = gatt.getService(UUID.fromString("7e889611-7c22-4017-96fc-5a2d23e4bcb3")).getCharacteristic(UUID.fromString("e4181542-6f68-461a-811a-e71f604bfa4f"))
                enableNotifications(gatt, characteristic)
            }
        }
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            with(characteristic) {
                var stringValue = String(value, Charsets.UTF_8)
                stringValue = stringValue.substring(0, (stringValue.indexOf('.')+3))
                airflow = stringValue
            }
        }
        fun writeDescriptor(gatt:BluetoothGatt, descriptor: BluetoothGattDescriptor, payload: ByteArray) {
            gatt.let { gatt ->
                descriptor.value = payload
                gatt.writeDescriptor(descriptor)
            } ?: error("Not connected to a BLE device!")
        }
        fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val payload = when {
                characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else -> {
                    Log.v("FFTMic", "${characteristic.uuid} doesn't support notifications/indications")
                    return
                }
            }
            characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                if (gatt.setCharacteristicNotification(characteristic, true) == false) {
                    Log.v("FFTMic", "setCharacteristicNotification failed for ${characteristic.uuid}")
                    return
                }
                writeDescriptor(gatt, cccDescriptor, payload)
            } ?: Log.e("FFTMic", "${characteristic.uuid} doesn't contain the CCC descriptor!")
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.v("FFTMic", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                targetDevice = result.device
            }
        }
    }

    fun init(){
        //Set Service UUIDs that we are looking for
        val filters = mutableListOf<ScanFilter?>()
        filters.add(ScanFilter.Builder().setServiceUuid(
            ParcelUuid.fromString("7e889611-7c22-4017-96fc-5a2d23e4bcb3")
        ).build())

        //Scan for devices that advertise this service UUID
        //bleScanner.startScan(scanCallback)
        bleScanner.startScan(filters, scanSettings, scanCallback)
        Handler().postDelayed({
            bleScanner.stopScan(scanCallback)
            with(targetDevice!!){
                val gatt = connectGatt(mContext, false, gattCallback, 2)
            }
        }, 1000)
    }

    fun blueAirflow(): String {
        return airflow
    }
}
