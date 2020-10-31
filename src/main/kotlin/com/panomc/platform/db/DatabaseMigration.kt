package com.panomc.platform.db

import com.panomc.platform.Main
import com.panomc.platform.db.model.SchemeVersion
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection
import javax.inject.Inject

abstract class DatabaseMigration {
    @Inject
    lateinit var databaseManager: DatabaseManager

    init {
        Main.getComponent().inject(this)
    }

    abstract val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection>


    abstract val FROM_SCHEME_VERSION: Int
    abstract val SCHEME_VERSION: Int
    abstract val SCHEME_VERSION_INFO: String

    fun isMigratable(version: Int) = version == FROM_SCHEME_VERSION

    fun migrate(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit = { handler ->
        var currentIndex = 0

        fun invoke() {
            val localHandler: (AsyncResult<*>) -> Unit = {
                when {
                    it.failed() -> handler.invoke(it)
                    currentIndex == handlers.lastIndex -> handler.invoke(it)
                    else -> {
                        currentIndex++

                        invoke()
                    }
                }
            }

            if (currentIndex <= handlers.lastIndex)
                handlers[currentIndex].invoke(sqlConnection, localHandler)
        }

        invoke()
    }

    fun updateSchemeVersion(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        databaseManager.getDatabase().schemeVersionDao.add(
            sqlConnection,
            SchemeVersion(SCHEME_VERSION.toString(), SCHEME_VERSION_INFO)
        ) { _, asyncResult ->
            handler.invoke(asyncResult)
        }
    }
}
