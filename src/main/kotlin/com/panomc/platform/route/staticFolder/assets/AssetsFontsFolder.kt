package com.panomc.platform.route.staticFolder.assets

import com.panomc.platform.model.Route
import com.panomc.platform.util.AssetsStaticHandler

class AssetsFontsFolder : Route() {
    override val routes = arrayListOf("/assets/fonts/*")

    override fun getHandler() = AssetsStaticHandler("fonts").setCachingEnabled(false)
}