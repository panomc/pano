package com.panomc.platform.di.module

import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.util.AuthProvider
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import javax.inject.Singleton

@Module
class AuthProviderModule {

    @Provides
    @Singleton
    fun provideAuthProvider(vertx: Vertx, configManager: ConfigManager, databaseManager: DatabaseManager) =
        AuthProvider(vertx, databaseManager, configManager)
}