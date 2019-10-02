package com.panomc.platform.route.staticFolder.assets

import com.panomc.platform.model.Route
import com.panomc.platform.util.AssetsStaticHandler

class AssetsImgFolder : Route() {
    override val routes = arrayListOf("/assets/img/*", "/panel/assets/img/*")

    override fun getHandler() = AssetsStaticHandler("img").setCachingEnabled(false)
}