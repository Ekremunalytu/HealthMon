package com.example.healthmon.data.service

import android.util.Log
import com.example.healthmon.ble.BleConnectionManager
import com.example.healthmon.ble.BleScannerService
import com.example.healthmon.ble.ConnectionState
import com.example.healthmon.ble.ScanState
import com.example.healthmon.ble.SensorData
import com.example.healthmon.data.model.HeartRateData
import com.example.healthmon.data.model.AccelerometerData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ESP32 BLE üzerinden gerçek sensör verisi alan servis
 * MockSensorService yerine kullanılır
 */
@Singleton
class RealSensorService @Inject constructor(
    private val bleScanner: BleScannerService,
    private val bleConnection: BleConnectionManager
) {
    companion object {
        private const val TAG = "RealSensorService"
    }
    
    // Bağlantı durumları
    val connectionState: StateFlow<ConnectionState> = bleConnection.connectionState
    val scanState: StateFlow<ScanState> = bleScanner.scanState
    
    /**
     * Bluetooth açık mı?
     */
    fun isBluetoothEnabled(): Boolean = bleScanner.isBluetoothEnabled()
    
    /**
     * ESP32 taramasını başlat
     */
    fun startScan() {
        Log.d(TAG, "ESP32 taraması başlatılıyor")
        bleScanner.startScan()
    }
    
    /**
     * Taramayı durdur
     */
    fun stopScan() = bleScanner.stopScan()
    
    /**
     * Bulunan ESP32'ye bağlan
     */
    fun connectToDevice() {
        bleScanner.foundDevice.value?.let { device ->
            Log.d(TAG, "ESP32'ye bağlanılıyor: ${device.address}")
            bleConnection.connect(device)
        } ?: Log.e(TAG, "Bağlanılacak cihaz bulunamadı!")
    }
    
    /**
     * Bağlantıyı kes
     */
    fun disconnect() {
        bleConnection.disconnect()
        bleScanner.reset()
    }
    
    /**
     * ESP32'den gelen ham sensör verileri
     */
    fun getSensorDataFlow(): Flow<SensorData> = bleConnection.sensorData
    
    /**
     * PPG'den hesaplanan kalp atışı
     * Not: Gerçek BPM hesaplaması backend'de yapılıyor
     */
    fun getHeartRateFlow(): Flow<HeartRateData> = bleConnection.sensorData.map { data ->
        HeartRateData(bpm = estimateBpmFromPpg(data.ppg))
    }
    
    /**
     * Akselerometre verisi
     */
    fun getAccelerometerFlow(): Flow<AccelerometerData> = bleConnection.sensorData.map { data ->
        AccelerometerData(
            x = data.accelerometerX,
            y = data.accelerometerY,
            z = data.accelerometerZ
        )
    }
    
    /**
     * PPG değerinden basit BPM tahmini
     * Gerçek hesaplama backend'de yapılıyor, bu sadece anlık gösterim için
     */
    private fun estimateBpmFromPpg(ppg: Int): Int {
        // PPG değerini normalize et (2000 civarı baz değer)
        // Bu basit bir placeholder, gerçek algoritma peak detection gerektirir
        return 60 + ((ppg - 1800) / 10).coerceIn(0, 60)
    }
    
    /**
     * Bağlı mı?
     */
    fun isConnected(): Boolean = bleConnection.isConnected()
}
