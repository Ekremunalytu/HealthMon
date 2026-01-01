package com.example.healthmon.di

import android.content.Context
import com.example.healthmon.ble.BleConnectionManager
import com.example.healthmon.ble.BleScannerService
import com.example.healthmon.data.service.RealSensorService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * BLE servisleri için Hilt DI modülü
 */
@Module
@InstallIn(SingletonComponent::class)
object BleModule {
    
    @Provides
    @Singleton
    fun provideBleScannerService(
        @ApplicationContext context: Context
    ): BleScannerService {
        return BleScannerService(context)
    }
    
    @Provides
    @Singleton
    fun provideBleConnectionManager(
        @ApplicationContext context: Context
    ): BleConnectionManager {
        return BleConnectionManager(context)
    }
    
    @Provides
    @Singleton
    fun provideRealSensorService(
        scanner: BleScannerService,
        connection: BleConnectionManager
    ): RealSensorService {
        return RealSensorService(scanner, connection)
    }
}
