package com.panomc.platform.model

import com.panomc.platform.Main
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

abstract class SetupApi : Api() {
    init {
        @Suppress("LeakingThis")
        Main.getComponent().inject(this)
    }

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        getHandler(context)
    }
}