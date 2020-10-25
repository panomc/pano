package com.panomc.platform.route.api.post.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
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

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                isPostExistsByID(connection, id, handler) { exists ->
                    if (!exists)
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.NOT_EXISTS))
                        }
                    else
                        deletePostByID(connection, id, handler) {
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Successful())
                            }
                        }
                }
        }
    }

    private fun isPostExistsByID(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (exists: Boolean) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post WHERE id = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1)
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DELETE_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_102))
                }
        }
    }

    private fun deletePostByID(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "DELETE FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post WHERE id = ?"

        databaseManager.getSQLConnection(connection).updateWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke()
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DELETE_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_101))
                }
        }
    }
}