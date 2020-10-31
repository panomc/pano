package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostDeleteAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/delete")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager


    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().postDao.isExistsByID(
                id,
                sqlConnection
            ) { exists, _ ->
                if (exists == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.DELETE_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_102))
                    }
                else {
                    if (!exists) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.NOT_EXISTS))
                        }

                        return@isExistsByID
                    }

                    databaseManager.getDatabase().postDao.delete(
                        id,
                        sqlConnection
                    ) { result, _ ->
                        if (result == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.DELETE_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_101))
                            }
                        else
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Successful())
                            }
                    }
                }

            }
        }
    }
}