package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostMoveDraftAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/moveDraft")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

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
                        handler.invoke(Error(ErrorCode.MOVE_DRAFT_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_109))
                    }

                    return@isExistsByID
                }

                if (!exists) {
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.NOT_EXISTS))
                    }

                    return@isExistsByID
                }

                databaseManager.getDatabase().postDao.moveDraftByID(
                    id,
                    databaseManager.getSQLConnection(connection)
                ) { result, _ ->
                    if (result == null) {
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.MOVE_DRAFT_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_108))
                        }

                        return@moveDraftByID
                    }

                    databaseManager.closeConnection(connection) {
                        handler.invoke(Successful())
                    }
                }
            }
        }
    }
}