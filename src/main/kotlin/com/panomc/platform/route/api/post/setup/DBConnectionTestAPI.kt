package com.panomc.platform.route.api.post.setup

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Api
import com.panomc.platform.model.RouteType
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class DBConnectionTestAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/setup/dbConnectionTest")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        val response = context.response()
        val data = context.bodyAsJson

        response
            .putHeader("content-type", "application/json; charset=utf-8")

        var port = 3306
        var host = data.getString("host")

        if (host.contains(":")) {
            val splitHost = host.split(":")

            host = splitHost[0]

            port = splitHost[1].toInt()
        }

        val mySQLClientConfig = io.vertx.core.json.JsonObject()
            .put("host", host)
            .put("port", port)
            .put("database", data.getString("dbName"))
            .put("username", data.getString("username"))
            .put("password", data.getString("password"))

        val mySQLClient = MySQLClient.createNonShared(context.vertx(), mySQLClientConfig)

        mySQLClient.getConnection { connection ->
            if (connection.succeeded())
                connection.result().close {
                    mySQLClient.close {
                        response.end(
                            JsonObject(
                                mapOf(
                                    "result" to "ok"
                                )
                            ).toJsonString()
                        )
                    }
                }
            else
                mySQLClient.close {
                    response.end(
                        JsonObject(
                            mapOf(
                                "result" to "error",
                                "error" to ErrorCode.INVALID_DATA
                            )
                        ).toJsonString()
                    )
                }
        }
    }
}