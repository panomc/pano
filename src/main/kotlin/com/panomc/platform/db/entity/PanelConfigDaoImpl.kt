package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PanelConfigDao
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

class PanelConfigDaoImpl(override val tableName: String = "panel_config") : DaoImpl(), PanelConfigDao {
    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int(11) NOT NULL,
              `option` varchar(255) NOT NULL,
              `value` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Panel Config per player table.';
        """
        ) {
            handler.invoke(it)
        }
    }
}