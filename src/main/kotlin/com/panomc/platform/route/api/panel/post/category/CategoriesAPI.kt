package com.panomc.platform.route.api.panel.post.category

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.PanelApi
import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import com.panomc.platform.model.Successful
import com.panomc.platform.util.AuthProvider
import com.panomc.platform.util.SetupManager
import io.vertx.ext.web.RoutingContext

@Endpoint
class CategoriesAPI(
    private val databaseManager: DatabaseManager,
    setupManager: SetupManager,
    authProvider: AuthProvider
) : PanelApi(setupManager, authProvider) {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/post/category/categories")

    override suspend fun handler(context: RoutingContext): Result {
        val sqlConnection = createConnection(databaseManager, context)

        val count = databaseManager.postCategoryDao.getCount(sqlConnection)

        val categories = databaseManager.postCategoryDao.getAll(sqlConnection)

        val result = mutableMapOf<String, Any?>(
            "categories" to categories,
            "categoryCount" to count
        )

        return Successful(result)
    }
}