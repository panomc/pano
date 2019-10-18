package com.panomc.platform.di.module

import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.DatabaseManager
import com.panomc.platform.util.SetupManager
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import javax.inject.Singleton

@Module
class DatabaseManagerModule {

    @Provides
    @Singleton
    fun provideDatabaseManager(vertx: Vertx, logger: Logger, configManager: ConfigManager, setupManager: SetupManager) =
        DatabaseManager(vertx, logger, configManager, setupManager)
}