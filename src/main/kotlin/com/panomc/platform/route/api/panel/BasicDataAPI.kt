package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.platform.util.PlatformCodeManager
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class BasicDataAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/basicData")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var platformCodeManager: PlatformCodeManager

    @Inject
    lateinit var configManager: ConfigManager

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
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_8))
                    }
                else
                    databaseManager.getDatabase().userDao.getByID(
                        userID,
                        sqlConnection
                    ) { user, _ ->
                        if (user == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_7))
                            }
                        else
                            databaseManager.getDatabase().panelNotificationDao.getCountByUserID(
                                userID,
                                sqlConnection
                            ) { count, _ ->
                                if (count == null)
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_68))
                                    }
                                else
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(
                                            Successful(
                                                mapOf(
                                                    "user" to mapOf(
                                                        "username" to user.username,
                                                        "email" to user.email,
                                                        "permission_id" to user.permissionID
                                                    ),
                                                    "website" to mapOf(
                                                        "name" to configManager.getConfig()["website-name"],
                                                        "description" to configManager.getConfig()["website-description"]
                                                    ),
                                                    "platform_server_match_key" to platformCodeManager.getPlatformKey(),
                                                    "platform_server_match_key_time_started" to platformCodeManager.getTimeStarted(),
                                                    "platform_host_address" to context.request().host(),
                                                    "servers" to listOf<Map<String, Any?>>(),
                                                    "notifications_count" to count
                                                )
                                            )
                                        )
                                    }
                            }
                    }
            }
        }
    }
}