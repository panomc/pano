package com.panomc.platform.di.module

import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.SetupManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SetupManagerModule {

    @Provides
    @Singleton
    fun provideSetupManager(configManager: ConfigManager) = SetupManager(configManager)
}