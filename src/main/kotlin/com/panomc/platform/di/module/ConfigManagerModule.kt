package com.panomc.platform.di.module

import com.panomc.platform.config.ConfigManager
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import javax.inject.Singleton

@Module
class ConfigManagerModule {

    @Provides
    @Singleton
    fun provideConfigManager(logger: Logger, vertx: Vertx) = ConfigManager(logger, vertx)
}