package com.panomc.platform.route.api.post.server

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import java.util.*
import javax.inject.Inject

class ConnectNewAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/server/connectNew")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var platformCodeManager: PlatformCodeManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (!setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        val response = context.response()

        response
            .putHeader("content-type", "application/json; charset=utf-8")

        connectNew(context) { result ->
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

    private fun connectNew(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        databaseManager.createConnection { connection, _ ->
            when {
                connection == null -> handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
                data.getString("platformCode", "") == platformCodeManager.getPlatformKey().toString() -> addServer(connection, data, handler) { token ->
                    databaseManager.closeConnection(connection) {
                        handler.invoke(
                            Successful(
                                mapOf(
                                    "token" to token
                                )
                            )
                        )
                    }
                }
                else -> databaseManager.closeConnection(connection) {
                    handler.invoke(Error(ErrorCode.CONNECT_NEW_SERVER_API_PLATFORM_CODE_WRONG))
                }
            }
        }
    }

    private fun addServer(
        connection: Connection,
        data: io.vertx.core.json.JsonObject,
        resultHandler: (authResult: Result) -> Unit,
        handler: (token: String) -> Unit
    ) {
        val key = Keys.keyPairFor(SignatureAlgorithm.RS256)

        val token = Jwts.builder()
            .setSubject("SERVER_CONNECT")
            .signWith(
                key.private
            )
            .compact()

        val query =
            "INSERT INTO ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}server (name, player_count, max_player_count, server_type, server_version, favicon, secret_key, public_key, token, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        databaseManager.getSQLConnection(connection).updateWithParams(
            query,
            JsonArray()
                .add(data.getString("serverName"))
                .add(data.getInteger("playerCount"))
                .add(data.getInteger("maxPlayerCount"))
                .add(data.getString("serverType"))
                .add(data.getString("serverVersion"))
                .add(data.getString("favicon"))
                .add(Base64.getEncoder().encodeToString(key.private.encoded))
                .add(Base64.getEncoder().encodeToString(key.public.encoded))
                .add(token)
                .add(data.getString("status"))
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(token)
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.CONNECT_NEW_SERVER_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_29))
                }
        }
    }
}