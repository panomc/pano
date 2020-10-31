package com.panomc.platform.route.api.setup.step

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Api
import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import com.panomc.platform.model.Successful
import com.panomc.platform.util.SetupManager
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

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return
        }

        setupManager.backStep()

        handler.invoke(Successful(setupManager.getCurrentStepData()))
    }
}