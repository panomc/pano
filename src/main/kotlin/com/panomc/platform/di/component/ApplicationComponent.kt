package com.panomc.platform.di.component

import com.panomc.platform.Main
import com.panomc.platform.di.module.LoggerModule
import com.panomc.platform.di.module.RecaptchaModule
import com.panomc.platform.di.module.RouterModule
import com.panomc.platform.di.module.VertxModule
import com.panomc.platform.model.Route
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (RecaptchaModule::class),
        (VertxModule::class),
        (LoggerModule::class),
        (RouterModule::class)
    ]
)
interface ApplicationComponent {
    fun inject(route: Route)
    fun inject(main: Main)
}