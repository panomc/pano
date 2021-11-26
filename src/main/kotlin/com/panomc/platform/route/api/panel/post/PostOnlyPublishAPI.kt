package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PostOnlyPublishAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/onlyPublish")

    override fun handler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        val idOrToken = LoginUtil.getUserIDOrToken(context)

        if (idOrToken == null || (idOrToken !is Int && idOrToken !is String)) {
            handler.invoke(Error(ErrorCode.NOT_LOGGED_IN))

            return
        }

        databaseManager.createConnection(
            (this::createConnectionHandler)(
                handler,
                id,
                idOrToken
            )
        )
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        id: Int,
        idOrToken: Any
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        if (idOrToken is Int) {
            databaseManager.getDatabase().postDao.isExistsByID(
                id,
                sqlConnection,
                (this::isExistsByIDHandler)(handler, sqlConnection, id, idOrToken)
            )

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            idOrToken as String,
            sqlConnection,
            (this::getUserIDFromTokenHandler)(handler, sqlConnection, id)
        )
    }

    private fun getUserIDFromTokenHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        id: Int,
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_105))
            }

            return@handler
        }

        databaseManager.getDatabase().postDao.isExistsByID(
            id,
            sqlConnection,
            (this::isExistsByIDHandler)(handler, sqlConnection, id, userID)
        )
    }

    private fun isExistsByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        id: Int,
        userID: Int
    ) = handler@{ exists: Boolean?, _: AsyncResult<*> ->
        if (exists == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_104))
            }

            return@handler
        }

        if (!exists) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.NOT_EXISTS))
            }

            return@handler
        }

        databaseManager.getDatabase().postDao.publishByID(
            id,
            userID,
            sqlConnection,
            (this::publishByIDHandler)(handler, sqlConnection)
        )
    }

    private fun publishByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (result == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_103))

                return@closeConnection
            }

            handler.invoke(Successful())
        }
    }
}