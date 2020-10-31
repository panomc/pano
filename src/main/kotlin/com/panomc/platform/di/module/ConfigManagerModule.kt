package com.panomc.platform.di.module

import com.panomc.platform.config.ConfigManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ConfigManagerModule(private val mConfigManager: ConfigManager) {

    @Provides
    @Singleton
    fun provideConfigManager() = mConfigManager
}