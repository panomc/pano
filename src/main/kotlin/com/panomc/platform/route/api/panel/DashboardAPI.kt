package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.SystemProperty
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class DashboardAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/initPage/dashboard")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
                return@createConnection
            }

            val token = context.getCookie("pano_token").value

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                token,
                sqlConnection
            ) { userID, _ ->
                if (userID == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_16))
                    }
                else
                    databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
                        userID,
                        sqlConnection
                    ) { isUserInstalled, _ ->
                        if (isUserInstalled == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_17))
                            }
                        else
                            databaseManager.getDatabase().userDao.count(sqlConnection) { countOfUsers, _ ->
                                if (countOfUsers == null)
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_18))
                                    }
                                else
                                    databaseManager.getDatabase().postDao.count(
                                        sqlConnection
                                    ) { countOfPosts, _ ->
                                        if (countOfPosts == null)
                                            databaseManager.closeConnection(sqlConnection) {
                                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_19))
                                            }
                                        else
                                            databaseManager.getDatabase().ticketDao.count(
                                                sqlConnection
                                            ) { countOfTickets, _ ->
                                                if (countOfTickets == null)
                                                    databaseManager.closeConnection(sqlConnection) {
                                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_112))
                                                    }
                                                else
                                                    databaseManager.getDatabase().ticketDao.countOfOpenTickets(
                                                        sqlConnection
                                                    ) { countOfOpenTickets, _ ->
                                                        if (countOfOpenTickets == null)
                                                            databaseManager.closeConnection(sqlConnection) {
                                                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_118))
                                                            }
                                                        else
                                                            databaseManager.getDatabase().ticketDao.getLast5Tickets(
                                                                sqlConnection
                                                            ) { tickets, _ ->
                                                                if (tickets == null)
                                                                    databaseManager.closeConnection(sqlConnection) {
                                                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_76))
                                                                    }
                                                                else {
                                                                    val result = mutableMapOf<String, Any?>(
                                                                        "registered_player_count" to countOfUsers,
                                                                        "post_count" to countOfPosts,
                                                                        "tickets_count" to countOfTickets,
                                                                        "open_tickets_count" to countOfOpenTickets,
                                                                        "tickets" to tickets
                                                                    )

                                                                    if (!isUserInstalled) {
                                                                        result["getting_started_blocks"] = mapOf(
                                                                            "welcome_board" to false
                                                                        )

                                                                        databaseManager.closeConnection(sqlConnection) {
                                                                            handler.invoke(Successful(result))
                                                                        }
                                                                    } else
                                                                        databaseManager.getDatabase().systemPropertyDao.getValue(
                                                                            SystemProperty(
                                                                                -1,
                                                                                "show_getting_started",
                                                                                ""
                                                                            ),
                                                                            sqlConnection
                                                                        ) { systemProperty, _ ->
                                                                            if (systemProperty == null)
                                                                                databaseManager.closeConnection(
                                                                                    sqlConnection
                                                                                ) {
                                                                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_20))
                                                                                }
                                                                            else {
                                                                                result["getting_started_blocks"] =
                                                                                    mapOf(
                                                                                        "welcome_board" to systemProperty.value
                                                                                    )

                                                                                databaseManager.closeConnection(
                                                                                    sqlConnection
                                                                                ) {
                                                                                    handler.invoke(Successful(result))
                                                                                }
                                                                            }

                                                                        }
                                                                }
                                                            }
                                                    }
                                            }

                                    }
                            }
                    }
            }
        }
    }
}