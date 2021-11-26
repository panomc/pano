package com.panomc.platform.model

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

abstract class SetupApi : Api() {
    override fun getHandler() = Handler<RoutingContext> { context ->
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        handler(context)
    }
}