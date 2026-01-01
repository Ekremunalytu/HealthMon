package com.example.healthmon.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ESP32 BLE cihazını taramak için servis
 */
@Singleton
class BleScannerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BleScannerService"
    }
    
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner
    
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()
    
    private val _foundDevice = MutableStateFlow<BluetoothDevice?>(null)
    val foundDevice: StateFlow<BluetoothDevice?> = _foundDevice.asStateFlow()
    
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: return
            
            Log.d(TAG, "Cihaz bulundu: $deviceName")
            
            if (deviceName == BleConstants.DEVICE_NAME) {
                Log.d(TAG, "ESP32 bulundu! ${device.address}")
                _foundDevice.value = device
                _scanState.value = ScanState.DeviceFound(device)
                stopScan()
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Tarama başarısız: $errorCode")
            _scanState.value = ScanState.Error("Tarama hatası: $errorCode")
        }
    }
    
    /**
     * Bluetooth açık mı kontrol et
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
    
    /**
     * ESP32 taramasını başlat
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!isBluetoothEnabled()) {
            _scanState.value = ScanState.Error("Bluetooth kapalı")
            return
        }
        
        Log.d(TAG, "ESP32 taraması başlatılıyor...")
        _scanState.value = ScanState.Scanning
        _foundDevice.value = null
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        bleScanner?.startScan(null, settings, scanCallback)
    }
    
    /**
     * Taramayı durdur
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            bleScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Tarama durdurulurken hata: $e")
        }
        if (_scanState.value is ScanState.Scanning) {
            _scanState.value = ScanState.Idle
        }
        Log.d(TAG, "Tarama durduruldu")
    }
    
    /**
     * State'i sıfırla
     */
    fun reset() {
        _scanState.value = ScanState.Idle
        _foundDevice.value = null
    }
}

/**
 * BLE tarama durumları
 */
sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class DeviceFound(val device: BluetoothDevice) : ScanState()
    data class Error(val message: String) : ScanState()
}
