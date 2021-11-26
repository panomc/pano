package com.panomc.platform.route.api.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class LoginAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/auth/login")

    override fun handler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val usernameOrEmail = data.getString("usernameOrEmail")
        val password = data.getString("password")
        val rememberMe = data.getBoolean("rememberMe")
        val recaptcha = data.getString("recaptcha")

        validateForm(handler, usernameOrEmail, password, recaptcha) {
            databaseManager.createConnection(
                (this::createConnectionHandler)(
                    handler,
                    usernameOrEmail,
                    password,
                    rememberMe,
                    context
                )
            )
        }
    }

    private fun validateForm(
        handler: (result: Result) -> Unit,
        usernameOrEmail: String,
        password: String,
        recaptcha: String,
        successHandler: () -> Unit
    ) {
        if (usernameOrEmail.isEmpty()) {
            handler.invoke(Error(ErrorCode.LOGIN_LOGIN_IS_INVALID))

            return
        }

        if (!usernameOrEmail.matches(Regex("^[a-zA-Z0-9_]+\$")) && !usernameOrEmail.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {
            handler.invoke(Error(ErrorCode.LOGIN_LOGIN_IS_INVALID))

            return
        }

        if (password.isEmpty()) {
            handler.invoke(Error(ErrorCode.LOGIN_LOGIN_IS_INVALID))

            return
        }

        if (password.length < 6 || password.length > 128) {
            handler.invoke(Error(ErrorCode.LOGIN_LOGIN_IS_INVALID))

            return
        }

//        if (!this.reCaptcha.isValid(reCaptcha)) {
//            handler.invoke(Error(ErrorCode.RECAPTCHA_NOT_VALID))
//
//            return
//        }

        successHandler.invoke()
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        usernameOrEmail: String,
        password: String,
        rememberMe: Boolean,
        routingContext: RoutingContext
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().userDao.isLoginCorrect(
            usernameOrEmail,
            password,
            sqlConnection,
            (this::isLoginCorrectHandler)(sqlConnection, handler, usernameOrEmail, rememberMe, routingContext)
        )
    }

    private fun isLoginCorrectHandler(
        sqlConnection: SqlConnection,
        handler: (result: Result) -> Unit,
        usernameOrEmail: String,
        rememberMe: Boolean,
        routingContext: RoutingContext
    ) = handler@{ isLoginCorrect: Boolean?, _: AsyncResult<*> ->
        if (isLoginCorrect == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_216))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.getUserIDFromUsernameOrEmail(
            usernameOrEmail,
            sqlConnection,
            (this::getUserIDFromUsernameOrEmailHandler)(
                sqlConnection,
                handler,
                usernameOrEmail,
                rememberMe,
                routingContext
            )
        )
    }

    private fun getUserIDFromUsernameOrEmailHandler(
        sqlConnection: SqlConnection,
        handler: (result: Result) -> Unit,
        usernameOrEmail: String,
        rememberMe: Boolean,
        routingContext: RoutingContext
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_217))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.isEmailVerifiedByID(
            userID,
            sqlConnection,
            (this::isEmailVerifiedByIDHandler)(sqlConnection, handler, usernameOrEmail, rememberMe, routingContext)
        )
    }

    private fun isEmailVerifiedByIDHandler(
        sqlConnection: SqlConnection,
        handler: (result: Result) -> Unit,
        usernameOrEmail: String,
        rememberMe: Boolean,
        routingContext: RoutingContext
    ) = handler@{ isVerified: Boolean?, _: AsyncResult<*> ->
        if (isVerified == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_218))
            }

            return@handler
        }

        if (!isVerified) {
            // TODO v2 Add sending e-mail again

            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.LOGIN_EMAIL_NOT_VERIFIED))
            }

            return@handler
        }

        LoginUtil.login(
            usernameOrEmail,
            rememberMe,
            routingContext,
            databaseManager,
            sqlConnection,
            (this::loginHandler)(sqlConnection, handler)
        )
    }

    private fun loginHandler(
        sqlConnection: SqlConnection,
        handler: (result: Result) -> Unit
    ) = handler@{ isLoggedIn: Any, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (isLoggedIn is Error) {
                handler.invoke(isLoggedIn)

                return@closeConnection
            }

            if (isLoggedIn is Boolean && !isLoggedIn) {
                handler.invoke(Error(ErrorCode.LOGIN_LOGIN_IS_INVALID))

                return@closeConnection
            }

            handler.invoke(Successful())
        }
    }
}