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

import runsql.ConnectionPool;
import runsql.Processor;
import runsql.SqlStatementExecutionHook;
import runsql.impl.RunSqlConnectionPool;
import runsql.impl.RunSqlConnectionProperties;
import runsql.util.SqlCode;
import runsql.util.TransactionMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Uses multiple threads to perform the inserts into the target database. See {@link
 * RunSqlProcessorImport} for single threaded usages.
 * <p>
 * Whether or not this class has any performance benefits over a single threaded depends on many
 * factors. As always it is best to test before deciding which approach to use.
 * <p>
 * This job uses its own queue and {@link JobConsumer} to perform multithreading.
 */
public class RunSqlProcessorImportConcurrent implements Processor {
    private static final List<Object> POISON_PILL = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger();
    private final String insertTableName;
    private final String[] insertColumnNames;
    private final String importDriver;
    private final String importUrl;
    private final String importUser;
    private final String importPassword;
    private final int numberOfJobs;
    private final SqlStatementExecutionHook sqlStatementExecutionHook;
    private final TransactionMode transactionMode;
    private final BlockingQueue<List<Object>> jobQueue = new LinkedBlockingQueue<>();
    private ConnectionPool connectionPool;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private int columnCount;

    public RunSqlProcessorImportConcurrent(final Builder builder) {
        importDriver = builder.importDriver;
        importUrl = builder.importUrl;
        importUser = builder.importUser;
        importPassword = builder.importPassword;
        insertTableName = builder.insertTableName;
        insertColumnNames = builder.insertColumnNames;
        sqlStatementExecutionHook = builder.sqlStatementExecutionHook;
        transactionMode = builder.transactionMode;
        numberOfJobs = builder.numberOfJobs;
    }

    @Override
    public void process(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        columnCount = resultSetMetaData.getColumnCount();
        createConnectionPool();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfJobs);
        List<Future<?>> futures = new ArrayList<>(numberOfJobs);
        IntStream.range(0, numberOfJobs)
                 .forEach($ -> futures.add(executorService.submit(createJobConsumer())));
        while (resultSet.next()) {
            jobQueue.add(createRowListObject(resultSet));
        }
        try {
            IntStream.range(0, numberOfJobs).forEach($ -> jobQueue.add(POISON_PILL));
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (final ExecutionException e) {
                    LOGGER.error("Error occurred during execution of import job.");
                    throw new RuntimeException(e);
                }
            }
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (final InterruptedException e) {
            LOGGER.error("Error - Import job thread interrupted.");
            throw new RuntimeException(e);
        }
    }

    void createConnectionPool() {
        RunSqlConnectionProperties.Builder builder = new RunSqlConnectionProperties.Builder();
        builder.setUrl(importUrl);
        builder.setUser(importUser);
        builder.setPassword(importPassword);
        builder.setDriver(importDriver);
        builder.setTransactionMode(transactionMode);
        connectionPool = new RunSqlConnectionPool(builder.build());
    }

    private JobConsumer createJobConsumer() {
        return new JobConsumer(sqlStatementExecutionHook.createFreshCopy());
    }

    List<Object> createRowListObject(final ResultSet resultSet) {
        List<Object> result = new ArrayList<>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            try {
                result.add(resultSet.getObject(columnIndex));
            } catch (final SQLException e) {
                LOGGER.error("Error occurred when parsing the source data.");
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static class Builder extends RunSqlProcessorImport.Builder {
        private int numberOfJobs;

        public Builder setNumberOfJobs(final int numberOfJobs) {
            this.numberOfJobs = numberOfJobs;
            return this;
        }

        @Override
        public Processor build() {
            return new RunSqlProcessorImportConcurrent(this);
        }
    }

    private class JobConsumer implements Runnable {
        private final SqlStatementExecutionHook sqlStatementExecutionHook;
        private PreparedStatement preparedStatement;

        private JobConsumer(final SqlStatementExecutionHook sqlStatementExecutionHook) {
            this.sqlStatementExecutionHook = sqlStatementExecutionHook;
        }

        @Override
        public void run() {
            try (Connection connection = connectionPool.getConnection()) {
                int numberOfColumnsToInsert =
                        insertColumnNames == null ? columnCount : insertColumnNames.length;
                String insertSql = SqlCode.createInsertSql(insertTableName, insertColumnNames,
                                                           numberOfColumnsToInsert);
                preparedStatement = connection.prepareStatement(insertSql);
                sqlStatementExecutionHook.beforeAllStatements(connection, preparedStatement);
                boolean loop = true;
                while (loop) {
                    List<Object> rowListObject = jobQueue.take();
                    if (rowListObject == POISON_PILL) {
                        loop = false;
                    } else {
                        processRowListObject(connection, rowListObject);
                    }
                }
                sqlStatementExecutionHook.afterAllStatements(connection, preparedStatement);
            } catch (final Exception e) {
                LOGGER.error("Error occurred during target database load.");
                throw new RuntimeException(e);
            }
        }

        void processRowListObject(final Connection connection, final List<Object> rowListObject) {
            try {
                sqlStatementExecutionHook.beforeStatement(connection, preparedStatement);
                for (int parameterIndex = 1; parameterIndex <= columnCount; parameterIndex++) {
                    preparedStatement
                            .setObject(parameterIndex, rowListObject.get(parameterIndex - 1));
                }
                preparedStatement.addBatch();
                sqlStatementExecutionHook.afterStatement(connection, preparedStatement);
            } catch (final SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
