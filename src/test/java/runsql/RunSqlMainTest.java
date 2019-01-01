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

package runsql;

import runsql.impl.exceptions.RequiredArgumentException;
import runsql.impl.exceptions.RunSqlParseException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunSqlMainTest {
    static final String DRIVER = "org.hsqldb.jdbc.JDBCDriver";
    static final String URL = "jdbc:hsqldb:mem:mymemdb";
    static final String USER = "SA";
    static final String PASSWORD = "";
    private static final String[] EMPTY_STRING_ARRAY = {};

    @Test
    public void RunSqlMain() {
        RunSqlMain.callRunSql(new String[]{"-help"});
    }

    /* The following tests mirror the tests ProcessorTests. */

    @BeforeEach
    public void setUp() throws Exception {
        Class.forName(DRIVER);
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            try (InputStream createTableInputStream = getClass()
                    .getResourceAsStream("ProcessorTestSetupDb.sql")) {
                String createTableSql =
                        IOUtils.toString(createTableInputStream, Charset.defaultCharset());
                statement.execute(createTableSql);
            }
            try (InputStream insertDataInputStream = getClass()
                    .getResourceAsStream("ProcessorTestSetupData.sql")) {
                String insertDataSql =
                        IOUtils.toString(insertDataInputStream, Charset.defaultCharset());
                statement.execute(insertDataSql);
            }
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN;");
        }
    }

    @Test
    public void testBasicQuery() throws Exception {
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestBasicQueryExpectedResults.txt"));
        file.delete();
    }

    private void callRunSqlMain(final String... args) throws RunSqlParseException {
        RunSqlMain.callRunSql(runSqlArgs(args));
    }

    private String[] runSqlArgs(final String... args) {
        String[] connectionArgs = {"-user", USER, "-password", PASSWORD,
                                   "-driver", DRIVER, "-url", URL};
        List<String> allArgs = new ArrayList<>();
        allArgs.addAll(Arrays.asList(connectionArgs));
        allArgs.addAll(Arrays.asList(args));
        return allArgs.toArray(EMPTY_STRING_ARRAY);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicQueryMultipleJobs(final int numberOfJobs) throws Exception {
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath,
                       "-numberofjobs", String.valueOf(numberOfJobs));
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestBasicQueryExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBasicInsert() throws Exception {
        callRunSqlMain("-sql",
                       "insert into person values (3, 'firstthree', 'lasttwo', '2000-02-01', true, null);");
        File file = File.createTempFile("basic-insert", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestBasicInsertExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testColumnSeparator() throws Exception {
        File file = File.createTempFile("column-separator", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-columnseparator", "|", "-outputfilepath",
                       outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestColumnSeparatorExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testMultipleSqlStatements() throws Exception {
        File file = File.createTempFile("multiple-sql-statements", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql", "select * from person;", "-sql",
                       "select last_name from person;");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestMultipleSqlStatementsExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testMultipleSqlStatementsWithSplitter() throws Exception {
        File file = File.createTempFile("multiple-sql-statements", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql",
                       "select * from person;select last_name from person;");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestMultipleSqlStatementsExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testMultipleSqlStatementsWithSplitterNonDefaults() throws Exception {
        File file = File.createTempFile("multiple-sql-statements", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql",
                       "select * from person|select last_name from person;",
                       "-sqlstatementseparator", "|");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestMultipleSqlStatementsExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testValueWhenNull() throws Exception {
        File file = File.createTempFile("value-when-null", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql", "select * from person;",
                       "-valuewhennull", "<null>");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestValueWhenNull.txt"));
        file.delete();
    }

    @Test
    public void testEncloseStringsWithQuotes() throws Exception {
        File file = File.createTempFile("enclose-strings-with-quotes", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql",
                       "select person.*, 'escaped \" quote' x from person;",
                       "-valuewhennull", "<null>", "-quotevalue", "\"", "-escapecharacter", "\\",
                       "--quotemode=text");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestEncloseStringsWithQuotes.txt"));
        file.delete();
    }

    @Test
    public void testOutputColumnNames() throws Exception {
        File file = File.createTempFile("column-names", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql", "select * from person;",
                       "-includeheaders", "t");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestOutputColumnNamesExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testSqlInputFile() throws Exception {
        File inputFile = File.createTempFile("input-sql", "sql");
        String inputFilePath = inputFile.getAbsolutePath();
        try (OutputStream outputStream = new FileOutputStream(inputFilePath)) {
            IOUtils.write("select * from person;select last_name from person;", outputStream,
                          Charset.defaultCharset());
        }
        File file = File.createTempFile("multiple-sql-statements", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-inputfilepath", inputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestMultipleSqlStatementsExpectedResults.txt"));
        file.delete();
        inputFile.delete();
    }

    @Test
    public void testCsv() throws Exception {
        File file = File.createTempFile("csv-test", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql",
                       "select person.*, 'escaped \" quote' x from person;",
                       "-fileformat", "csv");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "MainTestCsvExpectedResults.csv"));
        file.delete();
    }

    @Test
    public void testCsvOverride() throws Exception {
        File file = File.createTempFile("csv-test", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql",
                       "select person.*, 'escaped \" quote' x from person;",
                       "-fileformat", "csv", "-columnseparator", "|");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "MainTestCsvOverrideExpectedResults.csv"));
        file.delete();
    }

    @Test
    public void testBadFileFormat() {
        assertThrows(RunSqlParseException.class,
                     () -> callRunSqlMain("-fileformat", "cxv", "--sql=blah"));
    }

    @Test
    public void testFileFormatInsertsWithNoTableName() {
        assertThrows(RunSqlParseException.class,
                     () -> callRunSqlMain("-fileformat", "inserts", "-sql", "blah"));
    }

    @Test
    public void testCsvMsDos() throws Exception {
        File file = File.createTempFile("csv-test", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql",
                       "select person.*, 'escaped \" quote' x, " +
                               "'0001' num_leading_zero, '1234.45' num_deciaml from person;",
                       "-fileformat", "msdoscsv");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "MainTestCsvMsDosExpectedResults.csv"));
        file.delete();
    }

    @Test
    public void testTableName() throws Exception {
        File file = File.createTempFile("table-name", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-tablename", "person", "-outputfilepath", outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestBasicQueryExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testInsertsWithTableNameOnly() throws Exception {
        File file = File.createTempFile("table-name-inserts", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-tablename", "person", "-outputfilepath", outputFilePath, "-fileformat",
                       "inserts");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "MainTestInsertsTableName.sql"));
        file.delete();
    }

    @Test
    public void testInsertsWithoutTableName() throws Exception {
        File file = File.createTempFile("no-table-name-inserts", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-tablename", "person", "-outputfilepath", outputFilePath,
                       "-fileformat", "inserts", "-sql",
                       "select first_name, last_name from person;");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "MainTestInsertsWithoutTableName.sql"));
        file.delete();
    }

    @Test
    public void testPrintSqlStream() throws Exception {
        File file = File.createTempFile("print-sql", "tmp");
        File printSqlFile = File.createTempFile("print-sql-path", "tmp");
        String outputFilePath = file.getAbsolutePath();
        String sqlPath = printSqlFile.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql",
                       "select first_name, last_name from person;",
                       "-echosql", sqlPath);
        assertTrue(TestUtils.doesFileMatchResourceFile(sqlPath, "MainTestPrintSql.txt"));
        file.delete();
        printSqlFile.delete();
    }

    @Test
    public void testPrintSqlStreamSameOutputStream() throws Exception {
        File file = File.createTempFile("print-sql", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-outputfilepath", outputFilePath, "-sql",
                       "select first_name, last_name from person;",
                       "-echosql", outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "MainTestEchoSqlSameOutputFile"));
        file.delete();
    }

    @Test
    public void testRequiredArguments() {
        assertThrows(RequiredArgumentException.class, () -> callRunSqlMain("-password", "sql"));
    }

    @Test
    public void testInvalidQuoteMode() {
        assertThrows(RunSqlParseException.class,
                     () -> callRunSqlMain("--quotemode", "badquotemode", "-sql", "dummysql"));
    }

    @Test
    public void testValidQuoteMode() throws Exception {
        File file = File.createTempFile("print-sql", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("--quotemode=always", "-tablename", "person", "--outputfilepath",
                       outputFilePath);
        file.delete();
    }

    @Test
    public void testQuoteModeWithRegexQuotes() throws Exception {
        File file = File.createTempFile("print-sql", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("--quotemode=always", "-tablename", "person", "-fileformat", "csv",
                       "-quotevalue", "|", "--outputfilepath", outputFilePath);
        file.delete();
    }

    @Test
    public void testQueryRollback() throws Exception {
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath,
                       "--transactionmode", "rollback");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestBasicQueryExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testQueryAuto() throws Exception {
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath,
                       "--transactionmode", "auto");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestBasicQueryExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testQueryN() throws Exception {
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath,
                       "--transactionmode", "20");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestBasicQueryExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testQueryBatchNoResults() throws Exception {
        File file = File.createTempFile("basic-query-query-batch-no-results", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath,
                       "--batchsize", "20");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath, "EmptyFile.txt"));
        file.delete();
    }

    @Test
    public void testQueryBatchWithSizeMoreThanStatements() throws Exception {
        batchInsertsExpectFullResults("--batchsize", "20");
    }

    private void batchInsertsExpectFullResults(
            final String... args) throws IOException, RunSqlParseException {
        batchInsertsExpect("BatchInsertsExpectedResults.txt", args);
    }

    private void batchInsertsExpect(final String expectedResultsFilePath,
                                    final String... args) throws IOException, RunSqlParseException {
        String inputFilePath = TestUtils.saveResourceFileToTemporaryFile("BatchInserts.sql");
        List<String> allArgs = new ArrayList<>();
        allArgs.add("--inputfilepath");
        allArgs.add(inputFilePath);
        allArgs.addAll(Arrays.asList(args));
        callRunSqlMain(allArgs.toArray(EMPTY_STRING_ARRAY));
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person order by person_id;", "-outputfilepath",
                       outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath, expectedResultsFilePath));
        file.delete();
    }

    @Test
    public void testQueryBatchWithSizeLessThanStatements() throws Exception {
        batchInsertsExpectFullResults("--batchsize", "4");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testQueryBatchWithSizeLessThanStatementsMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--batchsize", "4", "--numberofjobs",
                                      String.valueOf(numberOfJobs));
    }

    @Test
    public void testQueryBatchWithSizeEqualStatements() throws Exception {
        batchInsertsExpectFullResults("--batchsize", "14");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testQueryBatchWithSizeEqualStatementsMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--batchsize", "14", "--numberofjobs",
                                      String.valueOf(numberOfJobs));
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testQueryMultipleJobs(final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--numberofjobs", String.valueOf(numberOfJobs));
    }

    @Test
    public void testQueryMultipleJobsGreaterThanStatements() throws Exception {
        batchInsertsExpectFullResults("--numberofjobs", "20");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testQueryMultipleJobsGreaterThanStatementsMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--numberofjobs", "20", "--numberofjobs",
                                      String.valueOf(numberOfJobs));
    }

    @Test
    public void testQueryBatchWithSizeMoreThanStatementsRollback() throws Exception {
        batchInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "20",
                           "--transactionmode", "rollback");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testQueryBatchWithSizeMoreThanStatementsRollbackMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "20",
                           "--transactionmode", "rollback", "--numberofjobs",
                           String.valueOf(numberOfJobs));
    }

    @Test
    public void testQueryBatchWithSizeLessThanStatementsRollback() throws Exception {
        batchInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "3",
                           "--transactionmode", "rollback");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testQueryBatchWithSizeLessThanStatementsRollbackMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "3",
                           "--transactionmode", "rollback", "--numberofjobs",
                           String.valueOf(numberOfJobs));
    }

    @Test
    public void testQueryBatchWithSizeEqualStatementsRollback() throws Exception {
        batchInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "14",
                           "--transactionmode", "rollback");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testQueryBatchWithSizeEqualStatementsRollbackMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "14",
                           "--transactionmode", "rollback", "--numberofjobs",
                           String.valueOf(numberOfJobs));
    }

    @Test
    public void testTransactionSizeLessThanStatements() throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "4");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testTransactionSizeLessThanStatementsMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "4", "--numberofjobs",
                                      String.valueOf(numberOfJobs));
    }

    @Test
    public void testTransactionSizeMoreThanStatements() throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "50");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testTransactionSizeMoreThanStatementsMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "50", "--numberofjobs",
                                      String.valueOf(numberOfJobs));
    }

    @Test
    public void testTransactionSizeEqualToStatements() throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "14");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testTransactionSizeEqualToStatementsMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "14", "--numberofjobs",
                                      String.valueOf(numberOfJobs));
    }

    @Test
    public void testTransactionSizeGreaterThanBatchSize() throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "5", "--batchsize", "4");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testTransactionSizeGreaterThanBatchSizeMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "5", "--batchsize", "4",
                                      "--numberofjobs", String.valueOf(numberOfJobs));
    }

    @Test
    public void testTransactionSizeLessThanBatchSize() throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "2", "--batchsize", "5");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testTransactionSizeLessThanBatchSizeMultipleJobs(
            final int numberOfJobs) throws Exception {
        batchInsertsExpectFullResults("--transactionmode", "2", "--batchsize", "5",
                                      "--numberofjobs", String.valueOf(numberOfJobs));
    }

    @Test
    public void testBadInsertSmallTransactionNumber() throws Exception {
        batchBadInsertsExpect("testBadInsertSmallTransactionNumberExpectedResults.txt",
                              "--transactionmode", "3");
    }

    private void batchBadInsertsExpect(final String expectedResultsFilePath,
                                       final String... args) throws IOException, RunSqlParseException {
        String inputFilePath =
                TestUtils.saveResourceFileToTemporaryFile("BatchInsertsWithBadInsert.sql");
        List<String> allArgs = new ArrayList<>();
        allArgs.add("--inputfilepath");
        allArgs.add(inputFilePath);
        allArgs.addAll(Arrays.asList(args));
        try {
            callRunSqlMain(allArgs.toArray(EMPTY_STRING_ARRAY));
        } catch (final Exception exception) {
            // Ignore
        }
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person order by person_id;", "-outputfilepath",
                       outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath, expectedResultsFilePath));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBadInsertMultipleJobs(final int numberOfJobs) throws Exception {
        batchBadInsertsExpect("BatchInsertsMinusOneBadRecordExpectedResults.txt", "--numberofjobs",
                              String.valueOf(numberOfJobs));
    }

    @Test
    public void testBadInsertEqualTransactionNumber() throws Exception {
        batchBadInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--transactionmode",
                              "11");
    }

    @Test
    public void testBadInsertGreaterTransactionNumber() throws Exception {
        batchBadInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--transactionmode",
                              "30");
    }

    @Test
    public void testBadInsertSmallBatch() throws Exception {
        batchBadInsertsExpect("testBadInsertSmallTransactionNumberExpectedResults.txt",
                              "--batchsize", "3");
    }

    @Test
    public void testBadInsertEqualBatch() throws Exception {
        batchBadInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "11");
    }

    @Test
    public void testBadInsertGreaterBatch() throws Exception {
        batchBadInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "30");
    }

    @Test
    public void testBadInsertSmallBatchSmallTransaction() throws Exception {
        batchBadInsertsExpect("testBadInsertSmallTransactionNumberExpectedResults.txt",
                              "--batchsize", "3",
                              "--transactionmode", "2");
    }

    @Test
    public void testBadInsertSmallBatchBigTransaction() throws Exception {
        batchBadInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "3",
                              "--transactionmode", "20");
    }

    @Test
    public void testBadInsertLargeBatchSmallTransaction() throws Exception {
        batchBadInsertsExpect("ProcessorTestBasicQueryExpectedResults.txt", "--batchsize", "30",
                              "--transactionmode", "2");
    }

    @Test
    public void testBooleanDefaults() throws Exception {
        callRunSqlMain("--sql",
                       "insert into person values (3, 'firstthree', 'lasttwo', '2000-02-01', false, null);");
        File file = File.createTempFile("boolean-defaults", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "MainTestBooleanDefaultsExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBooleanNonDefaults() throws Exception {
        callRunSqlMain("--sql",
                       "insert into person values (3, 'firstthree', 'lasttwo', '2000-02-01', false, null);");
        File file = File.createTempFile("boolean-non-defaults", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath,
                       "--booleantruevalue", "t", "--booleanfalsevalue", "f");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "MainTestBooleanNonDefaultsExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testFetchSize() throws Exception {
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        callRunSqlMain("-sql", "select * from person;", "-outputfilepath", outputFilePath,
                       "--resultsetfetchsize", "100");
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "ProcessorTestBasicQueryExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBadFetchSize() throws Exception {
        File file = File.createTempFile("basic-query", "tmp");
        String outputFilePath = file.getAbsolutePath();
        assertThrows(RunSqlParseException.class,
                     () -> callRunSqlMain("-sql", "select * from person;", "-outputfilepath",
                                          outputFilePath, "--resultsetfetchsize", "100x"));
    }
}
