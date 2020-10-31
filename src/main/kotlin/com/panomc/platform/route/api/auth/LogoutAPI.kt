package com.panomc.platform.route.api.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Token
import com.panomc.platform.model.*
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

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.delete(
                Token(-1, token, -1, ""),
                databaseManager.getSQLConnection(connection)
            ) { result, _ ->
                if (result == null)
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.LOGOUT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_28))
                    }
                else
                    databaseManager.closeConnection(connection) {
                        deleteCookies(context)

                        handler.invoke(Successful())
                    }
            }
        }
    }

    private fun deleteCookies(context: RoutingContext) {
        context.removeCookie("pano_token")
    }
}