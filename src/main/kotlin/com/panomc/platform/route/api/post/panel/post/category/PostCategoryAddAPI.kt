package com.panomc.platform.route.api.post.panel.post.category

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import java.util.*
import javax.inject.Inject

class PostCategoryAddAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/category/add")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val title = data.getString("title")
        val description = data.getString("description")
        val url = data.getString("url")
        val color = data.getString("color")

        if (color.length != 7) {
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

        if (title.isEmpty() || title.length > 32)
            errors["title"] = true

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
                            addCategoryToDB(connection, title, description, url, color, handler) { id ->
                                databaseManager.closeConnection(connection) {
                                    handler.invoke(
                                        Successful(
                                            mapOf(
                                                "id" to id
                                            )
                                        )
                                    )
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
            "SELECT COUNT(id) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post_category WHERE url = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(url)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1)
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_94))
                }
        }
    }

    private fun addCategoryToDB(
        connection: Connection,
        title: String,
        description: String,
        url: String,
        color: String,
        resultHandler: (result: Result) -> Unit,
        handler: (id: Int) -> Unit
    ) {
        val query =
            "INSERT INTO ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post_category (`title`, `description`, `url`, `color`) VALUES (?, ?, ?, ?)"

        databaseManager.getSQLConnection(connection).updateWithParams(
            query,
            JsonArray()
                .add(Base64.getEncoder().encodeToString(title.toByteArray()))
                .add(Base64.getEncoder().encodeToString(description.toByteArray()))
                .add(url)
                .add(color.replace("#", ""))
        ) { queryResult ->
            if (queryResult.succeeded())

                handler.invoke(queryResult.result().keys.getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_93))
                }
        }
    }
}