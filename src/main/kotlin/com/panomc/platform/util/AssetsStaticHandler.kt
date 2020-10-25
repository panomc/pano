package com.panomc.platform.util

import com.panomc.platform.Main.Companion.getComponent
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.impl.StaticHandlerImpl
import javax.inject.Inject

class AssetsStaticHandler(private val mRoot: String) : StaticHandlerImpl(mRoot) {

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
                handle(context, isAdmin)
            }
        } else
            handle(context, false)
    }

    private fun handle(context: RoutingContext, isAdmin: Boolean) {
        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER") var assetsFolderRoot = if (setupManager.isSetupDone())
            "src/main/resources/themes/" + configManager.getConfig().string("current-theme") + "/assets/"
        else
            "src/main/resources/setup/assets/"

        val normalisedPath = context.normalisedPath()

        if (normalisedPath.startsWith("/panel/") && isAdmin)
            assetsFolderRoot = "src/main/resources/panel/assets/"
        else if (normalisedPath.startsWith("/panel/")) {
            context.reroute("/error-404")

            return
        }

        setWebRoot(assetsFolderRoot + mRoot)

        super.handle(context)
    }
}