package com.panomc.platform.route.api.post.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostMoveTrashAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/moveTrash")

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
                when {
                    exists == null -> databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.MOVE_TRASH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_107))
                    }
                    exists -> databaseManager.getDatabase().postDao.moveTrashByID(
                        id,
                        databaseManager.getSQLConnection(connection)
                    ) { result, _ ->
                        if (result == null)
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.MOVE_TRASH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_106))
                            }
                        else
                            handler.invoke(Successful())
                    }
                    else -> databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.NOT_EXISTS))
                    }
                }
            }
        }
    }

}