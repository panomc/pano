package com.panomc.platform.route.api.get.panel

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.PORT
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import java.net.InetAddress
import javax.inject.Inject

class BasicDataAPI : Api() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/basicData")

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

                getBasicData(context) { result ->
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

    private fun getUserIDFromToken(
        connection: Connection,
        token: String,
        handler: (userID: Int) -> Unit
    ) {
        val query =
            "SELECT user_id FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}token where token = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    handler.invoke(0)
                }
        }
    }

    private fun getBasicData(context: RoutingContext, handler: (result: Result) -> Unit) {
        val localHost = InetAddress.getLocalHost()

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else {
                val token = context.getCookie("pano_token").value

                val platformCodeGenerator = PlatformCodeGenerator()

                platformCodeGenerator.createPlatformCode(connection) { platformCodeGeneratorResult ->
                    if (platformCodeGeneratorResult is Successful)
                        getUserIDFromToken(connection, token) { userID ->
                            if (userID != 0)
                                getBasicUserData(connection, userID) { getBasicUserData ->
                                    if (getBasicUserData is Successful)
                                        databaseManager.closeConnection(connection) {
                                            handler.invoke(
                                                Successful(
                                                    mapOf(
                                                        "user" to getBasicUserData.map,
                                                        "website" to mapOf(
                                                            "name" to configManager.config["website-name"],
                                                            "description" to configManager.config["website-description"]
                                                        ),
                                                        "platform_server_match_key" to platformCodeGeneratorResult.map["platformCode"],
                                                        "platform_host_address" to localHost.hostAddress + ":" + PORT,
                                                        "servers" to listOf<Map<String, Any?>>()
                                                    )
                                                )
                                            )
                                        }
                                    else
                                        handler.invoke(getBasicUserData)
                                }
                            else
                                handler.invoke(Error(ErrorCode.BASIC_DATA_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_8))
                        }
                    else
                        handler.invoke(platformCodeGeneratorResult)
                }
            }
        }
    }

    private fun getBasicUserData(connection: Connection, userID: Int, handler: (result: Result) -> Unit) {
        val query =
            "SELECT username, email, permission_id FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user where id = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(
            query,
            JsonArray().add(userID)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(
                    Successful(
                        mapOf(
                            "username" to queryResult.result().results[0].getString(0),
                            "email" to queryResult.result().results[0].getString(1),
                            "permission_id" to queryResult.result().results[0].getInteger(2)
                        )
                    )
                )
            else
                databaseManager.closeConnection(connection) {
                    handler.invoke(Error(ErrorCode.BASIC_DATA_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_7))
                }
        }
    }
}