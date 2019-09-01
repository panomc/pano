package com.panomc.platform.di.module

import dagger.Module
import dagger.Provides
import io.vertx.core.logging.Logger
import javax.inject.Singleton

@Module
class LoggerModule(private val mLogger: Logger) {

    @Provides
    @Singleton
    fun provideLogger() = mLogger
}