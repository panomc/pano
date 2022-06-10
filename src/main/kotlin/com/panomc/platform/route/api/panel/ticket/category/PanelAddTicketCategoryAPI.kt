package com.panomc.platform.route.api.panel.ticket.category

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.model.*
import com.panomc.platform.util.AuthProvider
import com.panomc.platform.util.SetupManager
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies.json
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.objectSchema
import io.vertx.json.schema.common.dsl.Schemas.stringSchema

@Endpoint
class PanelAddTicketCategoryAPI(
    private val databaseManager: DatabaseManager,
    setupManager: SetupManager,
    authProvider: AuthProvider
) : PanelApi(setupManager, authProvider) {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/ticket/category")

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandler.builder(schemaParser)
            .body(
                json(
                    objectSchema()
                        .property("title", stringSchema())
                        .property("description", stringSchema())
                )
            )
            .build()

    override suspend fun handler(context: RoutingContext): Result {
        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val title = data.getString("title")
        val description = data.getString("description")

        validateForm(title)

        val sqlConnection = createConnection(databaseManager, context)

        databaseManager.ticketCategoryDao.add(TicketCategory(title = title, description = description), sqlConnection)

        return Successful()
    }

    private fun validateForm(
        title: String,
//        description: String,
    ) {
        val errors = mutableMapOf<String, Boolean>()

        if (title.isEmpty() || title.length > 32)
            errors["title"] = true

//        if (description.isEmpty())
//            errors["description"] = true

        if (errors.isNotEmpty()) {
            throw Errors(errors)
        }
    }
}