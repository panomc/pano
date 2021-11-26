package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PanelQuickNotificationsAndReadAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/quickNotificationsAndRead")

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
            databaseManager.getDatabase().panelNotificationDao.getLast5ByUserID(
                idOrToken,
                sqlConnection,
                (this::getLast5ByUserIDHandler)(handler, sqlConnection, idOrToken)
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
        sqlConnection: SqlConnection,
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_72))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.getLast5ByUserID(
            userID,
            sqlConnection,
            (this::getLast5ByUserIDHandler)(handler, sqlConnection, userID)
        )
    }

    private fun getLast5ByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        userID: Int
    ) = handler@{ notifications: List<PanelNotification>?, _: AsyncResult<*> ->
        if (notifications == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_71))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.getCountOfNotReadByUserID(
            userID,
            sqlConnection,
            (this::getCountByUserIDHandler)(handler, sqlConnection, userID, notifications)
        )
    }

    private fun getCountByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        userID: Int,
        notifications: List<PanelNotification>
    ) = handler@{ count: Int?, _: AsyncResult<*> ->
        if (count == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_70))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.markReadLat5ByUserID(
            userID,
            sqlConnection,
            (this::markReadLat5ByUserIDHandler)(handler, sqlConnection, notifications, userID, count)
        )
    }

    private fun markReadLat5ByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        notifications: List<PanelNotification>,
        userID: Int,
        count: Int
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (result == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_69))

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