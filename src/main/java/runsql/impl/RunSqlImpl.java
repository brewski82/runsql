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

import runsql.Arguments;
import runsql.ConnectionPool;
import runsql.ConnectionProperties;
import runsql.JobConsumer;
import runsql.JobProducer;
import runsql.JobQueue;
import runsql.Processor;
import runsql.RunSql;
import runsql.SqlParser;
import runsql.SqlStatementExecutionHook;
import runsql.impl.arguments.Parameter;
import runsql.impl.arguments.RunSqlArguments;
import runsql.impl.exceptions.RequiredArgumentException;
import runsql.impl.processor.RunSqlProcessorFactory;
import runsql.util.TransactionMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static runsql.impl.arguments.Parameter.BATCH_SIZE;
import static runsql.impl.arguments.Parameter.DRIVER;
import static runsql.impl.arguments.Parameter.ECHO_SQL;
import static runsql.impl.arguments.Parameter.HELP;
import static runsql.impl.arguments.Parameter.IMPORT_TABLE;
import static runsql.impl.arguments.Parameter.INPUT_FILE_PATH;
import static runsql.impl.arguments.Parameter.NUMBER_OF_JOBS;
import static runsql.impl.arguments.Parameter.OUTPUT_FILE_PATH;
import static runsql.impl.arguments.Parameter.PASSWORD;
import static runsql.impl.arguments.Parameter.RESULT_SET_FETCH_SIZE;
import static runsql.impl.arguments.Parameter.SPLIT_SQL_STATEMENTS;
import static runsql.impl.arguments.Parameter.SQL;
import static runsql.impl.arguments.Parameter.STATEMENT_SEPARATOR;
import static runsql.impl.arguments.Parameter.STRIP_COMMENTS;
import static runsql.impl.arguments.Parameter.TABLE_NAME;
import static runsql.impl.arguments.Parameter.TRANSACTION_MODE;
import static runsql.impl.arguments.Parameter.URL;
import static runsql.impl.arguments.Parameter.USER;

/**
 * The class responsible for core orchestration of RunSql.
 * <p>
 * This class parses users arguments, creates one or more jobs based on the arguments, creates a
 * connection pool to the target database, and executes the jobs. Created jobs are added to a queue
 * and one or more separate threads executes the jobs.
 * <p>
 * Developers wanting to expand RunSql can look at extending this class. For example, those who wish
 * to use a custom Processor to handle result sets class can override the "createProcessor" method
 * to their liking.
 */
public class RunSqlImpl implements RunSql {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Parameter> REQUIRED_ARGUMENTS = Arrays.asList(DRIVER, URL, SQL);
    protected Arguments arguments;
    protected ConnectionProperties connectionProperties;
    protected SqlParser sqlParser;
    protected JobQueue jobQueue;
    protected PrintStream printStream;
    protected PrintStream echoSqlPrintStream;
    protected Processor processor;
    protected ConnectionPool connectionPool;
    protected ExecutorService executorService;
    protected JobProducer jobProducer;
    protected int numberOfJobConsumers;
    private int returnStatus;

    @Override
    public int run(final String[] args) {
        arguments = processArguments(args);
        // If we encounter invalid arguments, we print the help info. Therefore if we did print
        // help do not continue.
        if (didPrintHelp()) {
            return returnStatus;
        }
        checkForRequiredArguments();
        numberOfJobConsumers =
                Arguments.parsePositiveInteger(arguments.getValue(NUMBER_OF_JOBS.getEitherName()))
                         .intValue();
        // When using import functionality, limit the number of jobs reading the source data to one.
        if (arguments.hasOption(IMPORT_TABLE.getEitherName())) {
            numberOfJobConsumers = 1;
        }
        connectionProperties = createConnectionProperties();
        sqlParser = createSqlParser();
        jobQueue = createJobQueue();
        printStream = openPrintStream();
        echoSqlPrintStream = openEchoSqlStream();
        processor = createProcessor();
        connectionPool = createConnectionPool();
        executorService = createExecutorService();
        jobProducer = createJobProducer();
        // Create a list of Futures to hold the results of each job consumer. When each future's
        // blocking "get" method returns, we know all consumers finished and we can exit the
        // program.
        List<Future<?>> futures = new ArrayList<>(numberOfJobConsumers);
        IntStream.range(0, numberOfJobConsumers)
                 .forEach($ -> futures.add(executorService.submit(createJobConsumer())));
        jobProducer.produceJobs();
        try {
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (final ExecutionException e) {
                    final String message = "Error occurred when running a job.";
                    LOGGER.error(message, e);
                    returnStatus = 1;
                }
            }
            LOGGER.trace("shutdown");
            executorService.shutdown();
            LOGGER.trace("awaitTermination");
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (final InterruptedException e) {
            final String message = "Error - Job thread interrupted.";
            LOGGER.error(message, e);
            returnStatus = 1;
        }
        return returnStatus;
    }

    protected Arguments processArguments(final String[] args) {
        Arguments arguments = new RunSqlArguments();
        arguments.parse(args);
        return arguments;
    }

    protected boolean didPrintHelp() {
        return arguments.hasOption(HELP.getEitherName());
    }

    protected void checkForRequiredArguments() {
        for (Parameter parameter : REQUIRED_ARGUMENTS) {
            if (!arguments.hasOption(parameter.getEitherName())) {
                if (parameter != SQL || !arguments
                        .hasOption(TABLE_NAME.getEitherName()) && !arguments
                        .hasOption(INPUT_FILE_PATH.getEitherName())) {
                    throw new RequiredArgumentException(parameter);
                }
            }
        }
    }

    protected ConnectionProperties createConnectionProperties() {
        RunSqlConnectionProperties.Builder builder = new RunSqlConnectionProperties.Builder();
        builder.setUrl(arguments.getValue(URL.getEitherName()));
        builder.setUser(arguments.getValue(USER.getEitherName()));
        builder.setPassword(arguments.getValue(PASSWORD.getEitherName()));
        builder.setDriver(arguments.getValue(DRIVER.getEitherName()));
        BigInteger batchSize =
                Arguments.parsePositiveInteger(arguments.getValue(BATCH_SIZE.getEitherName()));
        if (arguments.hasOption(IMPORT_TABLE.getEitherName())) {
            batchSize = BigInteger.ONE;
        }
        builder.setBatchSize(batchSize);
        // Default the transaction size to the batch size.
        TransactionMode transactionMode = TransactionMode
                .parseTransactionMode(arguments.getValue(TRANSACTION_MODE.getEitherName()));
        builder.setTransactionSize(batchSize);
        if (transactionMode == TransactionMode.N) {
            builder.setTransactionSize(Arguments.parsePositiveInteger(
                    arguments.getValue(TRANSACTION_MODE.getEitherName())));
        }
        if (!arguments.hasOption(TRANSACTION_MODE.getEitherName()) && arguments
                .hasOption(BATCH_SIZE.getEitherName())) {
            transactionMode = TransactionMode.N;
        }
        builder.setTransactionMode(transactionMode);
        BigInteger resultSetFetchSize =
                Arguments.parseInteger(arguments.getValue(RESULT_SET_FETCH_SIZE.getEitherName()));
        builder.setResultSetFetchSize(resultSetFetchSize.intValue());
        builder.isBatchMode(batchSize != null && batchSize.compareTo(BigInteger.ONE) > 0);
        return builder.build();
    }

    protected SqlParser createSqlParser() {
        boolean stripComments =
                Arguments.parseBoolean(arguments.getValue(STRIP_COMMENTS.getEitherName()));
        String statementSeparator = arguments.getValue(STATEMENT_SEPARATOR.getEitherName());
        boolean separateStatements =
                Arguments.parseBoolean(arguments.getValue(SPLIT_SQL_STATEMENTS.getEitherName()));
        SqlParser sqlParser = new RunSqlParser(stripComments, statementSeparator.toCharArray(),
                                               separateStatements);
        String[] sqlStatements = arguments.getValues(SQL.getEitherName());
        if (sqlStatements != null) {
            for (String sqlStatement : sqlStatements) {
                sqlParser.addReader(new StringReader(sqlStatement));
            }
        }
        if (sqlParser.getReaders().isEmpty() && arguments.hasOption(TABLE_NAME.getEitherName())) {
            sqlParser.addReader(new StringReader(
                    "select * from " + arguments.getValue(TABLE_NAME.getEitherName())));
        }
        if (arguments.hasOption(INPUT_FILE_PATH.getEitherName())) {
            String inputFilePath = arguments.getValue(INPUT_FILE_PATH.getEitherName());
            Reader inputFileReader;
            try {
                inputFileReader =
                        "-".equalsIgnoreCase(inputFilePath) ? new InputStreamReader(System.in) :
                                new FileReader(inputFilePath);
            } catch (final FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            sqlParser.addReader(new BufferedReader(inputFileReader));
        }
        return sqlParser;
    }

    protected JobQueue createJobQueue() {
        return new RunSqlJobQueue();
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected PrintStream openPrintStream() {
        String outputFilePath = arguments.getValue(OUTPUT_FILE_PATH.getEitherName());
        if (outputFilePath == null || "-".equalsIgnoreCase(outputFilePath)) {
            return System.out;
        } else {
            try {
                return new PrintStream(outputFilePath);
            } catch (final FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected PrintStream openEchoSqlStream() {
        String sqlPrintStreamPath = arguments.getValue(ECHO_SQL.getEitherName());
        String outputFilePath = arguments.getValue(OUTPUT_FILE_PATH.getEitherName());
        if (outputFilePath != null && outputFilePath.equals(sqlPrintStreamPath)) {
            return printStream;
        }
        PrintStream sqlPrintStream = null;
        if (sqlPrintStreamPath != null) {
            try {
                sqlPrintStream = "-".equalsIgnoreCase(sqlPrintStreamPath) ? System.out :
                        new PrintStream(sqlPrintStreamPath);
            } catch (final FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return sqlPrintStream;
    }

    protected Processor createProcessor() {
        return RunSqlProcessorFactory.createProcessor(arguments, printStream);
    }

    protected ConnectionPool createConnectionPool() {
        return new RunSqlConnectionPool(connectionProperties);
    }

    protected ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(numberOfJobConsumers);
    }

    protected JobProducer createJobProducer() {
        return new RunSqlJobProducer(sqlParser, processor, jobQueue, numberOfJobConsumers);
    }

    protected JobConsumer createJobConsumer() {
        return new RunSqlJobConsumer(jobQueue, connectionPool, connectionProperties,
                                     createSqlStatementExecutionHook(), echoSqlPrintStream);
    }

    protected SqlStatementExecutionHook createSqlStatementExecutionHook() {
        return new RunSqlStatementExecutionHook(connectionProperties);
    }

    protected void closePrintStream(final Arguments arguments, final PrintStream printStream) {
        try {
            String outputFilePath = arguments.getValue(OUTPUT_FILE_PATH.getEitherName());
            if (outputFilePath != null && !"-".equalsIgnoreCase(outputFilePath)) {
                printStream.close();
            }
        } catch (final Exception e) {
            // Ignore
        }
    }

    protected void closeEchoSqlStream(final Arguments arguments, final PrintStream sqlPrintStream) {
        try {
            String sqlPrintStreamPath = arguments.getValue(ECHO_SQL.getEitherName());
            String outputFilePath = arguments.getValue(OUTPUT_FILE_PATH.getEitherName());
            if (outputFilePath != null && outputFilePath.equals(sqlPrintStreamPath)) {
                return;
            }
            if (sqlPrintStreamPath != null && !"-"
                    .equalsIgnoreCase(sqlPrintStreamPath) && sqlPrintStream != null) {
                sqlPrintStream.close();
            }
        } catch (final Exception e) {
            // Ignore
        }
    }
}
