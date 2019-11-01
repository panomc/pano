package com.panomc.platform.route.api.post.auth

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class LogoutAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/auth/logout")

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

        auth.isLoggedIn(context) { isLoggedIn ->
            if (!isLoggedIn) {
                context.reroute("/")

                return@isLoggedIn
            }

            response
                .putHeader("content-type", "application/json; charset=utf-8")

            logout(context) { result ->
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
        }
    }

    private fun logout(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else {
                val token = context.getCookie("pano_token").value

                deleteToken(connection, token, handler) {
                    databaseManager.closeConnection(connection) {
                        deleteCookies(context)

                        handler.invoke(Successful())
                    }
                }
            }
        }
    }

    private fun deleteToken(
        connection: Connection,
        token: String,
        resultHandler: (authResult: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "DELETE from ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}token WHERE token = ?"

        databaseManager.getSQLConnection(connection)
            .updateWithParams(query, JsonArray().add(token)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke()
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.LOGOUT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_28))
                    }
            }
    }

    private fun deleteCookies(context: RoutingContext) {
        context.removeCookie("pano_token")
    }
}