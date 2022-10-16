package com.panomc.platform.route.api.panel.playerDetail

import com.panomc.platform.ErrorCode
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.platform.util.AuthProvider
import com.panomc.platform.util.MailType
import com.panomc.platform.util.MailUtil
import com.panomc.platform.util.SetupManager
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies.json
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.objectSchema
import io.vertx.json.schema.common.dsl.Schemas.stringSchema

@Endpoint
class PanelSendValidationEmailAPI(
    setupManager: SetupManager,
    authProvider: AuthProvider,
    private val databaseManager: DatabaseManager,
    private val mailUtil: MailUtil
) : PanelApi(setupManager, authProvider) {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/sendValidationEmail")

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .body(
                json(
                    objectSchema()
                        .property("username", stringSchema())
                )
            )
            .build()

    override suspend fun handler(context: RoutingContext): Result {
        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val username = data.getString("username")

        val sqlConnection = databaseManager.createConnection()

        val isExists = databaseManager.userDao.isExistsByUsername(username, sqlConnection)

        if (!isExists) {
            throw Error(ErrorCode.NOT_EXISTS)
        }

        val userId =
            databaseManager.userDao.getUserIdFromUsername(username, sqlConnection) ?: throw Error(ErrorCode.NOT_EXISTS)

        val isEmailVerified = databaseManager.userDao.isEmailVerifiedById(userId, sqlConnection)

        if (isEmailVerified) {
            throw Error(ErrorCode.EMAIL_ALREADY_VERIFIED)
        }

        mailUtil.sendMail(sqlConnection, userId, MailType.ACTIVATION)

        return Successful()
    }
}