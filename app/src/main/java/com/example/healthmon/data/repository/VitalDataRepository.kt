package com.example.healthmon.data.repository

import com.example.healthmon.data.model.HeartRateData
import com.example.healthmon.data.model.InactivityData
import com.example.healthmon.data.model.VitalDataState
import com.example.healthmon.data.service.MockSensorService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for vital data from sensors
 */
@Singleton
class VitalDataRepository @Inject constructor(
    private val mockSensorService: MockSensorService
) {
    
    /**
     * Get heart rate data flow
     */
    fun getHeartRateFlow(): Flow<HeartRateData> = mockSensorService.getHeartRateFlow()
    
    /**
     * Get inactivity data flow
     */
    fun getInactivityFlow(): Flow<InactivityData> = mockSensorService.getInactivityFlow().map { minutes ->
        InactivityData(durationMinutes = minutes)
    }
    
    /**
     * Get combined vital data state
     */
    fun getVitalDataStateFlow(): Flow<VitalDataState> = combine(
        getHeartRateFlow(),
        getInactivityFlow()
    ) { heartRate, inactivity ->
        VitalDataState(
            heartRate = heartRate,
            inactivity = inactivity,
            isConnected = true
        )
    }
    
    /**
     * Reset inactivity tracking
     */
    fun resetInactivity() {
        mockSensorService.resetInactivity()
    }
}
