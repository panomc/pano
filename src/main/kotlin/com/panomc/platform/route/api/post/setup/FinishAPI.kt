package com.panomc.platform.route.api.post.setup

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Api
import com.panomc.platform.model.Error
import com.panomc.platform.model.RouteType
import com.panomc.platform.model.Successful
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.DatabaseManager
import com.panomc.platform.util.SetupManager
import com.panomc.platform.util.auth.LoginSystem
import com.panomc.platform.util.auth.RegisterSystem
import io.vertx.core.Handler
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
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        val response = context.response()

        if (setupManager.getStep() == 3) {
            val data = context.bodyAsJson
            val remoteIP = context.request().remoteAddress().host()

            response
                .putHeader("content-type", "application/json; charset=utf-8")

            databaseManager.initDatabaseTables {
                if (it.failed() && it.cause() is ConnectException)
                    response.end(
                        JsonObject(
                            mapOf(
                                "result" to "error",
                                "error" to ErrorCode.FINISH_API_CANT_CONNECT_DATABASE_PLEASE_CHECK_YOUR_INFO
                            )
                        ).toJsonString()
                    )
                else if (it.failed())
                    response.end(
                        JsonObject(
                            mapOf(
                                "result" to "error",
                                "error" to ErrorCode.FINISH_API_SOMETHING_WENT_WRONG_IN_DATABASE
                            )
                        ).toJsonString()
                    )
                else {
                    val registerSystem = RegisterSystem()

                    registerSystem.register(data, remoteIP, true) {
                        if (it is Successful) {
                            val loginSystem = LoginSystem()

                            loginSystem.login(data, remoteIP) {
                                if (it is Successful)
                                    loginSystem.createSession(data.getString("username"), context) {
                                        if (it is Successful) {
                                            setupManager.finishSetup()

                                            response.end(
                                                JsonObject(
                                                    mapOf(
                                                        "result" to "ok"
                                                    )
                                                ).toJsonString()
                                            )
                                        } else if (it is Error)
                                            response.end(
                                                JsonObject(
                                                    mapOf(
                                                        "result" to "error",
                                                        "error" to it.errorCode
                                                    )
                                                ).toJsonString()
                                            )
                                    }
                                else if (it is Error)
                                    response.end(
                                        JsonObject(
                                            mapOf(
                                                "result" to "error",
                                                "error" to it.errorCode
                                            )
                                        ).toJsonString()
                                    )
                            }
                        } else if (it is Error)
                            response.end(
                                JsonObject(
                                    mapOf(
                                        "result" to "error",
                                        "error" to it.errorCode
                                    )
                                ).toJsonString()
                            )
                    }
                }
            }
        } else
            response.end(setupManager.getCurrentStepData().toJsonString())
    }
}