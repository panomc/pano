package com.panomc.platform.route.api.server

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Server
import com.panomc.platform.model.*
import com.panomc.platform.util.PlatformCodeManager
import com.panomc.platform.util.SetupManager
import io.vertx.ext.web.RoutingContext
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
    lateinit var platformCodeManager: PlatformCodeManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        if (!setupManager.isSetupDone()) {
            context.reroute("/")

            return
        }

        connectNew(context) { result ->
            handler.invoke(result)
        }
    }

    private fun connectNew(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        databaseManager.createConnection { connection, _ ->
            when {
                connection == null -> handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
                data.getString("platformCode", "") == platformCodeManager.getPlatformKey()
                    .toString() -> databaseManager.getDatabase().serverDao.add(
                    Server(
                        -1,
                        data.getString("serverName"),
                        data.getInteger("playerCount"),
                        data.getInteger("maxPlayerCount"),
                        data.getString("serverType"),
                        data.getString("serverVersion"),
                        data.getString("favicon"),
                        data.getString("status")
                    ),
                    databaseManager.getSQLConnection(connection),
                ) { token, _ ->
                    if (token == null) {
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.CONNECT_NEW_SERVER_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_29))
                        }

                        return@add
                    }

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
}