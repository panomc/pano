package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PanelNotificationsPageAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/notifications/loadMore")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        val idOrToken = LoginUtil.getUserIDOrToken(context)

        if (idOrToken == null) {
            handler.invoke(Error(ErrorCode.NOT_LOGGED_IN))

            return
        }

        databaseManager.createConnection((this::createConnectionHandler)(handler, idOrToken, id))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        idOrToken: Any?,
        lastNotificationID: Int
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        if (idOrToken is Int) {
            databaseManager.getDatabase().panelNotificationDao.get10ByUserIDAndStartFromID(
                idOrToken,
                lastNotificationID,
                sqlConnection,
                (this::get10ByUserIDAndStartFromIDHandler)(handler, sqlConnection, idOrToken, lastNotificationID)
            )

            return@handler
        }

        if (idOrToken !is String) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.NOT_LOGGED_IN))
            }

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            idOrToken,
            sqlConnection,
            (this::getUserIDFromTokenHandler)(handler, sqlConnection, lastNotificationID)
        )
    }

    private fun getUserIDFromTokenHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        lastNotificationID: Int
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_166))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.get10ByUserIDAndStartFromID(
            userID,
            lastNotificationID,
            sqlConnection,
            (this::get10ByUserIDAndStartFromIDHandler)(handler, sqlConnection, userID, lastNotificationID)
        )
    }

    private fun get10ByUserIDAndStartFromIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        userID: Int,
        lastNotificationID: Int
    ) = handler@{ notifications: List<PanelNotification>?, _: AsyncResult<*> ->
        if (notifications == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_167))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.markReadLast10StartFromID(
            userID,
            lastNotificationID,
            sqlConnection,
            (this::markReadLast10StartFromIDHandler)(handler, sqlConnection, userID, notifications)
        )
    }

    private fun markReadLast10StartFromIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        userID: Int,
        notifications: List<PanelNotification>
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (result == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_168))

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
                    )
                )
            )
        }
    }
}