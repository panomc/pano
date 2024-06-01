package com.panomc.platform.route.api.panel

import com.panomc.platform.PanoPluginDescriptor
import com.panomc.platform.PanoPluginWrapper
import com.panomc.platform.PluginManager
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.platform.util.AddonHashStatus
import com.panomc.platform.util.AddonStatusType
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.arraySchema
import io.vertx.json.schema.common.dsl.Schemas.enumSchema
import org.pf4j.PluginState
import java.io.PrintWriter
import java.io.StringWriter

@Endpoint
class PanelGetPluginsAPI(
    private val databaseManager: DatabaseManager,
    private val pluginManager: PluginManager
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/plugins", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .queryParameter(
                optionalParam(
                    "status", arraySchema().items(enumSchema(*AddonStatusType.entries.map { it.name }.toTypedArray()))
                )
            )
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        val parameters = getParameters(context)

        val statusType = AddonStatusType.valueOf(
            parameters.queryParameter("status")?.jsonArray?.first() as String? ?: AddonStatusType.ALL.name
        )

        val plugins = when (statusType) {
            AddonStatusType.ACTIVE -> pluginManager.plugins.filter { it.pluginState == PluginState.STARTED }
            AddonStatusType.DISABLED -> pluginManager.plugins.filter { it.pluginState != PluginState.STARTED }
            else -> pluginManager.plugins
        }.map { it as PanoPluginWrapper }

        val hashList = plugins.map { it.hash }

        val sqlClient = getSqlClient()

        val addonHashes = databaseManager.addonHashDao.byListOfHash(hashList, sqlClient)

        val result = mutableMapOf(
            "plugins" to plugins.map {
                val panoPluginDescriptor = it.descriptor as PanoPluginDescriptor

                mapOf(
                    "id" to it.pluginId,
                    "author" to panoPluginDescriptor.provider,
                    "description" to panoPluginDescriptor.pluginDescription,
                    "version" to panoPluginDescriptor.version,
                    "status" to it.pluginState,
                    "dependencies" to panoPluginDescriptor.dependencies,
                    "license" to panoPluginDescriptor.license,
                    "error" to if (it.failedException == null) null else getStackTraceAsString(it.failedException),
                    "hash" to it.hash,
                    "verifyStatus" to if (addonHashes[it.hash] == null) AddonHashStatus.UNKNOWN else addonHashes[it.hash]!!.status,
                    "sourceUrl" to panoPluginDescriptor.sourceUrl
                )
            }
        )

        return Successful(result)
    }

    private fun getStackTraceAsString(exception: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        exception.printStackTrace(printWriter)
        return stringWriter.toString()
    }
}