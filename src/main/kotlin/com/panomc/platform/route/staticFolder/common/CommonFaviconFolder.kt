package com.panomc.platform.route.staticFolder.common

import com.panomc.platform.model.Route
import io.vertx.ext.web.handler.StaticHandler

class CommonFaviconFolder : Route() {
    override val routes = arrayListOf("/common/favicon/*")

    override fun getHandler() = StaticHandler.create("src/main/resources/common/favicon").setCachingEnabled(true)!!
}