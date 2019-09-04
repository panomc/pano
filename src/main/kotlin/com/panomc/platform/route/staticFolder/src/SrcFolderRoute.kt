package com.panomc.platform.route.staticFolder.src

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Route
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.core.impl.StringEscapeUtils
import io.vertx.ext.web.RoutingContext
import java.io.File
import javax.inject.Inject

class SrcFolderRoute : Route() {
    override val routes = arrayListOf("/src/*")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var setupManager: SetupManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        val response = context.response()
        val normalisedPath = context.normalisedPath()

        val srcFolderRoot = if (setupManager.isSetupDone())
            "view/ui/site/themes/" + configManager.config.string("current-theme") + "/"
        else
            "view/ui/setup/"

        val componentFile = File("$srcFolderRoot$normalisedPath/index.js")
        val componentUIFile = File("$srcFolderRoot$normalisedPath/index.html")

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