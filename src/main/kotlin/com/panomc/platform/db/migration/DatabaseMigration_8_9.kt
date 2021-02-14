package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

@Suppress("ClassName")
class DatabaseMigration_8_9 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 8
    override val SCHEME_VERSION = 9
    override val SCHEME_VERSION_INFO = "Delete platformCode system property."

    override val handlers: List<(sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit> =
        listOf(
            deletePlatformCode()
        )

    private fun deletePlatformCode(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .preparedQuery("DELETE FROM ${getTablePrefix()}system_property WHERE `option` = ?")
                .execute(Tuple.of("platformCode"))
                {
                    handler.invoke(it)
                }
        }
}