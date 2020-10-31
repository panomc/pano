package com.panomc.platform.route.api.setup

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Api
import com.panomc.platform.model.RouteType
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class CheckAPI : Api() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/setup/step/check")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        val response = context.response()

        response
            .putHeader("content-type", "application/json; charset=utf-8")

        response.end(setupManager.getCurrentStepData().toJsonString())
    }
}