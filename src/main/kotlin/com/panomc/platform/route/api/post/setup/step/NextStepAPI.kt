package com.panomc.platform.route.api.post.setup.step

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Api
import com.panomc.platform.model.RouteType
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class NextStepAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/setup/step/nextStep")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        val response = context.response()
        val data = context.bodyAsJson

        response
            .putHeader("content-type", "application/json; charset=utf-8")

        val clientStep = data.getInteger("step")

        if (clientStep == setupManager.getStep()) {
            var passStep = false

            if (clientStep == 0)
                passStep = true
            else if (clientStep == 1 && !data.getString("websiteName").isNullOrEmpty() && !data.getString("websiteDescription").isNullOrEmpty()) {
                configManager.config["website-name"] = data.getString("websiteName")
                configManager.config["website-description"] = data.getString("websiteDescription")

                passStep = true
            } else if (
                clientStep == 2 &&
                !data.getString("host").isNullOrEmpty() &&
                !data.getString("dbName").isNullOrEmpty() &&
                !data.getString("username").isNullOrEmpty()
            ) {
                @Suppress("UNCHECKED_CAST") val databaseOptions =
                    (configManager.config["database"] as MutableMap<String, Any>)

                databaseOptions.replace("host", data.getString("host"))
                databaseOptions.replace("name", data.getString("dbName"))
                databaseOptions.replace(
                    "username",
                    data.getString("username")
                )
                databaseOptions.replace(
                    "password",
                    if (data.getString("password").isNullOrEmpty()) "" else data.getString("password")
                )
                databaseOptions.replace(
                    "prefix",
                    if (data.getString("prefix").isNullOrEmpty()) "" else data.getString("prefix")
                )

                passStep = true
            }

            if (passStep)
                setupManager.nextStep()
        }

        response.end(setupManager.getCurrentStepData().toJsonString())
    }
}