package com.panomc.platform.di.module

import com.panomc.platform.util.ConfigManager
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import javax.inject.Singleton

@Module
class ConfigManagerModule(logger: Logger, vertx: Vertx) {

    @Singleton
    private val mConfigManager = ConfigManager(logger, vertx)

    @Provides
    @Singleton
    fun provideConfigManager() = mConfigManager
}