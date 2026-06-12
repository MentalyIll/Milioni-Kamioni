package com.example.stayfree.di

import android.content.Context
import androidx.room.Room
import com.example.stayfree.data.local.db.AppDatabase
import com.example.stayfree.data.local.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "stayfree.db")
            .build()

    @Provides fun provideAppUsageDao(db: AppDatabase): AppUsageDao = db.appUsageDao()
    @Provides fun provideBlockRuleDao(db: AppDatabase): BlockRuleDao = db.blockRuleDao()
    @Provides fun provideScheduleDao(db: AppDatabase): ScheduleDao = db.scheduleDao()
    @Provides fun provideWebsiteBlockDao(db: AppDatabase): WebsiteBlockDao = db.websiteBlockDao()
    @Provides fun provideInAppBlockDao(db: AppDatabase): InAppBlockDao = db.inAppBlockDao()
    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
}
