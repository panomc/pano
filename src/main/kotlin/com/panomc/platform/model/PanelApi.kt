package com.panomc.platform.model

import com.panomc.platform.ErrorCode
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

abstract class PanelApi : Api() {

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (!setupManager.isSetupDone()) {
            sendResult(Error(ErrorCode.INSTALLATION_REQUIRED), context)

            return@Handler
        }

        authProvider.isLoggedIn(context) { isLoggedIn ->
            if (!isLoggedIn) {
                sendResult(Error(ErrorCode.NOT_LOGGED_IN), context)

                return@isLoggedIn
            }

            authProvider.hasAccessPanel(context) { isAdmin, _ ->
                if (isAdmin)
                    handler(context)
                else
                    sendResult(Error(ErrorCode.NO_PERMISSION), context)
            }
        }
    }
}