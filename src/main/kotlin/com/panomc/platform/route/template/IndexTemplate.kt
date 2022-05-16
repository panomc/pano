package com.panomc.platform.route.template

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.model.Template
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

@Endpoint
class IndexTemplate() : Template() {
    private val mHotLinks = mapOf<String, String>()

    override val routes = arrayListOf("/*")

    override val order = 999

    override fun getHandler() = Handler<RoutingContext> { context ->
        val response = context.response()
        val normalisedPath = context.normalizedPath()

        if (!mHotLinks[normalisedPath.lowercase()].isNullOrEmpty())
            response.putHeader(
                "location",
                mHotLinks[normalisedPath.lowercase()]
            ).setStatusCode(302).end()
        else response.end("Hello to Pano web platform!")
    }
}