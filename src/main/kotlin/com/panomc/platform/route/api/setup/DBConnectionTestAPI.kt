package com.panomc.platform.route.api.setup

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf

class DBConnectionTestAPI : SetupApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/setup/dbConnectionTest")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        var port = 3306
        var host = data.getString("host")

        if (host.contains(":")) {
            val splitHost = host.split(":")

            host = splitHost[0]

            port = splitHost[1].toInt()
        }

        val mySQLClientConfig = jsonObjectOf(
            Pair("host", host),
            Pair("port", port),
            Pair("database", data.getString("dbName")),
            Pair("username", data.getString("username")),
            Pair("password", if (data.getString("password").isNullOrEmpty()) null else data.getString("password"))
        )

        val mySQLClient = MySQLClient.createNonShared(context.vertx(), mySQLClientConfig)

        mySQLClient.getConnection { connection ->
            if (connection.succeeded())
                connection.result().close {
                    mySQLClient.close {
                        handler.invoke(Successful())
                    }
                }
            else
                mySQLClient.close {
                    handler.invoke(Error(ErrorCode.INVALID_DATA))
                }
        }
    }
}