/*
 * Copyright 2019 William Bruschi - williambruschi.net
 *
 * This file is part of runsql.
 *
 * runsql is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * runsql is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with runsql.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package runsql.impl;

import runsql.ConnectionPool;
import runsql.ConnectionProperties;
import runsql.util.TransactionMode;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class RunSqlConnectionPool implements ConnectionPool {
    private final BasicDataSource basicDataSource;
    private final ConnectionProperties connectionProperties;

    public RunSqlConnectionPool(final ConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
        basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(connectionProperties.getDriver());
        basicDataSource.setUrl(connectionProperties.getUrl());
        basicDataSource.setUsername(connectionProperties.getUser());
        basicDataSource.setPassword(connectionProperties.getPassword());
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = basicDataSource.getConnection();
        TransactionMode transactionMode = connectionProperties.getTransactionMode();
        if (transactionMode != null) {
            switch (transactionMode) {
                case AUTO:
                    connection.setAutoCommit(true);
                    break;
                case ROLLBACK:
                    connection.setAutoCommit(false);
                    break;
                case N:
                    connection.setAutoCommit(false);
                    break;
            }
        }
        return connection;
    }

    @Override
    public void close() throws SQLException {
        basicDataSource.close();
    }
}
