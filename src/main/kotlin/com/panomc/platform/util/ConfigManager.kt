package com.panomc.platform.util

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.kotlin.config.getConfigAwait
import kotlinx.coroutines.runBlocking
import java.io.File

class ConfigManager(mLogger: Logger, mVertx: Vertx) {

    val config = com.beust.klaxon.JsonObject()

    private val mConfigFile = File("config.json")

    private val mIsFileConfig = when {
        mConfigFile.exists() -> true
        mConfigFile.createNewFile() -> {
            mConfigFile.writeText(DEFAULT_CONFIG.toJsonString(true))

            true
        }
        else -> false
    }

    companion object {
        private const val CONFIG_VERSION = 1

        val DEFAULT_CONFIG by lazy {
            JsonObject(
                mapOf(
                    "config-version" to CONFIG_VERSION,
                    "development-mode" to true,

                    "website-name" to "",
                    "website-description" to "",

                    "setup" to mapOf(
                        "step" to 0
                    ),

                    "database" to mapOf(
                        "host" to "",
                        "name" to "",
                        "username" to "",
                        "password" to "",
                        "prefix" to "pano_"
                    ),

                    "pano-account" to mapOf(
                        "username" to "",
                        "email" to "",
                        "access_token" to ""
                    ),

                    "current-theme" to "Vanilla"
                )
            )
        }
    }

    init {
        if (mIsFileConfig) {
            val mFileStore = ConfigStoreOptions()
                .setType("file")
                .setConfig(JsonObject().put("path", "config.json"))

            val mOptions = ConfigRetrieverOptions().addStore(mFileStore)

            val mRetriever = ConfigRetriever.create(mVertx, mOptions)

            runBlocking {
                config.clear()

                config.putAll(mRetriever.getConfigAwait().map)
            }

            mRetriever.listen { change ->
                config.clear()

                config.putAll(change.newConfiguration.map)
            }
        } else {
            config.clear()

            config.putAll(DEFAULT_CONFIG.map)
        }
    }

    fun JsonObject.toJsonString(prettyPrint: Boolean = false, canonical: Boolean = false): String {
        val jsonObject = com.beust.klaxon.JsonObject()

        jsonObject.putAll(this.map)

        return jsonObject.toJsonString(prettyPrint, canonical)
    }

    fun saveConfig() {
        mConfigFile.writeText(config.toJsonString(true))
    }
}