package com.panomc.platform.model

import com.panomc.platform.Main
import com.panomc.platform.util.Auth
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

abstract class PanelApi : Api() {
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

        val response = context.response()

        val auth = Auth()

        auth.isAdmin(context) { isAdmin ->
            if (isAdmin) {
                response
                    .putHeader("content-type", "application/json; charset=utf-8")

                getHandler(context) { result ->
                    getResultHandler(result, context)
                }
            } else
                context.reroute("/")
        }
    }

    abstract fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit)
}