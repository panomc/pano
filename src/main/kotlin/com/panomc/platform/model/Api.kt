package com.panomc.platform.model

import com.beust.klaxon.JsonObject
import com.panomc.platform.Main
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.util.AuthProvider
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

abstract class Api : Route() {
    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var authProvider: AuthProvider

    init {
        @Suppress("LeakingThis")
        Main.getComponent().inject(this)
    }

    fun sendResult(result: Result, context: RoutingContext) {
        val response = context.response()

        response
            .putHeader("content-type", "application/json; charset=utf-8")

        when (result) {
            is Successful -> {
                val responseMap = mutableMapOf<String, Any?>(
                    "result" to "ok"
                )

                responseMap.putAll(result.map)

                response.end(
                    JsonObject(
                        responseMap
                    ).toJsonString()
                )
            }
            is Error -> response.end(
                JsonObject(
                    mapOf(
                        "result" to "error",
                        "error" to result.errorCode
                    )
                ).toJsonString()
            )
            is Errors -> response.end(
                JsonObject(
                    mapOf(
                        "result" to "error",
                        "error" to result.errors
                    )
                ).toJsonString()
            )
        }
    }

    override fun getHandler() = Handler<RoutingContext> { context ->
        handler(context)
    }

    fun handler(context: RoutingContext) {
        handler(context) { result ->
            sendResult(result, context)
        }
    }

    abstract fun handler(context: RoutingContext, handler: (result: Result) -> Unit)
}