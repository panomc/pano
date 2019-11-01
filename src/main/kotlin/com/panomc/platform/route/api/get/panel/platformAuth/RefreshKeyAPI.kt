package com.panomc.platform.route.api.get.panel.platformAuth

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
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
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

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
        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else {
                val platformCodeGenerator = PlatformCodeGenerator()

                platformCodeGenerator.createPlatformCode(connection) { platformCodeGeneratorResult ->
                    if (platformCodeGeneratorResult is Successful)
                        databaseManager.closeConnection(connection) {
                            handler.invoke(
                                Successful(
                                    mapOf(
                                        "key" to platformCodeGeneratorResult.map["platformCode"]
                                    )
                                )
                            )
                        }
                    else
                        handler.invoke(platformCodeGeneratorResult)
                }
            }
        }
    }
}