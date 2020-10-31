package com.panomc.platform.route.api.setup

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.platform.util.SetupManager
import com.panomc.platform.util.auth.LoginSystem
import com.panomc.platform.util.auth.RegisterSystem
import io.vertx.ext.web.RoutingContext
import java.net.ConnectException
import javax.inject.Inject

class FinishAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/setup/finish")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return
        }

        if (setupManager.getStep() == 3) {
            val data = context.bodyAsJson
            val remoteIP = context.request().remoteAddress().host()

            databaseManager.initDatabase {
                if (it.failed() && it.cause() is ConnectException)
                    handler.invoke(Error(ErrorCode.FINISH_API_CANT_CONNECT_DATABASE_PLEASE_CHECK_YOUR_INFO))
                else if (it.failed())
                    handler.invoke(Error(ErrorCode.FINISH_API_SOMETHING_WENT_WRONG_IN_DATABASE))
                else {
                    val registerSystem = RegisterSystem()

                    registerSystem.register(data, remoteIP, true) { registerResult ->
                        if (registerResult is Successful) {
                            val loginSystem = LoginSystem()

                            loginSystem.login(data, remoteIP) { loginResult ->
                                if (loginResult is Successful)
                                    loginSystem.createSession(data.getString("username"), context) { result ->
                                        handler.invoke(result)
                                    }
                                else if (loginResult is Error)
                                    handler.invoke(loginResult)
                            }
                        } else if (registerResult is Error)
                            handler.invoke(registerResult)
                    }
                }
            }
        } else
            handler.invoke(Successful(setupManager.getCurrentStepData()))
    }
}