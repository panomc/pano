package com.panomc.platform.model

import com.panomc.platform.ErrorCode
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

abstract class SetupApi : Api() {
    override fun getHandler() = Handler<RoutingContext> { context ->
        if (setupManager.isSetupDone()) {
            sendResult(Error(ErrorCode.PLATFORM_ALREADY_INSTALLED), context)

            return@Handler
        }

        handler(context)
    }
}