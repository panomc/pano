package com.panomc.platform.route.api.setup

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.User
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import com.panomc.platform.util.RegisterUtil
import io.vertx.ext.web.RoutingContext
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
            false
        ) { resultOfValidateForm ->
            if (resultOfValidateForm is Error) {
                handler.invoke(resultOfValidateForm)

                return@validateForm
            }

            databaseManager.createConnection { sqlConnection, _ ->
                if (sqlConnection == null) {
                    handler.invoke(Error(ErrorCode.FINISH_API_CANT_CONNECT_DATABASE_PLEASE_CHECK_YOUR_INFO))

                    return@createConnection
                }

                databaseManager.initDatabase(sqlConnection) {
                    if (it.failed()) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_128))

                        return@initDatabase
                    }

                    RegisterUtil.register(
                        databaseManager,
                        sqlConnection,
                        User(-1, username, email, password, remoteIP),
                        true
                    ) { resultOfRegister, _ ->
                        if (resultOfRegister == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_144))

                            return@register
                        }

                        if (resultOfRegister is Error) {
                            handler.invoke(resultOfValidateForm)

                            return@register
                        }

                        LoginUtil.login(
                            username,
                            password,
                            true,
                            context,
                            databaseManager,
                            sqlConnection
                        ) { isLoggedIn, _ ->
                            if (isLoggedIn == null) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_129))

                                return@login
                            }

                            if (isLoggedIn) {
                                setupManager.finishSetup()

                                handler.invoke(Successful())

                                return@login
                            }

                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_130))
                        }
                    }
                }
            }
        }
    }
}