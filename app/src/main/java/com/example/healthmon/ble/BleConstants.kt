package com.example.healthmon.ble

import java.util.UUID

/**
 * ESP32 BLE bağlantısı için sabitler
 */
object BleConstants {
    // ESP32 cihaz adı
    const val DEVICE_NAME = "CDTP-Watch"
    
    // Heart Rate Service (standart BLE)
    val SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
    
    // Sensor Data Characteristic
    val SENSOR_DATA_UUID: UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")
    
    // Client Characteristic Configuration Descriptor
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    
    // Tarama timeout (ms)
    const val SCAN_TIMEOUT_MS = 10_000L
}
