package com.panomc.platform.model

import com.panomc.platform.util.LoginUtil
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

abstract class LoggedInApi : Api() {
    override fun getHandler() = Handler<RoutingContext> { context ->
        if (!setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        LoginUtil.isLoggedIn(databaseManager, context) { isLoggedIn, _ ->
            if (!isLoggedIn) {
                context.reroute("/")

                return@isLoggedIn
            }

            getHandler(context)
        }
    }
}