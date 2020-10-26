package com.panomc.platform.route.api.post.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostOnlyPublishAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/onlyPublish")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().postDao.isExistsByID(
                id,
                databaseManager.getSQLConnection(connection)
            ) { exists, _ ->
                if (exists == null) {
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.PUBLISH_ONLY_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_104))
                    }

                    return@isExistsByID
                }

                if (!exists) {
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.NOT_EXISTS))
                    }

                    return@isExistsByID
                }

                databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                    token,
                    databaseManager.getSQLConnection(connection)
                ) { userID, _ ->
                    if (userID == null) {
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.PUBLISH_ONLY_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_105))
                        }

                        return@getUserIDFromToken
                    }

                    databaseManager.getDatabase().postDao.publishByID(
                        id,
                        userID,
                        databaseManager.getSQLConnection(connection)
                    ) { result, _ ->
                        if (result == null) {
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.PUBLISH_ONLY_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_103))
                            }

                            return@publishByID
                        }

                        databaseManager.closeConnection(connection) {
                            handler.invoke(Successful())
                        }
                    }
                }
            }
        }
    }
}