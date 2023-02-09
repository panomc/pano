package com.panomc.platform.route.api.panel.post.category

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.PanelPermission
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.PostCategory
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies.json
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.objectSchema
import io.vertx.json.schema.common.dsl.Schemas.stringSchema

@Endpoint
class PanelAddPostCategoryAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/post/category", RouteType.POST))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .body(
                json(
                    objectSchema()
                        .property("title", stringSchema())
                        .property("description", stringSchema())
                        .property("url", stringSchema())
                        .optionalProperty("color", stringSchema())
                )
            )
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(PanelPermission.MANAGE_POSTS, context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val title = data.getString("title")
        val description = data.getString("description")
        val url = data.getString("url")
        val color = data.getString("color") ?: "#1976d2"

        validateForm(title, url, color)

        val sqlConnection = createConnection(context)

        val exists = databaseManager.postCategoryDao.existsByUrl(url, sqlConnection)

        if (exists) {
            val errors = mutableMapOf<String, Boolean>()

            errors["url"] = true

            throw Errors(errors)
        }

        val id = databaseManager.postCategoryDao.add(
            PostCategory(title = title, description = description, url = url, color = color),
            sqlConnection
        )

        return Successful(
            mapOf(
                "id" to id
            )
        )
    }

    private fun validateForm(
        title: String,
//        description: String,
        url: String,
        color: String
    ) {
//        if (color.length != 7) {
//            throw Error(ErrorCode.UNKNOWN)
//        }

        val errors = mutableMapOf<String, Boolean>()

        if (title.isEmpty() || title.length > 32)
            errors["title"] = true

//        if (description.isEmpty())
//            errors["description"] = true

        if (url.isEmpty() || url.length < 3 || url.length > 32 || !url.matches(Regex("^[a-zA-Z0-9-]+\$")))
            errors["url"] = true

        if (errors.isNotEmpty()) {
            throw Errors(errors)
        }
    }
}