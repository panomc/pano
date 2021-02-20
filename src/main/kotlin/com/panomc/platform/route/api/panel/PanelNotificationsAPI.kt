package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PanelNotificationsAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/notifications")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection((this::createConnectionHandler)(handler, token))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        token: String
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            token,
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
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_66))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.getCountByUserID(
            userID,
            sqlConnection,
            (this::getCountByUserIDHandler)(handler, sqlConnection, userID)
        )
    }

    private fun getCountByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        userID: Int
    ) = handler@{ count: Int?, _: AsyncResult<*> ->
        if (count == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_65))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.getAllByUserID(
            userID,
            sqlConnection,
            (this::getAllByUserIDHandler)(handler, sqlConnection, userID, count)
        )
    }

    private fun getAllByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        userID: Int,
        count: Int
    ) = handler@{ notifications: List<PanelNotification>?, _: AsyncResult<*> ->
        if (notifications == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_110))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.markReadAll(
            userID,
            sqlConnection,
            (this::markReadAllHandler)(handler, sqlConnection, count, userID, notifications)
        )
    }

    private fun markReadAllHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        count: Int,
        userID: Int,
        notifications: List<PanelNotification>
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (result == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_111))

                return@closeConnection
            }

            val notificationsDataList = mutableListOf<Map<String, Any?>>()

            notifications.forEach { notification ->
                notificationsDataList.add(
                    mapOf(
                        "id" to notification.id,
                        "type_ID" to notification.typeID,
                        "date" to notification.date,
                        "status" to notification.status,
                        "isPersonal" to (notification.userID == userID)
                    )
                )
            }

            handler.invoke(
                Successful(
                    mutableMapOf(
                        "notifications" to notificationsDataList,
                        "notifications_count" to count
                    )
                )
            )
        }
    }
}