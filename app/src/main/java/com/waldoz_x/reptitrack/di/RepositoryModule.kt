package com.waldoz_x.reptitrack.di

import com.waldoz_x.reptitrack.domain.repository.TerrariumRepository
import com.waldoz_x.reptitrack.data.repository.TerrariumRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTerrariumRepository(
        impl: TerrariumRepositoryImpl
    ): TerrariumRepository
}
