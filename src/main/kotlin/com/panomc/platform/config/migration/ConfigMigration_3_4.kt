package com.panomc.platform.config.migration

import com.panomc.platform.config.ConfigManager
import com.panomc.platform.config.ConfigMigration
import com.panomc.platform.util.KeyGeneratorUtil
import io.jsonwebtoken.io.Encoders

@Suppress("ClassName")
class ConfigMigration_3_4(
    override val FROM_VERSION: Int = 3,
    override val VERSION: Int = 4,
    override val VERSION_INFO: String = "Convert access_token to access-token"
) : ConfigMigration() {
    override fun migrate(configManager: ConfigManager) {
        val key = KeyGeneratorUtil.generateJWTKeys()

        configManager.getConfig().putAll(
            mapOf(
                "jwt-keys" to mapOf(
                    "private" to Encoders.BASE64.encode(key.private.encoded),
                    "public" to Encoders.BASE64.encode(key.public.encoded)
                )
            )
        )
    }
}