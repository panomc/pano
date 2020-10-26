package com.panomc.platform.route.api.post.panel.dashboard

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class CloseGettingStartedCardAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/dashboard/closeGettingStartedCard")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            val token = context.getCookie("pano_token").value

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                token,
                databaseManager.getSQLConnection(connection)
            ) { userID, _ ->
                if (userID == null)
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_23))
                    }
                else
                    databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
                        userID,
                        databaseManager.getSQLConnection(connection)
                    ) { result, _ ->
                        if (result == null)
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_24))
                            }
                        else
                            databaseManager.getDatabase().systemPropertyDao.update(
                                SystemProperty(
                                    -1,
                                    "false",
                                    "show_getting_started"
                                ), databaseManager.getSQLConnection(connection)
                            ) { updateResult, _ ->
                                databaseManager.closeConnection(connection) {
                                    if (updateResult == null)
                                        handler.invoke(Error(ErrorCode.CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_22))
                                    else
                                        handler.invoke(Successful())
                                }
                            }
                    }
            }
        }
    }
}