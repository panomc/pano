package com.panomc.platform.route.api.get.panel.post.category

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class CategoriesAPI : Api() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/post/category/categories")

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

        val response = context.response()

        val auth = Auth()

        auth.isAdmin(context) { isAdmin ->
            if (isAdmin) {
                response
                    .putHeader("content-type", "application/json; charset=utf-8")

                getCategoriesData() { result ->
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

    private fun getCategoriesData(handler: (result: Result) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                getCountOfCategories(connection, handler) { countOfCategories ->
                    getCategories(connection, handler) { categories ->
                        val result = mutableMapOf<String, Any?>(
                            "categories" to categories,
                            "category_count" to countOfCategories
                        )

                        databaseManager.closeConnection(connection) {
                            handler.invoke(Successful(result))
                        }
                    }
                }
        }
    }

    private fun getCountOfCategories(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (postsCountByPageType: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category"

        databaseManager.getSQLConnection(connection).query(query) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_88))
                }
        }
    }

    private fun getCategories(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (categories: List<Map<String, Any>>) -> Unit
    ) {
        val query =
            "SELECT id, title, description, url, color FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category ORDER BY id DESC"

        databaseManager.getSQLConnection(connection).query(query) { queryResult ->
            if (queryResult.succeeded()) {
                val categories = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0)
                    queryResult.result().results.forEach { categoryInDB ->
                        categories.add(
                            mapOf(
                                "id" to categoryInDB.getInteger(0),
                                "title" to categoryInDB.getString(1),
                                "description" to categoryInDB.getString(2),
                                "url" to categoryInDB.getString(3),
                                "color" to categoryInDB.getString(4)
                            )
                        )
                    }

                handler.invoke(categories)
            } else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_87))
                }
        }
    }
}