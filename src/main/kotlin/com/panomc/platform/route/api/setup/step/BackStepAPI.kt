package com.panomc.platform.route.api.setup.step

import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import com.panomc.platform.model.SetupApi
import com.panomc.platform.model.Successful
import io.vertx.ext.web.RoutingContext

class BackStepAPI : SetupApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/setup/step/backStep")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        setupManager.backStep()

        handler.invoke(Successful(setupManager.getCurrentStepData()))
    }
}