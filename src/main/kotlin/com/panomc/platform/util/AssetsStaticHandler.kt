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

    override fun handle(context: RoutingContext) {
        if (context.normalisedPath().startsWith("/panel/")) {
            val auth = Auth()

            auth.isAdmin(context) { isAdmin ->
                val assetsFolderRoot = if (setupManager.isSetupDone())
                    if (isAdmin)
                        "view/ui/site/panel/assets/"
                    else
                        "view/ui/site/themes/" + configManager.config.string("current-theme") + "/assets/"
                else
                    "view/ui/setup/assets/"

                handle(assetsFolderRoot, context)
            }
        } else {
            val assetsFolderRoot = if (setupManager.isSetupDone())
                "view/ui/site/themes/" + configManager.config.string("current-theme") + "/assets/"
            else
                "view/ui/setup/assets/"

            handle(assetsFolderRoot, context)
        }
    }

    private fun handle(path: String, context: RoutingContext) {
        setWebRoot(path + mRoot)

        super.handle(context)
    }
}