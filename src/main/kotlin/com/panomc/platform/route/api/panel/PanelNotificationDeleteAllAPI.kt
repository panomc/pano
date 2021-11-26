package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PanelNotificationDeleteAllAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/notifications/deleteAll")

    override fun handler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val idOrToken = LoginUtil.getUserIDOrToken(context)

        if (idOrToken == null || (idOrToken !is Int && idOrToken !is String)) {
            handler.invoke(Error(ErrorCode.NOT_LOGGED_IN))

            return
        }

        databaseManager.createConnection((this::createConnectionHandler)(handler, idOrToken))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        idOrToken: Any
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        if (idOrToken is Int) {
            databaseManager.getDatabase().panelNotificationDao.deleteAllByUserID(
                idOrToken,
                sqlConnection,
                (this::deleteAllByUserIDHandler)(handler, sqlConnection)
            )

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            idOrToken as String,
            sqlConnection,
            (this::getUserIDFromTokenHandler)(handler, sqlConnection)
        )
    }

    private fun getUserIDFromTokenHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_173))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.deleteAllByUserID(
            userID,
            sqlConnection,
            (this::deleteAllByUserIDHandler)(handler, sqlConnection)
        )
    }

    private fun deleteAllByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (result == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_174))

                return@closeConnection
            }

            handler.invoke(Successful())
        }
    }
}