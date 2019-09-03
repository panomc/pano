package com.panomc.platform.di.component

import com.panomc.platform.Main
import com.panomc.platform.di.module.*
import com.panomc.platform.route.template.IndexTemplate
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (RecaptchaModule::class),
        (VertxModule::class),
        (LoggerModule::class),
        (RouterModule::class),
        (TemplateEngineModule::class),
        (ConfigManagerModule::class)
    ]
)
interface ApplicationComponent {
    fun inject(main: Main)

    fun inject(indexTemplate: IndexTemplate)
}