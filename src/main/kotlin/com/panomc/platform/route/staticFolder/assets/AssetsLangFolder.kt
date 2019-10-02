package com.panomc.platform.route.staticFolder.assets

import com.panomc.platform.model.Route
import com.panomc.platform.util.AssetsStaticHandler

class AssetsLangFolder : Route() {
    override val routes = arrayListOf("/assets/lang/*", "/panel/assets/lang/*")

    override fun getHandler() = AssetsStaticHandler("lang").setCachingEnabled(false)
}