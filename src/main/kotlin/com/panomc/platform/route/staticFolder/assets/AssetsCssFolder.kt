package com.panomc.platform.route.staticFolder.assets

import com.panomc.platform.model.Route
import com.panomc.platform.util.AssetsStaticHandler

class AssetsCssFolder : Route() {
    override val routes = arrayListOf("/assets/css/*", "/panel/assets/css/*")

    override fun getHandler() = AssetsStaticHandler("css").setCachingEnabled(false)
}