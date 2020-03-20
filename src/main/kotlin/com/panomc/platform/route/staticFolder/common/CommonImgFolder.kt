package com.panomc.platform.route.staticFolder.common

import com.panomc.platform.model.Route
import io.vertx.ext.web.handler.StaticHandler

class CommonImgFolder : Route() {
    override val routes = arrayListOf("/common/img/*")

    override fun getHandler() = StaticHandler.create("src/main/resources/common/img").setCachingEnabled(true)!!
}