package com.panomc.platform.route.staticFolder.src

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Route
import com.panomc.platform.util.Auth
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.core.impl.StringEscapeUtils
import io.vertx.ext.web.RoutingContext
import java.io.File
import javax.inject.Inject

class SrcFolderRoute : Route() {
    override val routes = arrayListOf("/src/*", "/panel/src/*")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var setupManager: SetupManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        val normalisedPath = context.normalisedPath()

        if (normalisedPath.startsWith("/panel/")) {
            val auth = Auth()

            auth.isAdmin(context) { isAdmin ->
                val srcFolderRoot = if (setupManager.isSetupDone())
                    if (isAdmin)
                        "view/ui/site/"
                    else
                        "view/ui/site/themes/" + configManager.config.string("current-theme") + "/"
                else
                    "view/ui/setup/"

                handle(srcFolderRoot, context)
            }
        } else {
            val srcFolderRoot = if (setupManager.isSetupDone())
                "view/ui/site/themes/" + configManager.config.string("current-theme") + "/"
            else
                "view/ui/setup/"

            handle(srcFolderRoot, context)
        }
    }

    private fun handle(path: String, context: RoutingContext) {
        val response = context.response()
        val normalisedPath = context.normalisedPath()

        val componentFile = File("$path$normalisedPath/index.js")
        val componentUIFile = File("$path$normalisedPath/index.html")

        if (componentFile.exists() && componentUIFile.exists())
            response.end(
                componentFile.readText().replace(
                    "PANO.UI",
                    "\"${StringEscapeUtils.escapeJavaScript(componentUIFile.readText())}\""
                )
            )
        else
            context.reroute("/")

    }
}