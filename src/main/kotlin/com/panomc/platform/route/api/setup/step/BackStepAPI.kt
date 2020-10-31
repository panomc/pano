package com.panomc.platform.route.api.setup.step

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Api
import com.panomc.platform.model.RouteType
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class BackStepAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/setup/step/backStep")

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

        setupManager.backStep()

        response.end(setupManager.getCurrentStepData().toJsonString())
    }
}