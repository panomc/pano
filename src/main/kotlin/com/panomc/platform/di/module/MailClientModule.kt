package com.panomc.platform.di.module

import com.panomc.platform.config.ConfigManager
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.StartTLSOptions
import javax.inject.Singleton

@Module
class MailClientModule(configManager: ConfigManager) {
    @Singleton
    private val mMailClientConfig = MailConfig()

    init {
        val emailConfig = (configManager.getConfig()["email"] as Map<*, *>)

        mMailClientConfig.hostname = emailConfig["host"] as String
        mMailClientConfig.port = emailConfig["port"] as Int

        if (emailConfig["SSL"] as Boolean) {
            mMailClientConfig.starttls = StartTLSOptions.REQUIRED
            mMailClientConfig.isSsl = true
        }

        mMailClientConfig.username = emailConfig["username"] as String
        mMailClientConfig.password = emailConfig["password"] as String

        mMailClientConfig.authMethods = "PLAIN";
    }

    @Provides
    @Singleton
    fun provideMailClient(vertx: Vertx) = MailClient.createShared(vertx, mMailClientConfig, "mailClient")
}