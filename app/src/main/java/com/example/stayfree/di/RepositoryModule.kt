package com.example.stayfree.di

import com.example.stayfree.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindUsageRepository(impl: UsageRepositoryImpl): UsageRepository

    @Binds @Singleton
    abstract fun bindBlockingRepository(impl: BlockingRepositoryImpl): BlockingRepository

    @Binds @Singleton
    abstract fun bindWebsiteBlockRepository(impl: WebsiteBlockRepositoryImpl): WebsiteBlockRepository

    @Binds @Singleton
    abstract fun bindInAppBlockRepository(impl: InAppBlockRepositoryImpl): InAppBlockRepository
}
