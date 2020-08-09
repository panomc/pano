package com.panomc.platform.route.api.post.panel.post.category

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostCategoryDeleteAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/category/delete")

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
                isCategoryExistsByID(connection, id, handler) { exists ->
                    if (!exists)
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.NOT_EXISTS))
                        }
                    else
                        removePostCategoriesByCategoryID(connection, id, handler) {
                            deleteCategoryByID(connection, id, handler) {
                                databaseManager.closeConnection(connection) {
                                    handler.invoke(Successful())
                                }
                            }
                        }
                }
        }
    }

    private fun isCategoryExistsByID(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (exists: Boolean) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category WHERE id = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1)
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_98))
                }
        }
    }

    private fun removePostCategoriesByCategoryID(
        connection: Connection,
        categoryID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "UPDATE ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post SET category_id = ? WHERE category_id = ?"

        databaseManager.getSQLConnection(connection)
            .updateWithParams(query, JsonArray().add(-1).add(categoryID)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke()
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_121))
                    }
            }
    }

    private fun deleteCategoryByID(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "DELETE FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category WHERE id = ?"

        databaseManager.getSQLConnection(connection).updateWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke()
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_97))
                }
        }
    }
}