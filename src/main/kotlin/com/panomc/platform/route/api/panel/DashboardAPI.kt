package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.SystemProperty
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class DashboardAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/initPage/dashboard")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

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
                        handler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_16))
                    }
                else
                    databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
                        userID,
                        sqlConnection
                    ) { isUserInstalled, _ ->
                        if (isUserInstalled == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_17))
                            }
                        else
                            databaseManager.getDatabase().userDao.count(sqlConnection) { countOfUsers, _ ->
                                if (countOfUsers == null)
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_18))
                                    }
                                else
                                    databaseManager.getDatabase().postDao.count(
                                        sqlConnection
                                    ) { countOfPosts, _ ->
                                        if (countOfPosts == null)
                                            databaseManager.closeConnection(sqlConnection) {
                                                handler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_19))
                                            }
                                        else
                                            databaseManager.getDatabase().ticketDao.count(
                                                sqlConnection
                                            ) { countOfTickets, _ ->
                                                if (countOfTickets == null)
                                                    databaseManager.closeConnection(sqlConnection) {
                                                        handler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_112))
                                                    }
                                                else
                                                    databaseManager.getDatabase().ticketDao.countOfOpenTickets(
                                                        sqlConnection
                                                    ) { countOfOpenTickets, _ ->
                                                        if (countOfOpenTickets == null)
                                                            databaseManager.closeConnection(sqlConnection) {
                                                                handler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_118))
                                                            }
                                                        else
                                                            databaseManager.getDatabase().ticketDao.getLast5Tickets(
                                                                sqlConnection
                                                            ) { tickets, _ ->
                                                                if (tickets == null)
                                                                    databaseManager.closeConnection(sqlConnection) {
                                                                        handler.invoke(Error(ErrorCode.TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_76))
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
                                                                                    handler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_20))
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