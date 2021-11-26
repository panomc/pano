package com.panomc.platform.route.api.panel.dashboard

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.SystemProperty
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class CloseConnectServerCardAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/dashboard/closeConnectServerCard")

    override fun handler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val idOrToken = LoginUtil.getUserIDOrToken(context)

        if (idOrToken == null || (idOrToken !is Int && idOrToken !is String)) {
            handler.invoke(Error(ErrorCode.NOT_LOGGED_IN))

            return
        }

        databaseManager.createConnection((this::createConnectionHandler)(handler, idOrToken))
    }

    private fun createConnectionHandler(handler: (result: Result) -> Unit, idOrToken: Any) =
        handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@handler
            }

            if (idOrToken is Int) {
                databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
                    idOrToken,
                    sqlConnection,
                    (this::isUserInstalledSystemByUserIDHandler)(handler, sqlConnection)
                )

                return@handler
            }

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                idOrToken as String,
                sqlConnection,
                (this::getUserIDFromTokenHandler)(handler, sqlConnection)
            )
        }

    private fun getUserIDFromTokenHandler(handler: (result: Result) -> Unit, sqlConnection: SqlConnection) =
        handler@{ userID: Int?, _: AsyncResult<*> ->
            if (userID == null) {
                databaseManager.closeConnection(sqlConnection) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_26))
                }

                return@handler
            }

            databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
                userID,
                sqlConnection,
                (this::isUserInstalledSystemByUserIDHandler)(handler, sqlConnection)
            )
        }

    private fun isUserInstalledSystemByUserIDHandler(handler: (result: Result) -> Unit, sqlConnection: SqlConnection) =
        handler@{ isUserInstalledSystem: Boolean?, _: AsyncResult<*> ->
            if (isUserInstalledSystem == null) {
                databaseManager.closeConnection(sqlConnection) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_27))
                }

                return@handler
            }

            databaseManager.getDatabase().systemPropertyDao.update(
                SystemProperty(
                    -1,
                    "false",
                    "show_connect_server_info"
                ),
                sqlConnection,
                (this::updateHandler)(handler, sqlConnection)
            )
        }

    private fun updateHandler(handler: (result: Result) -> Unit, sqlConnection: SqlConnection) =
        handler@{ result: Result?, _: AsyncResult<*> ->
            databaseManager.closeConnection(sqlConnection) {
                if (result == null)
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_25))
                else
                    handler.invoke(Successful())
            }
        }
}