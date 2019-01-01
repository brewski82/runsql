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
import runsql.Job;
import runsql.JobConsumer;
import runsql.JobQueue;
import runsql.Processor;
import runsql.SqlStatementExecutionHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RunSqlJobConsumer implements JobConsumer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final JobQueue jobQueue;
    private final ConnectionPool connectionPool;
    private final ConnectionProperties connectionProperties;
    private final SqlStatementExecutionHook sqlStatementExecutionHook;
    private final PrintStream echoSqlPrintStream;

    public RunSqlJobConsumer(final JobQueue jobQueue, final ConnectionPool connectionPool,
                             final ConnectionProperties connectionProperties,
                             final SqlStatementExecutionHook sqlStatementExecutionHook,
                             final PrintStream echoSqlPrintStream) {
        this.jobQueue = jobQueue;
        this.connectionPool = connectionPool;
        this.connectionProperties = connectionProperties;
        this.sqlStatementExecutionHook = sqlStatementExecutionHook;
        this.echoSqlPrintStream = echoSqlPrintStream;
    }

    @Override
    public void run() {
        consumeJobs();
    }

    /**
     * Each RunSqlConsumer will established its own, single database connection when executing
     * jobs.
     */
    @Override
    public void consumeJobs() {
        try (Connection connection = connectionPool.getConnection()) {
            try (Statement statement = connection
                    .createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                initStatement(connection, statement);
                boolean loop = true;
                while (loop) {
                    Job job = jobQueue.takeJob();
                    if (job == RunSqlJobProducer.POISON_PILL) {
                        loop = false;
                    } else {
                        processJob(job, connection, statement);
                    }
                }
                sqlStatementExecutionHook.afterAllStatements(connection, statement);
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initStatement(final Connection connection,
                               final Statement statement) throws SQLException {
        statement.setFetchSize(connectionProperties.getResultSetFetchSize());
        sqlStatementExecutionHook.beforeAllStatements(connection, statement);
    }

    private void processJob(final Job job, final Connection connection,
                            final Statement statement) throws SQLException {
        LOGGER.trace("Process job: " + job.getSql());
        maybeEchoSql(job);
        sqlStatementExecutionHook.beforeStatement(connection, statement);
        if (connectionProperties.isBatchMode()) {
            processJobBatch(job, connection, statement);
        } else {
            processJobNonBatch(job, connection, statement);
        }
        sqlStatementExecutionHook.afterStatement(connection, statement);
    }

    private void maybeEchoSql(final Job job) {
        if (echoSqlPrintStream != null) {
            echoSqlPrintStream.println(job.getSql());
        }
    }

    private void processJobBatch(final Job job, final Connection connection,
                                 final Statement statement) throws SQLException {
        String sql = job.getSql();
        LOGGER.trace("Add to batch.");
        statement.addBatch(sql);
    }

    private void processJobNonBatch(final Job job, final Connection connection,
                                    final Statement statement) throws SQLException {
        sqlStatementExecutionHook.beforeStatement(connection, statement);
        String sql = job.getSql();
        boolean haveResult = statement.execute(sql);
        if (haveResult) {
            try {
                Processor processor = job.getProcessor();
                if (processor != null) {
                    processor.process(statement.getResultSet());
                }
            } catch (final ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
