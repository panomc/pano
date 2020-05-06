package com.panomc.platform.route.api.get.panel.platformAuth

import com.beust.klaxon.JsonObject
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.Auth
import com.panomc.platform.util.PlatformCodeManager
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class RefreshKeyAPI : Api() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/platformAuth/refreshKey")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var platformCodeManager: PlatformCodeManager

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

                getRefreshKeyData { result ->
                    if (result is Successful) {
                        val responseMap = mutableMapOf<String, Any?>(
                            "result" to "ok"
                        )

                        responseMap.putAll(result.map)

                        response.end(
                            JsonObject(
                                responseMap
                            ).toJsonString()
                        )
                    } else if (result is Error)
                        response.end(
                            JsonObject(
                                mapOf(
                                    "result" to "error",
                                    "error" to result.errorCode
                                )
                            ).toJsonString()
                        )
                }
            } else
                context.reroute("/")
        }
    }

    private fun getRefreshKeyData(handler: (result: Result) -> Unit) {
        handler.invoke(
            Successful(
                mapOf(
                    "key" to platformCodeManager.getPlatformKey(),
                    "timeStarted" to platformCodeManager.getTimeStarted()
                )
            )
        )
    }
}