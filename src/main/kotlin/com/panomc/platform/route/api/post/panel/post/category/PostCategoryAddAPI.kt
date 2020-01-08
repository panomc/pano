package com.panomc.platform.route.api.post.panel.post.category

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostCategoryAddAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/category/add")

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

                addCategory(context) { result ->
                    when (result) {
                        is Successful -> {
                            val responseMap = mutableMapOf<String, Any?>(
                                "result" to "ok"
                            )

                            responseMap.putAll(result.map)

                            response.end(
                                JsonObject(
                                    responseMap
                                ).toJsonString()
                            )
                        }
                        is Error -> response.end(
                            JsonObject(
                                mapOf(
                                    "result" to "error",
                                    "error" to result.errorCode
                                )
                            ).toJsonString()
                        )
                        is Errors -> response.end(
                            JsonObject(
                                mapOf(
                                    "result" to "error",
                                    "error" to result.errors
                                )
                            ).toJsonString()
                        )
                    }
                }
            } else
                context.reroute("/")
        }
    }

    private fun addCategory(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val name = data.getString("name")
        val description = data.getString("description")
        val url = data.getString("url")
        val colorCode = data.getString("colorCode")

        if (colorCode.length != 7) {
            context.response().end(
                JsonObject(
                    mapOf(
                        "result" to "error"
                    )
                ).toJsonString()
            )

            return
        }

        val errors = mutableMapOf<String, Boolean>()

        if (name.isEmpty() || name.length > 32)
            errors["name"] = true

        if (description.isEmpty())
            errors["description"] = true

        if (url.isEmpty() || url.length < 3 || url.length > 32 || !url.matches(Regex("^[a-zA-Z0-9]+\$")))
            errors["url"] = true

        if (errors.isNotEmpty())
            handler.invoke(
                Errors(
                    errors
                )
            )
        else
            databaseManager.createConnection { connection, _ ->
                if (connection == null)
                    handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
                else
                    isUrlExists(connection, url, handler) { exists ->
                        if (exists) {
                            errors["url"] = true

                            databaseManager.closeConnection(connection) {
                                handler.invoke(
                                    Errors(
                                        errors
                                    )
                                )
                            }
                        } else
                            addCategoryToDB(connection, name, description, url, colorCode, handler) {
                                databaseManager.closeConnection(connection) {
                                    handler.invoke(Successful())
                                }
                            }
                    }
            }
    }

    private fun isUrlExists(
        connection: Connection,
        url: String,
        resultHandler: (result: Result) -> Unit,
        handler: (exists: Boolean) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category WHERE url = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(url)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1)
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_49))
                }
        }
    }

    private fun addCategoryToDB(
        connection: Connection,
        name: String,
        description: String,
        url: String,
        colorCode: String,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "INSERT INTO ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category (`title`, `description`, `url`, `color`) VALUES (?, ?, ?, ?)"

        databaseManager.getSQLConnection(connection).updateWithParams(
            query,
            JsonArray()
                .add(name)
                .add(description)
                .add(url)
                .add(colorCode.replace("#", ""))
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke()
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_48))
                }
        }
    }

    private class Errors(val errors: Map<String, Any>) : Result()
}