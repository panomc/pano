package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.SystemProperty
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class DashboardAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/initPage/dashboard")

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
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_16))
            }

            return@handler
        }

        databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
            userID,
            sqlConnection,
            (this::isUserInstalledSystemByUserIDHandler)(handler, sqlConnection)
        )
    }

    private fun isUserInstalledSystemByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ isUserInstalled: Boolean?, _: AsyncResult<*> ->
        if (isUserInstalled == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_17))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.count(
            sqlConnection,
            (this::userDaoCountHandler)(handler, sqlConnection, isUserInstalled)
        )
    }

    private fun userDaoCountHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        isUserInstalled: Boolean
    ) = handler@{ userCount: Int?, _: AsyncResult<*> ->
        if (userCount == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_18))
            }

            return@handler
        }

        databaseManager.getDatabase().postDao.count(
            sqlConnection,
            (this::postDaoCount)(handler, sqlConnection, isUserInstalled, userCount)
        )
    }

    private fun postDaoCount(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        isUserInstalled: Boolean,
        userCount: Int
    ) = handler@{ postCount: Int?, _: AsyncResult<*> ->
        if (postCount == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_19))
            }

            return@handler
        }

        databaseManager.getDatabase().ticketDao.count(
            sqlConnection,
            (this::ticketDaoCount)(handler, sqlConnection, isUserInstalled, userCount, postCount)
        )
    }

    private fun ticketDaoCount(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        isUserInstalled: Boolean,
        userCount: Int,
        postCount: Int
    ) = handler@{ ticketCount: Int?, _: AsyncResult<*> ->
        if (ticketCount == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_112))
            }

            return@handler
        }

        databaseManager.getDatabase().ticketDao.countOfOpenTickets(
            sqlConnection,
            (this::countOfOpenTicketsHandler)(
                handler,
                sqlConnection,
                isUserInstalled,
                userCount,
                postCount,
                ticketCount
            )
        )
    }

    private fun countOfOpenTicketsHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        isUserInstalled: Boolean,
        userCount: Int,
        postCount: Int,
        ticketCount: Int
    ) = handler@{ openTicketCount: Int?, _: AsyncResult<*> ->
        if (openTicketCount == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_118))
            }

            return@handler
        }

        databaseManager.getDatabase().ticketDao.getLast5Tickets(
            sqlConnection,
            (this::getLast5TicketsHandler)(
                handler,
                sqlConnection,
                isUserInstalled,
                userCount,
                postCount,
                ticketCount,
                openTicketCount
            )
        )
    }

    private fun getLast5TicketsHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        isUserInstalled: Boolean,
        userCount: Int,
        postCount: Int,
        ticketCount: Int,
        openTicketCount: Int
    ) = handler@{ tickets: List<Map<String, Any>>?, _: AsyncResult<*> ->
        if (tickets == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_76))
            }

            return@handler
        }

        val result = mutableMapOf<String, Any?>(
            "registered_player_count" to userCount,
            "post_count" to postCount,
            "tickets_count" to ticketCount,
            "open_tickets_count" to openTicketCount,
            "tickets" to tickets
        )

        if (!isUserInstalled) {
            result["getting_started_blocks"] = mapOf(
                "welcome_board" to false
            )

            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Successful(result))
            }

            return@handler
        }

        databaseManager.getDatabase().systemPropertyDao.getValue(
            SystemProperty(
                -1,
                "show_getting_started",
                ""
            ),
            sqlConnection,
            (this::getValueHandler)(handler, sqlConnection, result)
        )
    }

    private fun getValueHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ systemProperty: SystemProperty?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (systemProperty == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_20))

                return@closeConnection
            }

            result["getting_started_blocks"] = mapOf(
                "welcome_board" to systemProperty.value
            )

            handler.invoke(Successful(result))
        }
    }
}