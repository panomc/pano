package com.panomc.platform.route.api.setup

import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import com.panomc.platform.model.SetupApi
import com.panomc.platform.model.Successful
import io.vertx.ext.web.RoutingContext

class CheckAPI : SetupApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/setup/step/check")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        handler.invoke(Successful(setupManager.getCurrentStepData()))
    }
}