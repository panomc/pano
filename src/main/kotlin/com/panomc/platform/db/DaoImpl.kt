package com.panomc.platform.db

import com.panomc.platform.Main
import javax.inject.Inject

abstract class DaoImpl {
    @Inject
    lateinit var databaseManager: DatabaseManager

    abstract val tableName: String

    init {
        Main.getComponent().inject(this)
    }
}