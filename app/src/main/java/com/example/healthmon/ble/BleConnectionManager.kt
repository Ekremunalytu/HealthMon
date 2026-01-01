package com.example.healthmon.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ESP32 ile GATT bağlantısını yöneten sınıf
 */
@Singleton
class BleConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BleConnectionManager"
    }
    
    private var bluetoothGatt: BluetoothGatt? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _sensorData = MutableSharedFlow<SensorData>(replay = 1)
    val sensorData: SharedFlow<SensorData> = _sensorData.asSharedFlow()
    
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "ESP32'ye bağlandı!")
                    _connectionState.value = ConnectionState.Connected
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Bağlantı kesildi")
                    _connectionState.value = ConnectionState.Disconnected
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                }
            }
        }
        
        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Servisler keşfedildi")
                enableNotifications(gatt)
            } else {
                Log.e(TAG, "Service discovery failed: $status")
                _connectionState.value = ConnectionState.Error("Servis bulunamadı")
            }
        }
        
        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == BleConstants.SENSOR_DATA_UUID) {
                val data = characteristic.value
                val jsonString = String(data, Charsets.UTF_8)
                Log.d(TAG, "Veri alındı: $jsonString")
                
                SensorDataParser.parse(jsonString)?.let { sensorData ->
                    scope.launch {
                        _sensorData.emit(sensorData)
                    }
                }
            }
        }
        
        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Notification aktif!")
                _connectionState.value = ConnectionState.Streaming
            }
        }
    }
    
    /**
     * ESP32'ye bağlan
     */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "ESP32'ye bağlanılıyor: ${device.address}")
        _connectionState.value = ConnectionState.Connecting
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }
    
    /**
     * Notification'ları aktif et
     */
    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt) {
        val service = gatt.getService(BleConstants.SERVICE_UUID)
        if (service == null) {
            Log.e(TAG, "Service bulunamadı: ${BleConstants.SERVICE_UUID}")
            _connectionState.value = ConnectionState.Error("Service bulunamadı")
            return
        }
        
        val characteristic = service.getCharacteristic(BleConstants.SENSOR_DATA_UUID)
        if (characteristic == null) {
            Log.e(TAG, "Characteristic bulunamadı!")
            _connectionState.value = ConnectionState.Error("Characteristic bulunamadı")
            return
        }
        
        gatt.setCharacteristicNotification(characteristic, true)
        
        val descriptor = characteristic.getDescriptor(BleConstants.CCCD_UUID)
        if (descriptor != null) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }
    
    /**
     * Bağlantıyı kes
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    /**
     * Bağlı mı kontrol et
     */
    fun isConnected(): Boolean = _connectionState.value is ConnectionState.Streaming
}

/**
 * BLE bağlantı durumları
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Streaming : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * ESP32'den gelen sensör verisi
 */
data class SensorData(
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f,
    val ppg: Int,
    val timestamp: Long = System.currentTimeMillis()
)
