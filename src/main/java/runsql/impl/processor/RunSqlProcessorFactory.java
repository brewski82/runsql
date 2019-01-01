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

import runsql.Arguments;
import runsql.Processor;
import runsql.SqlStatementExecutionHook;
import runsql.impl.RunSqlStatementExecutionHook;
import runsql.impl.arguments.ArgumentDefaults;
import runsql.impl.exceptions.RunSqlParseException;
import runsql.util.QuoteMode;
import runsql.util.TransactionMode;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static runsql.impl.arguments.Parameter.BATCH_SIZE;
import static runsql.impl.arguments.Parameter.BOOLEAN_FALSE_VALUE;
import static runsql.impl.arguments.Parameter.BOOLEAN_TRUE_VALUE;
import static runsql.impl.arguments.Parameter.COLUMN_SEPARATOR;
import static runsql.impl.arguments.Parameter.ESCAPE_CHARACTER;
import static runsql.impl.arguments.Parameter.FILE_FORMAT;
import static runsql.impl.arguments.Parameter.IMPORT_COLUMNS;
import static runsql.impl.arguments.Parameter.IMPORT_DRIVER;
import static runsql.impl.arguments.Parameter.IMPORT_PASSWORD;
import static runsql.impl.arguments.Parameter.IMPORT_TABLE;
import static runsql.impl.arguments.Parameter.IMPORT_URL;
import static runsql.impl.arguments.Parameter.IMPORT_USER;
import static runsql.impl.arguments.Parameter.INCLUDE_HEADERS;
import static runsql.impl.arguments.Parameter.NUMBER_OF_JOBS;
import static runsql.impl.arguments.Parameter.QUOTE_MODE;
import static runsql.impl.arguments.Parameter.QUOTE_VALUE;
import static runsql.impl.arguments.Parameter.ROW_SEPARATOR;
import static runsql.impl.arguments.Parameter.TABLE_NAME;
import static runsql.impl.arguments.Parameter.TRANSACTION_MODE;
import static runsql.impl.arguments.Parameter.VALUE_WHEN_NULL;

/**
 * Processors have many detailed configuration options. This class contains the logic to wire up a
 * processor.
 */
public class RunSqlProcessorFactory {
    public static final List<String> FILE_FORMAT_OPTIONS =
            Arrays.asList("csv", "msdoscsv", "inserts", "none");

    static String processSpecialChars(final String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        String result = input.replaceAll("\\\\n", "\n");
        result = result.replaceAll("\\\\t", "\t");
        result = result.replaceAll("\\\\b", "\b");
        result = result.replaceAll("\\\\r", "\r");
        result = result.replaceAll("\\\\f", "\f");
        return result;
    }

    public static Processor createProcessor(final Arguments arguments,
                                            final PrintStream printStream) throws RunSqlParseException {
        ResultSetProcessorFields fields = new ResultSetProcessorFields();
        fields.fileFormat = arguments.getValue(FILE_FORMAT.getEitherName()).toLowerCase();
        fields.tableName = arguments.getValue(TABLE_NAME.getEitherName());
        if (!FILE_FORMAT_OPTIONS.contains(fields.fileFormat)) {
            throw new RunSqlParseException("Invalid file format: " + fields.fileFormat, null);
        }
        if ("csv".equalsIgnoreCase(fields.fileFormat) || "msdoscsv"
                .equalsIgnoreCase(fields.fileFormat)) {
            fields.setToCvsMode();
        }
        if ("msdoscsv".equalsIgnoreCase(fields.fileFormat)) {
            fields.rowSeparator = "\r\n";
        }
        if ("inserts".equalsIgnoreCase(fields.fileFormat) && fields.tableName == null) {
            throw new RunSqlParseException(
                    "You must supply the tablename option with the inserts file format options",
                    null);
        }
        if ("inserts".equalsIgnoreCase(fields.fileFormat)) {
            fields.setToInsertsMode();
        }
        fields.processArguments(arguments);
        if (arguments.hasOption(IMPORT_TABLE.getEitherName())) {
            return buildImportProcessor(arguments);
        }
        RunSqlPrintStreamProcessor.Builder builder;
        if ("msdoscsv".equalsIgnoreCase(fields.fileFormat)) {
            builder = new RunSqlCsvMsDosProcessor.Builder();
        } else if ("inserts".equalsIgnoreCase(fields.fileFormat)) {
            RunSqlInsertsProcessor.Builder insertsBuilder = new RunSqlInsertsProcessor.Builder();
            insertsBuilder.tableName(fields.tableName);
            builder = insertsBuilder;
        } else {
            builder = new RunSqlPrintStreamProcessor.Builder();
        }
        return fields.buildResultSetProcessor(builder, printStream, arguments);
    }

    private static Processor buildImportProcessor(
            final Arguments arguments) throws RunSqlParseException {
        int numberOfJobs =
                Arguments.parsePositiveInteger(arguments.getValue(NUMBER_OF_JOBS.getEitherName()))
                         .intValue();
        RunSqlProcessorImport.Builder importBuilder;
        if (numberOfJobs > 1) {
            RunSqlProcessorImportConcurrent.Builder concurrentImportBuilder =
                    new RunSqlProcessorImportConcurrent.Builder();
            concurrentImportBuilder.setNumberOfJobs(numberOfJobs);
            importBuilder = concurrentImportBuilder;
        } else {
            importBuilder = new RunSqlProcessorImport.Builder();
        }
        importBuilder.setInsertTableName(arguments.getValue(IMPORT_TABLE.getEitherName()));
        String importColumns = arguments.getValue(IMPORT_COLUMNS.getEitherName());
        if (importColumns != null) {
            importBuilder.setInsertColumnNames(StringUtils.split(importColumns, ','));
        }
        importBuilder.setImportDriver(arguments.getValue(IMPORT_DRIVER.getEitherName()));
        importBuilder.setImportUrl(arguments.getValue(IMPORT_URL.getEitherName()));
        importBuilder.setImportUser(arguments.getValue(IMPORT_USER.getEitherName()));
        importBuilder.setImportPassword(arguments.getValue(IMPORT_PASSWORD.getEitherName()));
        BigInteger batchSize =
                Arguments.parsePositiveInteger(arguments.getValue(BATCH_SIZE.getEitherName()));
        long batchSizeLong = batchSize == null ? 1L : batchSize.longValue();
        long transactionSize = batchSizeLong;
        TransactionMode transactionMode = TransactionMode
                .parseTransactionMode(arguments.getValue(TRANSACTION_MODE.getEitherName()));
        if (transactionMode == TransactionMode.N) {
            BigInteger transactionSizeBig = Arguments
                    .parsePositiveInteger(arguments.getValue(TRANSACTION_MODE.getEitherName()));
            transactionSize = transactionSizeBig.longValue();
        }
        SqlStatementExecutionHook sqlStatementExecutionHook =
                new RunSqlStatementExecutionHook(true, batchSizeLong, transactionMode,
                                                 transactionSize);
        importBuilder.setSqlStatementExecutionHook(sqlStatementExecutionHook);
        importBuilder.setTransactionMode(transactionMode);
        return importBuilder.build();
    }

    private static class ResultSetProcessorFields {
        String fileFormat;
        String tableName;
        String columnSeparator = ArgumentDefaults.DEFAULT_COLUMN_SEPARATOR;
        String outputColumnNames = ArgumentDefaults.OUTPUT_COLUMN_NAMES;
        String escapeCharacter = ArgumentDefaults.DEFAULT_ESCAPE_CHARACTER;
        String encloseStringWithQuotes;
        String nullValue = ArgumentDefaults.DEFAULT_VALUE_WHEN_NULL;
        QuoteMode quoteMode;
        String rowSeparator = System.lineSeparator();

        private ResultSetProcessorFields() throws RunSqlParseException {
            quoteMode = QuoteMode.getQuoteMode(ArgumentDefaults.DEFAULT_QUOTE_MODE);
        }

        private void setToCvsMode() {
            columnSeparator = ",";
            nullValue = "";
            encloseStringWithQuotes = "\"";
            escapeCharacter = "\"";
            outputColumnNames = "t";
            quoteMode = QuoteMode.TEXT;
        }

        private void setToInsertsMode() {
            columnSeparator = ", ";
            nullValue = "null";
            encloseStringWithQuotes = "'";
            escapeCharacter = "'";
            outputColumnNames = "f";
            quoteMode = QuoteMode.TEXT;
        }

        private void processArguments(final Arguments arguments) throws RunSqlParseException {
            if (arguments.hasOption(COLUMN_SEPARATOR.getEitherName())) {
                columnSeparator =
                        processSpecialChars(arguments.getValue(COLUMN_SEPARATOR.getEitherName()));
            }
            if (arguments.hasOption(ROW_SEPARATOR.getEitherName())) {
                rowSeparator =
                        processSpecialChars(arguments.getValue(ROW_SEPARATOR.getEitherName()));
            }
            if (arguments.hasOption(VALUE_WHEN_NULL.getEitherName())) {
                nullValue = arguments.getValue(VALUE_WHEN_NULL.getEitherName());
            }
            if (arguments.hasOption(QUOTE_VALUE.getEitherName())) {
                encloseStringWithQuotes = arguments.getValue(QUOTE_VALUE.getEitherName());
            }
            if (arguments.hasOption(ESCAPE_CHARACTER.getEitherName())) {
                escapeCharacter = arguments.getValue(ESCAPE_CHARACTER.getEitherName());
            }
            if (arguments.hasOption(INCLUDE_HEADERS.getEitherName())) {
                outputColumnNames = arguments.getValue(INCLUDE_HEADERS.getEitherName());
            }
            if (arguments.hasOption(QUOTE_MODE.getEitherName())) {
                quoteMode = QuoteMode.getQuoteMode(arguments.getValue(QUOTE_MODE.getEitherName()));
            }
        }

        private Processor buildResultSetProcessor(final RunSqlPrintStreamProcessor.Builder builder,
                                                  final PrintStream printStream,
                                                  final Arguments arguments) throws RunSqlParseException {
            return builder.printStream(printStream)
                          .columnSeparator(columnSeparator)
                          .rowSeparator(rowSeparator)
                          .nullValue(nullValue)
                          .quoteString(encloseStringWithQuotes)
                          .escapeString(escapeCharacter)
                          .outputColumnNames(Arguments.parseBoolean(outputColumnNames))
                          .quoteMode(quoteMode)
                          .trueValue(arguments.getValue(BOOLEAN_TRUE_VALUE.getEitherName()))
                          .falseValue(arguments.getValue(BOOLEAN_FALSE_VALUE.getEitherName()))
                          .build();
        }
    }
}
