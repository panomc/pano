package com.panomc.platform.model

import com.beust.klaxon.JsonObject
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

abstract class Api : Route() {
    fun getResultHandler(result: Result, context: RoutingContext) {
        val response = context.response()

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

    fun getHandler(context: RoutingContext) {
        val response = context.response()

        response
            .putHeader("content-type", "application/json; charset=utf-8")

        getHandler(context) { result ->
            getResultHandler(result, context)
        }
    }

    override fun getHandler() = Handler<RoutingContext> { context ->
        getHandler(context)
    }

    abstract fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit)
}