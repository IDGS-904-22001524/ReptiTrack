// com.waldoz_x.reptitrack.di/AppModule.kt
package com.waldoz_x.reptitrack.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.waldoz_x.reptitrack.data.source.remote.HiveMqttClient
import com.waldoz_x.reptitrack.data.source.remote.TerrariumFirebaseDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Módulo para proveer instancias de Firebase Firestore.
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}

// Módulo para proveer la instancia de HiveMqttClient.
@Module
@InstallIn(SingletonComponent::class)
object MqttModule {
    @Provides
    @Singleton
    fun provideHiveMqttClient(): HiveMqttClient {
        return HiveMqttClient()
    }
}

// Módulo para proveer las fuentes de datos remotas.
@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideTerrariumFirebaseDataSource(firestore: FirebaseFirestore): TerrariumFirebaseDataSource {
        return TerrariumFirebaseDataSource(firestore)
    }
}

// Módulo para proveer la instancia de FirebaseAuth.
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
