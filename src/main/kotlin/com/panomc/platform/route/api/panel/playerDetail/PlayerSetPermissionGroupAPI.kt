package com.panomc.platform.route.api.panel.playerDetail

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.PermissionGroup
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PlayerSetPermissionGroupAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/player/set/permissionGroup")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val username = data.getString("username")
        val permissionGroup = data.getString("permission_group")

        databaseManager.createConnection(
            (this::createConnectionHandler)(
                handler,
                username,
                permissionGroup
            )
        )
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        username: String,
        permissionGroup: String,
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        if (permissionGroup == "-") {
            databaseManager.getDatabase().userDao.isExistsByUsername(
                username,
                sqlConnection,
                (this::isExistsByUsernameHandler)(sqlConnection, handler, username, 0)
            )

            return@handler
        }

        databaseManager.getDatabase().permissionGroupDao.isThere(
            PermissionGroup(-1, permissionGroup),
            sqlConnection,
            (this::isThereHandler)(sqlConnection, handler, username, permissionGroup)
        )
    }

    private fun isExistsByUsernameHandler(
        sqlConnection: SqlConnection,
        handler: (result: Result) -> Unit,
        username: String,
        permissionGroupID: Int
    ) = handler@{ exists: Boolean?, _: AsyncResult<*> ->
        if (exists == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_205))
            }

            return@handler
        }

        if (!exists) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.NOT_EXISTS))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.setPermissionGroupByUsername(
            permissionGroupID,
            username,
            sqlConnection,
            (this::setPermissionGroupByUsernameHandler)(sqlConnection, handler)
        )
    }

    private fun isThereHandler(
        sqlConnection: SqlConnection,
        handler: (result: Result) -> Unit,
        username: String,
        permissionGroup: String
    ) = handler@{ isTherePermissionGroup: Boolean?, _: AsyncResult<*> ->
        if (isTherePermissionGroup == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_202))
            }

            return@handler
        }

        if (!isTherePermissionGroup) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.NOT_EXISTS))
            }

            return@handler
        }

        databaseManager.getDatabase().permissionGroupDao.getPermissionGroupID(
            PermissionGroup(-1, permissionGroup),
            sqlConnection,
            (this::getPermissionGroupIDHandler)(sqlConnection, handler, username)
        )
    }

    private fun getPermissionGroupIDHandler(
        sqlConnection: SqlConnection,
        handler: (result: Result) -> Unit,
        username: String,
    ) = handler@{ permissionGroupID: Int?, _: AsyncResult<*> ->
        if (permissionGroupID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_203))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.isExistsByUsername(
            username,
            sqlConnection,
            (this::isExistsByUsernameHandler)(sqlConnection, handler, username, permissionGroupID)
        )
    }

    private fun setPermissionGroupByUsernameHandler(
        sqlConnection: SqlConnection,
        handler: (result: Result) -> Unit
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (result == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_204))
            }

            handler.invoke(Successful())
        }
    }
}