package com.panomc.platform.util

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.Error
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.json.JsonArray
import java.util.*
import javax.inject.Inject

class PlatformCodeGenerator {
    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    init {
        getComponent().inject(this)
    }

    private fun generatePlatformCode() = Random().nextInt(900000) + 100000

    fun createPlatformCode(connection: Connection, handler: (result: Result) -> Unit) {
        val sqlConnection = connection.getSQLConnection()
        val tablePrefix = (configManager.config["database"] as Map<*, *>)["prefix"].toString()
        val platformCode = generatePlatformCode()

        sqlConnection.queryWithParams(
            """
                    SELECT COUNT(value) FROM ${tablePrefix}system_property where option = ?
                """,
            JsonArray().add("platformCode")
        ) {
            when {
                it.failed() -> handler.invoke(Error(ErrorCode.PLATFORM_CODE_GENERATOR_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_9))
                it.result().results[0].getInteger(0) == 0 -> sqlConnection.updateWithParams(
                    """
                                        INSERT INTO ${tablePrefix}system_property (option, value) VALUES (?, ?)
                                    """.trimIndent(),
                    JsonArray().add("platformCode").add(platformCode.toString())
                ) {
                    if (it.succeeded())
                        handler.invoke(Successful(mapOf("platformCode" to platformCode)))
                    else
                        handler.invoke(Error(ErrorCode.PLATFORM_CODE_GENERATOR_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_10))
                }
                else -> sqlConnection.updateWithParams(
                    """
                                        UPDATE ${tablePrefix}system_property SET value = ? WHERE option = ?
                                    """.trimIndent(),
                    JsonArray().add("platformCode").add(platformCode.toString())
                ) {
                    if (it.succeeded())
                        handler.invoke(Successful(mapOf("platformCode" to platformCode)))
                    else
                        handler.invoke(Error(ErrorCode.PLATFORM_CODE_GENERATOR_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_11))
                }
            }
        }
    }
}