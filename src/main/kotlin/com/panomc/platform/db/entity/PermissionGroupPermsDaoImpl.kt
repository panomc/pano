package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PermissionGroupPermsDao
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

class PermissionGroupPermsDaoImpl(override val tableName: String = "permission_group_perms") : DaoImpl(),
    PermissionGroupPermsDao {
    override fun init(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .query(
                    """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `permission_id` int NOT NULL,
                              `permission_group_id` int NOT NULL,
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Permission Group Permission Table';
                        """
                )
                .execute {
                    handler.invoke(it)
                }
        }
}