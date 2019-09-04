package com.panomc.platform.util

class SetupManager(private val mConfigManager: ConfigManager) {

    fun isSetupDone() = ((mConfigManager.config["setup"] as Map<*, *>)["step"] as Int) == 4
}