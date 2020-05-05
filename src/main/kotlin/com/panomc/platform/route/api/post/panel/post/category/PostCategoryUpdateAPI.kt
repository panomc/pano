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

class PostCategoryUpdateAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/category/update")

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

                updateCategory(context) { result ->
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

    private fun updateCategory(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val id = data.getInteger("id")
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
                    isUrlExists(connection, id, url, handler) { exists ->
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
                            updateCategoryToDB(connection, id, name, description, url, colorCode, handler) {
                                databaseManager.closeConnection(connection) {
                                    handler.invoke(Successful())
                                }
                            }
                    }
            }
    }

    private fun isUrlExists(
        connection: Connection,
        id: Int,
        url: String,
        resultHandler: (result: Result) -> Unit,
        handler: (exists: Boolean) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category WHERE url = ? and id != ?"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(url).add(id)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(queryResult.result().results[0].getInteger(0) == 1)
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_UPDATE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_96))
                    }
            }
    }

    private fun updateCategoryToDB(
        connection: Connection,
        id: Int,
        name: String,
        description: String,
        url: String,
        colorCode: String,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "UPDATE ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category SET title = ?, description = ?, url = ?, color = ? WHERE id = ?"

        databaseManager.getSQLConnection(connection).updateWithParams(
            query,
            JsonArray()
                .add(name)
                .add(description)
                .add(url)
                .add(colorCode.replace("#", ""))
                .add(id)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke()
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_UPDATE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_95))
                }
        }
    }

    private class Errors(val errors: Map<String, Any>) : Result()
}