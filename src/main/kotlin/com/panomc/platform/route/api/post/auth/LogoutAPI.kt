package com.panomc.platform.route.api.post.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class LogoutAPI : LoggedInApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/auth/logout")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
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
            "DELETE from ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}token WHERE token = ?"

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