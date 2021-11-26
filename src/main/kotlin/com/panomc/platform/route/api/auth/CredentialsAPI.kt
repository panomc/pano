package com.panomc.platform.route.api.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.User
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class CredentialsAPI : LoggedInApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/auth/credentials")

    override fun handler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val idOrToken = LoginUtil.getUserIDOrToken(context)

        databaseManager.createConnection(
            (this::createConnectionHandler)(
                handler,
                idOrToken!!
            )
        )
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        idOrToken: Any
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        if (idOrToken is Int) {
            databaseManager.getDatabase().userDao.getByID(
                idOrToken,
                sqlConnection,
                (this::getByIDHandler)(handler, sqlConnection)
            )

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            idOrToken as String,
            sqlConnection,
            (this::getUserIDFromTokenHandler)(handler, sqlConnection)
        )
    }

    private fun getUserIDFromTokenHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_243))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.getByID(
            userID,
            sqlConnection,
            (this::getByIDHandler)(handler, sqlConnection)
        )
    }

    private fun getByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ user: User?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (user == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_242))

                return@closeConnection
            }

            handler.invoke(
                Successful(
                    mapOf(
                        "username" to user.username,
                        "email" to user.email,
                        "panelAccess" to (user.permissionGroupID != -1)
                    )
                )
            )
        }
    }
}