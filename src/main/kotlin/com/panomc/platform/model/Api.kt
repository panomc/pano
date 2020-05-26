package com.panomc.platform.model

import com.beust.klaxon.JsonObject
import io.vertx.ext.web.RoutingContext

abstract class Api : Route() {
    fun getResultHandler(result: Result, context: RoutingContext) {
        val response = context.response()

        if (result is Successful) {
            val responseMap = mutableMapOf<String, Any?>(
                "result" to "ok"
            )

            responseMap.putAll(result.map)

            response.end(
                JsonObject(
                    responseMap
                ).toJsonString()
            )
        } else if (result is Error)
            response.end(
                JsonObject(
                    mapOf(
                        "result" to "error",
                        "error" to result.errorCode
                    )
                ).toJsonString()
            )
    }
}