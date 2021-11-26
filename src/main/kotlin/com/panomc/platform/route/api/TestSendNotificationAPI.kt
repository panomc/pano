package com.panomc.platform.route.api

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import com.panomc.platform.util.NotificationStatus
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class TestSendNotificationAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/testNotification")

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
            databaseManager.getDatabase().panelNotificationDao.add(
                getNotification(idOrToken),
                sqlConnection,
                (this::addHandler)(handler, sqlConnection)
            )

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            idOrToken as String,
            sqlConnection,
            (this::getUserIDFromTokenHandler)(handler, sqlConnection)
        )
    }

    private fun getNotification(userID: Int) = PanelNotification(
        -1,
        userID,
        "TEST NOTIFICATION",
        System.currentTimeMillis(),
        NotificationStatus.NOT_READ
    )

    private fun getUserIDFromTokenHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.add(
            getNotification(userID),
            sqlConnection,
            (this::addHandler)(handler, sqlConnection)
        )
    }

    private fun addHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (result == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN))

                return@closeConnection
            }

            handler.invoke(Successful())
        }
    }
}