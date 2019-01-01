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

import runsql.ConnectionProperties;
import runsql.SqlStatementExecutionHook;
import runsql.util.TransactionMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class RunSqlStatementExecutionHook implements SqlStatementExecutionHook {
    private static final Logger LOGGER = LogManager.getLogger();
    final long batchSize;
    final long transactionSize;
    final boolean isBatchMode;
    final TransactionMode transactionMode;
    long statementNumberSinceLastTransaction;
    long statementNumberSinceLastBatch;

    public RunSqlStatementExecutionHook(final ConnectionProperties connectionProperties) {
        isBatchMode = connectionProperties.isBatchMode();
        batchSize = connectionProperties.getBatchSize() == null ? 0 :
                connectionProperties.getBatchSize().longValue();
        transactionMode = connectionProperties.getTransactionMode();
        transactionSize = connectionProperties.getTransactionSize() == null ? 0 :
                connectionProperties.getTransactionSize().longValue();
    }

    public RunSqlStatementExecutionHook(final boolean isBatchMode, final long batchSize,
                                        final TransactionMode transactionMode,
                                        final long transactionSize) {
        this.isBatchMode = isBatchMode;
        this.batchSize = batchSize;
        this.transactionMode = transactionMode;
        this.transactionSize = transactionSize;
    }

    /**
     * Tracks the number of statements ran and executes batches if necessary. Also conditionally
     * commits or rollbacks transactions.
     *
     * @param connection Database connection.
     * @param statement  The database statement.
     * @throws SQLException JDBC sql execption
     */
    @Override
    public void afterStatement(final Connection connection,
                               final Statement statement) throws SQLException {
        LOGGER.trace("afterStatement");
        statementNumberSinceLastBatch++;
        LOGGER.trace("since last batch: " + statementNumberSinceLastBatch);
        statementNumberSinceLastTransaction++;
        if (isBatchMode && statementNumberSinceLastBatch >= batchSize) {
            LOGGER.trace("Executing batch.");
            statement.executeBatch();
            statement.clearBatch();
            statementNumberSinceLastBatch = 0;
            if (transactionMode == TransactionMode.ROLLBACK) {
                LOGGER.trace("Rollback.");
                connection.rollback();
            }
        }
        if (transactionMode == TransactionMode.N && statementNumberSinceLastTransaction >= transactionSize) {
            LOGGER.trace("Commit.");
            connection.commit();
            statementNumberSinceLastTransaction = 0;
        }
        if (!isBatchMode && transactionMode == TransactionMode.ROLLBACK) {
            LOGGER.trace("Rollback.");
            connection.rollback();
        }
    }

    /**
     * Checks if any remaining batches need executing and commits or rolls back any open
     * transactions.
     *
     * @param connection Database connection.
     * @param statement  The database statement.
     * @throws SQLException JDBC sql exception
     */
    @Override
    public void afterAllStatements(final Connection connection,
                                   final Statement statement) throws SQLException {
        LOGGER.trace("afterAllStatements.");
        LOGGER.trace("since last batch: " + statementNumberSinceLastBatch);
        if (isBatchMode && statementNumberSinceLastBatch > 0) {
            LOGGER.trace("Execute batch.");
            statement.executeBatch();
            if (transactionMode == TransactionMode.ROLLBACK) {
                LOGGER.trace("Rollback.");
                connection.rollback();
            }
        }
        if (transactionMode == TransactionMode.N && (statementNumberSinceLastTransaction > 0 || statementNumberSinceLastBatch > 0)) {
            LOGGER.trace("Commit.");
            connection.commit();
        }
    }

    @Override
    public void beforeStatement(final Connection connection, final Statement statement) {
    }

    @Override
    public void beforeAllStatements(final Connection connection, final Statement statement) {
    }

    @Override
    public SqlStatementExecutionHook createFreshCopy() {
        LOGGER.trace("Create fresh copy.");
        return new RunSqlStatementExecutionHook(isBatchMode, batchSize, transactionMode,
                                                transactionSize);
    }
}
