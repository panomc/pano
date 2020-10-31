package com.panomc.platform.route.api.panel.platformAuth

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.PanelApi
import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import com.panomc.platform.model.Successful
import com.panomc.platform.util.PlatformCodeManager
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class RefreshKeyAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/platformAuth/refreshKey")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var platformCodeManager: PlatformCodeManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        handler.invoke(
            Successful(
                mapOf(
                    "key" to platformCodeManager.getPlatformKey(),
                    "timeStarted" to platformCodeManager.getTimeStarted()
                )
            )
        )
    }
}