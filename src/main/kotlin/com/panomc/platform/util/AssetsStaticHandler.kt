package com.panomc.platform.util

import com.panomc.platform.Main.Companion.getComponent
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.impl.StaticHandlerImpl
import javax.inject.Inject

class AssetsStaticHandler(private val mRoot: String) : StaticHandlerImpl(mRoot, null) {

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var configManager: ConfigManager

    init {
        getComponent().inject(this)
    }

    override fun handle(context: RoutingContext?) {
        val assetsFolderRoot = if (setupManager.isSetupDone())
            "view/ui/site/themes/" + configManager.config.string("current-theme") + "/assets/"
        else
            "view/ui/setup/assets/"

        setWebRoot(assetsFolderRoot + mRoot)

        super.handle(context)
    }
}