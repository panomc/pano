package com.panomc.platform.route.api.setup

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Api
import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import com.panomc.platform.model.Successful
import com.panomc.platform.util.SetupManager
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

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return
        }

        handler.invoke(Successful(setupManager.getCurrentStepData()))
    }
}