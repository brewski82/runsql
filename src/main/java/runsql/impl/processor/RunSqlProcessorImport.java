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

package runsql.impl.processor;

import runsql.Processor;
import runsql.SqlStatementExecutionHook;
import runsql.util.SqlCode;
import runsql.util.TransactionMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handles the importing of data from database source to the database target. The source database
 * connection is handled by the {@link runsql.impl.RunSqlImpl} class. This class establishes the
 * connection to the target database, loops through the records from the source database and inserts
 * them into the target database.
 * <p>
 * Also see {@link RunSqlProcessorImportConcurrent} which uses multiple threads to insert data into
 * the target database.
 */
public class RunSqlProcessorImport extends RunSqlProcessorTemplate {
    private final String insertTableName;
    private final String[] insertColumnNames;
    private final String importDriver;
    private final String importUrl;
    private final String importUser;
    private final String importPassword;
    private final SqlStatementExecutionHook sqlStatementExecutionHook;
    private final TransactionMode transactionMode;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private int numberOfColumnsToInsert;

    public RunSqlProcessorImport(final Builder builder) {
        insertTableName = builder.insertTableName;
        insertColumnNames = builder.insertColumnNames;
        importDriver = builder.importDriver;
        importUrl = builder.importUrl;
        importUser = builder.importUser;
        importPassword = builder.importPassword;
        sqlStatementExecutionHook = builder.sqlStatementExecutionHook;
        transactionMode = builder.transactionMode;
    }

    @Override
    protected void processPreRows() throws SQLException, ClassNotFoundException {
        Class.forName(importDriver);
        connection = DriverManager.getConnection(importUrl, importUser, importPassword);
        connection.setAutoCommit(transactionMode == TransactionMode.AUTO);
        numberOfColumnsToInsert =
                insertColumnNames == null ? columnCount : insertColumnNames.length;
        String insertSql = SqlCode.createInsertSql(insertTableName, insertColumnNames,
                                                   numberOfColumnsToInsert);
        preparedStatement = connection.prepareStatement(insertSql);
    }

    @Override
    protected void processPostRows() throws SQLException {
        sqlStatementExecutionHook.afterAllStatements(connection, preparedStatement);
        connection.close();
    }

    @Override
    protected void processPostRow() throws SQLException {
        preparedStatement.addBatch();
        sqlStatementExecutionHook.afterStatement(connection, preparedStatement);
    }

    @Override
    protected void processColumn() throws SQLException {
        if (currentColumnNumber <= numberOfColumnsToInsert) {
            preparedStatement
                    .setObject(currentColumnNumber, resultSet.getObject(currentColumnNumber));
        }
    }

    public static class Builder {
        protected String insertTableName;
        protected String[] insertColumnNames;
        protected String importDriver;
        protected String importUrl;
        protected String importUser;
        protected String importPassword;
        protected SqlStatementExecutionHook sqlStatementExecutionHook;
        protected TransactionMode transactionMode;

        public Builder setInsertTableName(final String insertTableName) {
            this.insertTableName = insertTableName;
            return this;
        }

        public Builder setInsertColumnNames(final String[] insertColumnNames) {
            this.insertColumnNames = insertColumnNames;
            return this;
        }

        public Builder setImportDriver(final String importDriver) {
            this.importDriver = importDriver;
            return this;
        }

        public Builder setImportUrl(final String importUrl) {
            this.importUrl = importUrl;
            return this;
        }

        public Builder setImportUser(final String importUser) {
            this.importUser = importUser;
            return this;
        }

        public Builder setImportPassword(final String importPassword) {
            this.importPassword = importPassword;
            return this;
        }

        public Builder setSqlStatementExecutionHook(
                final SqlStatementExecutionHook sqlStatementExecutionHook) {
            this.sqlStatementExecutionHook = sqlStatementExecutionHook;
            return this;
        }

        public Builder setTransactionMode(final TransactionMode transactionMode) {
            this.transactionMode = transactionMode;
            return this;
        }

        public Processor build() {
            return new RunSqlProcessorImport(this);
        }
    }
}
