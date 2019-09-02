package com.panomc.platform.route.staticFolder.common

import com.panomc.platform.model.Route
import io.vertx.ext.web.handler.StaticHandler

class CommonJsFolder : Route() {
    override val routes = arrayListOf("/common/js/*")

    override fun getHandler() = StaticHandler.create("view/common/js").setCachingEnabled(true)!!
}