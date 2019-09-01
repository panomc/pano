package com.panomc.platform.di.module

import dagger.Module
import dagger.Provides
import de.triology.recaptchav2java.ReCaptcha
import javax.inject.Singleton

@Module
class RecaptchaModule {
    private companion object {
        private const val SECRET_KEY = ""
    }

    @Provides
    @Singleton
    fun provideRecaptcha(): ReCaptcha {
        return ReCaptcha(SECRET_KEY)
    }
}