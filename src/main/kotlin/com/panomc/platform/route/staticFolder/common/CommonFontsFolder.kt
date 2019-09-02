package com.panomc.platform.route.staticFolder.common

import com.panomc.platform.model.Route
import io.vertx.ext.web.handler.StaticHandler

class CommonFontsFolder : Route() {
    override val routes = arrayListOf("/common/fonts/*")

    override fun getHandler() = StaticHandler.create("view/common/fonts").setCachingEnabled(true)!!
}