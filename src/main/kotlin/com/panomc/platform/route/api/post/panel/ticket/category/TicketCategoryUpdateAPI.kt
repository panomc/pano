package com.panomc.platform.route.api.post.panel.ticket.category

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class TicketCategoryUpdateAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/ticket/category/update")

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

        val errors = mutableMapOf<String, Boolean>()

        if (name.isEmpty() || name.length > 32)
            errors["name"] = true

        if (description.isEmpty())
            errors["description"] = true

        if (errors.isNotEmpty())
            handler.invoke(Errors(errors))
        else
            databaseManager.createConnection { connection, _ ->
                if (connection == null)
                    handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
                else
                    updateCategoryInDB(connection, id, name, description, handler) {
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Successful())
                        }
                    }
            }
    }

    private fun updateCategoryInDB(
        connection: Connection,
        id: Int,
        name: String,
        description: String,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "UPDATE ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}ticket_category SET title = ?, description = ? WHERE id = ?"

        databaseManager.getSQLConnection(connection)
            .updateWithParams(query, JsonArray().add(name).add(description).add(id)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke()
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.TICKET_CATEGORY_UPDATE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_92))
                    }
            }
    }

    private class Errors(val errors: Map<String, Any>) : Result()
}