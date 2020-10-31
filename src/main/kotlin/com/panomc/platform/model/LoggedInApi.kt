package com.panomc.platform.model

import com.panomc.platform.Main
import com.panomc.platform.util.Auth
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

abstract class LoggedInApi : Api() {
    init {
        @Suppress("LeakingThis")
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (!setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        val auth = Auth()

        auth.isLoggedIn(context) { isLoggedIn ->
            if (!isLoggedIn) {
                context.reroute("/")

                return@isLoggedIn
            }

            getHandler(context)
        }
    }
}