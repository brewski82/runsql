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

package runsql.impl.arguments;

import runsql.Arguments;
import runsql.TestUtils;
import runsql.impl.exceptions.RunSqlParseException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static runsql.impl.arguments.Parameter.DRIVER;
import static runsql.impl.arguments.Parameter.IMPORT_DRIVER;
import static runsql.impl.arguments.Parameter.IMPORT_PASSWORD;
import static runsql.impl.arguments.Parameter.IMPORT_URL;
import static runsql.impl.arguments.Parameter.IMPORT_USER;
import static runsql.impl.arguments.Parameter.PASSWORD;
import static runsql.impl.arguments.Parameter.SQL;
import static runsql.impl.arguments.Parameter.URL;
import static runsql.impl.arguments.Parameter.USER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RunSqlArgumentsTest {
    @Test
    public void testBasicParse() {
        Arguments arguments = new RunSqlArguments();
        arguments.parse(new String[]{"--url=jdbc://testurl"});
    }

    @Test
    public void testUnknownArgument() {
        Arguments arguments = new RunSqlArguments();
        assertThrows(RunSqlParseException.class,
                     () -> arguments.parse(new String[]{"-U", "jdbc://testurl", "-badarg"}));
    }

    @Test
    public void testArgumentWithoutPropertiesFile() {
        Arguments arguments = new RunSqlArguments();
        String myUrl = "jdbc://myjdbcurl";
        arguments.parse(new String[]{"-U", myUrl});
        assertEquals(myUrl, arguments.getValue(URL.getEitherName()));
    }

    @Test
    public void testArgumentWithPropertiesFile() throws Exception {
        String propertiesFilePath = getTestSourcePropertiesFilePath();
        Arguments arguments = new RunSqlArguments();
        arguments.parse(new String[]{"-propertiesfilepath", propertiesFilePath, "-propertiesprefix",
                                     "testsource."});
        assertEquals("testuser", arguments.getValue(USER.getEitherName()));
        assertEquals("testpassword", arguments.getValue(PASSWORD.getEitherName()));
        assertEquals("temp", arguments.getValue(URL.getEitherName()));
    }

    private String getTestSourcePropertiesFilePath() throws IOException {
        File file = File.createTempFile("runsql-test-temp-file", "tmp");
        try (OutputStream outputStream = new FileOutputStream(file);
             InputStream inputStream = TestUtils.class
                     .getResourceAsStream("RunSqlTest.properties")) {
            IOUtils.copy(inputStream, outputStream);
        }
        return file.getAbsolutePath();
    }

    @Test
    public void testCommandLineArgumentsPrecedence() throws Exception {
        String propertiesFilePath = getTestSourcePropertiesFilePath();
        Arguments arguments = new RunSqlArguments();
        arguments.parse(new String[]{"-propertiesfilepath", propertiesFilePath,
                                     "-propertiesfileprefix", "testsource.", "--user", "myuser"});
        assertEquals("myuser", arguments.getValue(USER.getEitherName()));
    }

    @Test
    public void testMultipleSqlStatements() {
        Arguments arguments = new RunSqlArguments();
        arguments.parse(new String[]{"-s", "statementOne", "--sql=statementTwo"});
        assertArrayEquals(new String[]{"statementOne", "statementTwo"},
                          arguments.getValues(SQL.getEitherName()));
    }

    @Test
    public void testImportArgumentsSamePropertyFile() throws Exception {
        String propertiesFilePath = getTestSourcePropertiesFilePath();
        Arguments arguments = new RunSqlArguments();
        arguments.parse(new String[]{"-propertiesfilepath", propertiesFilePath,
                                     "--importpropertiesprefix", "import.", "--propertiesprefix",
                                     "testsource.",
                                     "--driver", "driver", "--importurl", "importurl"});
        assertEquals("import-user", arguments.getValue(IMPORT_USER.getEitherName()));
        assertEquals("testuser", arguments.getValue(USER.getEitherName()));
        assertEquals("testpassword", arguments.getValue(PASSWORD.getEitherName()));
        assertNull(arguments.getValue(IMPORT_PASSWORD.getEitherName()));
        assertEquals("driver", arguments.getValue(IMPORT_DRIVER.getEitherName()));
        assertEquals("driver", arguments.getValue(DRIVER.getEitherName()));
        assertEquals("importurl", arguments.getValue(IMPORT_URL.getEitherName()));
        assertEquals("temp", arguments.getValue(URL.getEitherName()));
        Arguments secondArguments = new RunSqlArguments();
        secondArguments.parse(new String[]{"-propertiesfilepath", propertiesFilePath,
                                           "--importpropertiesprefix", "import.",
                                           "--propertiesprefix", "testsource.",
                                           "--importdriver", "importdriver"});
        assertEquals("importdriver", secondArguments.getValue(IMPORT_DRIVER.getEitherName()));
        assertNull(secondArguments.getValue(DRIVER.getEitherName()));
    }

    @Test
    public void testImportArgumentsSamePropertyFileSamePrefix() throws Exception {
        String propertiesFilePath = getTestSourcePropertiesFilePath();
        Arguments arguments = new RunSqlArguments();
        arguments
                .parse(new String[]{"-propertiesfilepath", propertiesFilePath, "--propertiesprefix",
                                    "testsource.", "--driver", "driver", "--importurl",
                                    "importurl"});
        assertEquals("testuser", arguments.getValue(IMPORT_USER.getEitherName()));
        assertEquals("testuser", arguments.getValue(USER.getEitherName()));
        assertEquals("driver", arguments.getValue(IMPORT_DRIVER.getEitherName()));
        assertEquals("driver", arguments.getValue(DRIVER.getEitherName()));
        assertEquals("importurl", arguments.getValue(IMPORT_URL.getEitherName()));
        assertEquals("temp", arguments.getValue(URL.getEitherName()));
    }
}
