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

import runsql.RunSqlMain;
import runsql.TestUtils;
import runsql.impl.exceptions.RunSqlParseException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunSqlProcessorImportTest {
    static final String DRIVER = "org.hsqldb.jdbc.JDBCDriver";
    static final String URL = "jdbc:hsqldb:mem:mymemdb";
    static final String USER = "SA";
    static final String PASSWORD = "";

    @BeforeEach
    public void setUp() throws ClassNotFoundException, SQLException, IOException {
        Class.forName(DRIVER);
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            try (InputStream createTableInputStream = TestUtils.class
                    .getResourceAsStream("CreateSchemaImport.sql")) {
                String createSchemaSql =
                        IOUtils.toString(createTableInputStream, Charset.defaultCharset());
                statement.execute(createSchemaSql);
            }
            try (InputStream insertDataInputStream = TestUtils.class
                    .getResourceAsStream("ImportData.sql")) {
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

    private void callRunSqlMain(final String... args) throws RunSqlParseException {
        RunSqlMain.callRunSql(runSqlArgs(args));
    }

    private void callRunSqlImport(final String... args) throws RunSqlParseException {
        List<String> allArgs = new ArrayList<>(Arrays.asList(args));
        allArgs.add("--importtable");
        allArgs.add("import_person");
        callRunSqlMain(allArgs.toArray(EMPTY_STRING_ARRAY));
    }

    private void printImportTable(final String outputFilePath) throws RunSqlParseException {
        callRunSqlMain("--sql", "select * from import_person order by person_id;",
                       "-outputfilepath", outputFilePath);
    }

    private String[] runSqlArgs(final String... args) {
        String[] connectionArgs =
                {"-user", USER, "-password", PASSWORD, "-driver", DRIVER, "-url", URL};
        List<String> allArgs = new ArrayList<>();
        allArgs.addAll(Arrays.asList(connectionArgs));
        allArgs.addAll(Arrays.asList(args));
        return allArgs.toArray(EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBasicImport() throws Exception {
        callRunSqlImport("--tablename", "person");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportConcurrent(final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--numberofjobs", String.valueOf(numberOfJobs));
        File file =
                File.createTempFile("basic-import-" + numberOfJobs + "-", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBasicImportBatchSize() throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "5");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportBatchSizeConcurrent(final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "5", "--numberofjobs",
                         String.valueOf(numberOfJobs));
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBasicImportLargeBatchSize() throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "50");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportLargeBatchSize(final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "50", "--numberofjobs",
                         String.valueOf(numberOfJobs));
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBasicImportBatchSizeSmallTransactionSize() throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "5", "--transactionmode", "3");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportBatchSizeSmallTransactionSize(
            final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "5", "--transactionmode", "3",
                         "--numberofjobs", String.valueOf(numberOfJobs));
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBasicImportBatchSizeLargeTransactionSize() throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "5", "--transactionmode", "30");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportBatchSizeLargeTransactionSize(
            final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "5", "--transactionmode", "30",
                         "--numberofjobs", String.valueOf(numberOfJobs));
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBasicImportLargeBatchSizeTransactionSize() throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "50", "--transactionmode", "3");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportLargeBatchSizeTransactionSize(
            final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "50", "--transactionmode", "3",
                         "--numberofjobs", String.valueOf(numberOfJobs));
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBasicImportLargeBatchSizeLargeTransactionSize() throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "50", "--transactionmode", "30");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportLargeBatchSizeLargeTransactionSize(
            final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "50", "--transactionmode", "30",
                         "--numberofjobs", String.valueOf(numberOfJobs));
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.doesFileMatchResourceFile(outputFilePath,
                                                       "BasicImportExpectedResults.txt"));
        file.delete();
    }

    @Test
    public void testBasicImportBatchSizeRollback() throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "5", "--transactionmode",
                         "rollback");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.isFileEmpty(outputFilePath));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportBatchSizeRollback(final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "5", "--transactionmode",
                         "rollback", "--numberofjobs", String.valueOf(numberOfJobs));
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.isFileEmpty(outputFilePath));
        file.delete();
    }

    @Test
    public void testBasicImportBatchSizeRollbackLarge() throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "50", "--transactionmode",
                         "rollback");
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.isFileEmpty(outputFilePath));
        file.delete();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 13, 21, 40})
    public void testBasicImportBatchSizeRollbackLarge(final int numberOfJobs) throws Exception {
        callRunSqlImport("--tablename", "person", "--batchsize", "50", "--transactionmode",
                         "rollback", "--numberofjobs", String.valueOf(numberOfJobs));
        File file = File.createTempFile("basic-import", "tmp");
        String outputFilePath = file.getAbsolutePath();
        printImportTable(outputFilePath);
        assertTrue(TestUtils.isFileEmpty(outputFilePath));
        file.delete();
    }
}
