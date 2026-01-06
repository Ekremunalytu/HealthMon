package com.example.healthmon.di

import com.example.healthmon.data.network.AuthRepository
import com.example.healthmon.data.repository.CaregiverRepository
import com.example.healthmon.data.repository.PatientRepository
import com.example.healthmon.data.repository.VitalDataRepository
import com.example.healthmon.data.service.ApiService
import com.example.healthmon.data.service.IngestionApiService
import com.example.healthmon.data.service.MockSensorService
import com.example.healthmon.data.service.WebSocketService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // =========================================================================
    // Backend Docker Container URLs
    // =========================================================================
    // 10.0.2.2 maps to host machine's localhost from Android Emulator
    // Docker ports: core=8000, ingestion=8001
    // For physical device on same WiFi, replace 10.0.2.2 with your Mac's IP
    
    // Core service (port 8000): Auth, patient info, settings, WebSocket
    private const val CORE_BASE_URL = "http://10.0.2.2:8000/"
    
    // Ingestion service (port 8001): Sensor data submission (/api/v1/ingest)
    private const val INGESTION_BASE_URL = "http://10.0.2.2:8001/"
    
    // WebSocket URL for real-time communication
    const val WS_BASE_URL = "ws://10.0.2.2:8000/ws"

    // =========================================================================
    // Common Dependencies
    // =========================================================================
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // =========================================================================
    // Core Service (port 8000) - Auth, Patient, Caregiver APIs
    // =========================================================================
    
    @Provides
    @Singleton
    @Named("CoreRetrofit")
    fun provideCoreRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(CORE_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideApiService(@Named("CoreRetrofit") retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    // =========================================================================
    // Ingestion Service (port 8001) - Sensor Data Submission
    // =========================================================================
    
    @Provides
    @Singleton
    @Named("IngestionRetrofit")
    fun provideIngestionRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(INGESTION_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideIngestionApiService(@Named("IngestionRetrofit") retrofit: Retrofit): IngestionApiService =
        retrofit.create(IngestionApiService::class.java)

    // =========================================================================
    // Repositories
    // =========================================================================

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService): AuthRepository =
        AuthRepository(apiService)

    @Provides
    @Singleton
    fun provideMockSensorService(): MockSensorService = MockSensorService()

    @Provides
    @Singleton
    fun provideVitalDataRepository(
        mockSensorService: MockSensorService,
        realSensorService: com.example.healthmon.data.service.RealSensorService,
        webSocketService: WebSocketService,
        ingestionApiService: IngestionApiService
    ): VitalDataRepository = VitalDataRepository(mockSensorService, realSensorService, webSocketService, ingestionApiService)

    @Provides
    @Singleton
    fun provideWebSocketService(okHttpClient: OkHttpClient, moshi: Moshi): WebSocketService =
        WebSocketService(okHttpClient, moshi)

    @Provides
    @Singleton
    fun providePatientRepository(
        apiService: ApiService,
        ingestionApiService: IngestionApiService,
        webSocketService: WebSocketService
    ): PatientRepository = PatientRepository(apiService, ingestionApiService, webSocketService)

    @Provides
    @Singleton
    fun provideCaregiverRepository(
        apiService: ApiService,
        webSocketService: WebSocketService
    ): CaregiverRepository = CaregiverRepository(apiService, webSocketService)
}
