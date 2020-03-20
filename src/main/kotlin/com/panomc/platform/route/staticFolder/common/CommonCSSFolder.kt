package com.panomc.platform.route.staticFolder.common

import com.panomc.platform.model.Route
import io.vertx.ext.web.handler.StaticHandler

class CommonCSSFolder : Route() {
    override val routes = arrayListOf("/common/css/*")

    override fun getHandler() = StaticHandler.create("src/main/resources/common/css").setCachingEnabled(true)!!
}