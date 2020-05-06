package com.panomc.platform.di.module

import com.panomc.platform.util.PlatformCodeManager
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import javax.inject.Singleton

@Module
class PlatformCodeManagerModule {

    @Provides
    @Singleton
    fun providePlatformCodeManger(vertx: Vertx) = PlatformCodeManager(vertx)
}