package com.panomc.platform.route.api.setup

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.User
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import com.panomc.platform.util.RegisterUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection
import javax.inject.Inject

class FinishAPI : SetupApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/setup/finish")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        if (setupManager.getStep() != 3) {
            handler.invoke(Successful(setupManager.getCurrentStepData()))

            return
        }

        val data = context.bodyAsJson

        val username = data.getString("username")
        val email = data.getString("email")
        val password = data.getString("password")

        val remoteIP = context.request().remoteAddress().host()

        RegisterUtil.validateForm(
            username,
            email,
            email,
            password,
            password,
            false,
            "",
            null,
            (this::validateFormHandler)(handler, context, username, email, password, remoteIP)
        )
    }

    private fun validateFormHandler(
        handler: (result: Result) -> Unit,
        context: RoutingContext,
        username: String,
        email: String,
        password: String,
        remoteIP: String
    ) = handler@{ result: Result ->
        if (result is Error) {
            handler.invoke(result)

            return@handler
        }

        databaseManager.createConnection(
            (this::createConnectionHandler)(
                handler,
                context,
                username,
                email,
                password,
                remoteIP
            )
        )
    }


    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        context: RoutingContext,
        username: String,
        email: String,
        password: String,
        remoteIP: String
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.initDatabase(
            sqlConnection,
            (this::initDatabaseHandler)(handler, context, sqlConnection, username, email, password, remoteIP)
        )
    }

    private fun initDatabaseHandler(
        handler: (result: Result) -> Unit,
        context: RoutingContext,
        sqlConnection: SqlConnection,
        username: String,
        email: String,
        password: String,
        remoteIP: String
    ) = handler@{ asyncResult: AsyncResult<*> ->
        if (asyncResult.failed()) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_128))
            }

            return@handler
        }

        RegisterUtil.register(
            databaseManager,
            sqlConnection,
            User(-1, username, email, password, remoteIP, 0, System.currentTimeMillis().toString()),
            true,
            (this::registerHandler)(handler, context, sqlConnection, username, password)
        )
    }

    private fun registerHandler(
        handler: (result: Result) -> Unit,
        context: RoutingContext,
        sqlConnection: SqlConnection,
        username: String,
        password: String,
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        if (result == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_144))
            }

            return@handler
        }

        if (result is Error) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(result)
            }

            return@handler
        }

        LoginUtil.login(
            username,
            password,
            true,
            context,
            databaseManager,
            sqlConnection,
            (this::loginHandler)(handler, sqlConnection)
        )
    }

    private fun loginHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ isLoggedIn: Boolean?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (isLoggedIn == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_129))

                return@closeConnection
            }

            if (isLoggedIn) {
                setupManager.finishSetup()

                handler.invoke(Successful())

                return@closeConnection
            }

            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_130))
        }
    }
}