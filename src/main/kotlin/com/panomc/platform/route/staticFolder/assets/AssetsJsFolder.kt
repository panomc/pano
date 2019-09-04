package com.panomc.platform.route.staticFolder.assets

import com.panomc.platform.model.Route
import com.panomc.platform.util.AssetsStaticHandler

class AssetsJsFolder : Route() {
    override val routes = arrayListOf("/assets/js/*")

    override fun getHandler() = AssetsStaticHandler("js").setCachingEnabled(false)
}