package com.panomc.platform.route.api.post.panel.post

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostMoveTrashAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/moveTrash")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (!setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        val auth = Auth()

        auth.isAdmin(context) { isAdmin ->
            if (isAdmin) {
                val response = context.response()

                response
                    .putHeader("content-type", "application/json; charset=utf-8")

                moveTrash(context) { result ->
                    if (result is Successful) {
                        val responseMap = mutableMapOf<String, Any?>(
                            "result" to "ok"
                        )

                        responseMap.putAll(result.map)

                        response.end(
                            JsonObject(
                                responseMap
                            ).toJsonString()
                        )
                    } else if (result is Error)
                        response.end(
                            JsonObject(
                                mapOf(
                                    "result" to "error",
                                    "error" to result.errorCode
                                )
                            ).toJsonString()
                        )
                }
            } else
                context.reroute("/")
        }
    }

    private fun moveTrash(context: RoutingContext, handler: (result: Result) -> Unit) {
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
                        moveTrashInDB(connection, id, handler) {
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
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post WHERE id = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1)
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.MOVE_TRASH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_62))
                }
        }
    }

    private fun moveTrashInDB(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "UPDATE ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post SET move_date = ?, status = ? WHERE id = ?"

        databaseManager.getSQLConnection(connection).updateWithParams(
            query,
            JsonArray()
                .add(System.currentTimeMillis())
                .add(0)
                .add(id)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke()
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.MOVE_TRASH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_61))
                }
        }
    }
}