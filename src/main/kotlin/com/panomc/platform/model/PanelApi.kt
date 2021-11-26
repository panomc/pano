package com.panomc.platform.model

import com.panomc.platform.ErrorCode
import com.panomc.platform.util.LoginUtil
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

abstract class PanelApi : Api() {

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (!setupManager.isSetupDone()) {
            sendResult(Error(ErrorCode.INSTALLATION_REQUIRED), context)

            return@Handler
        }

        LoginUtil.isLoggedIn(databaseManager, context) { isLoggedIn, _ ->
            if (!isLoggedIn) {
                context.reroute("/")

                return@isLoggedIn
            }

            LoginUtil.hasAccessPanel(databaseManager, context) { isAdmin, _ ->
                if (isAdmin)
                    getHandler(context)
                else
                    context.reroute("/")
            }
        }
    }
}