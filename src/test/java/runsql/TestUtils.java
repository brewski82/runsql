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

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestUtils {
    public static void initResultSetMock(final ResultSet resultSet,
                                         final ResultSetMetaData resultSetMetaData) {
        try {
            Mockito.when(resultSetMetaData.getColumnCount()).thenReturn(7);
            {
                final int colNum = 1;
                Mockito.when(resultSetMetaData.getColumnType(colNum)).thenReturn(Types.INTEGER);
                final String colName = "id";
                Mockito.when(resultSetMetaData.getColumnName(colNum)).thenReturn(colName);
                final int returnVal = 1;
                Mockito.when(resultSet.getInt(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getInt(colNum)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colNum)).thenReturn(returnVal);
            }
            {
                final int colNum = 2;
                Mockito.when(resultSetMetaData.getColumnType(colNum)).thenReturn(Types.VARCHAR);
                final String colName = "name";
                Mockito.when(resultSetMetaData.getColumnName(colNum)).thenReturn(colName);
                final String returnVal = "joe";
                Mockito.when(resultSet.getString(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getString(colNum)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colNum)).thenReturn(returnVal);
            }
            {
                final int colNum = 3;
                Mockito.when(resultSetMetaData.getColumnType(colNum)).thenReturn(Types.DECIMAL);
                final String colName = "amount";
                Mockito.when(resultSetMetaData.getColumnName(colNum)).thenReturn(colName);
                final BigDecimal returnVal = new BigDecimal("123.34");
                Mockito.when(resultSet.getBigDecimal(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getBigDecimal(colNum)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colNum)).thenReturn(returnVal);
            }
            {
                final int colNum = 4;
                Mockito.when(resultSetMetaData.getColumnType(colNum)).thenReturn(Types.BOOLEAN);
                final String colName = "bool";
                Mockito.when(resultSetMetaData.getColumnName(colNum)).thenReturn(colName);
                final boolean returnVal = true;
                Mockito.when(resultSet.getBoolean(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getBoolean(colNum)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colNum)).thenReturn(returnVal);
            }
            {
                final Date returnVal = Date.valueOf("2010-02-01");
                Mockito.when(resultSetMetaData.getColumnType(5)).thenReturn(Types.DATE);
                final String colName = "date";
                Mockito.when(resultSetMetaData.getColumnName(5)).thenReturn(colName);
                Mockito.when(resultSet.getDate(colName)).thenReturn(returnVal);
                final int colNum = 5;
                Mockito.when(resultSet.getDate(colNum)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colNum)).thenReturn(returnVal);
            }
            {
                final Time returnVal = Time.valueOf(LocalTime.NOON);
                Mockito.when(resultSetMetaData.getColumnType(6)).thenReturn(Types.TIME);
                final String colName = "time";
                Mockito.when(resultSetMetaData.getColumnName(6)).thenReturn(colName);
                Mockito.when(resultSet.getTime(colName)).thenReturn(returnVal);
                final int colNum = 6;
                Mockito.when(resultSet.getTime(colNum)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colNum)).thenReturn(returnVal);
            }
            {
                final Timestamp returnVal = Timestamp.valueOf(LocalDateTime.of(2010, 5, 20, 10, 9));
                Mockito.when(resultSetMetaData.getColumnType(7)).thenReturn(Types.TIMESTAMP);
                final String colName = "timestamp";
                Mockito.when(resultSetMetaData.getColumnName(7)).thenReturn(colName);
                Mockito.when(resultSet.getTimestamp(colName)).thenReturn(returnVal);
                final int colNum = 7;
                Mockito.when(resultSet.getTimestamp(colNum)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colName)).thenReturn(returnVal);
                Mockito.when(resultSet.getObject(colNum)).thenReturn(returnVal);
            }
            Mockito.when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            Mockito.when(resultSet.next()).thenReturn(true).thenReturn(false);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean doesFileMatchResourceFile(final String fileName,
                                                    final String resourceFileName) throws IOException {
        String fileContents;
        String resourceContents;
        try (InputStream fileInputStream = new FileInputStream(new File(fileName));
             InputStream resourceInputStream = TestUtils.class
                     .getResourceAsStream(resourceFileName)) {
            fileContents = IOUtils.toString(fileInputStream, Charset.defaultCharset());
            resourceContents = IOUtils.toString(resourceInputStream, Charset.defaultCharset());
        }
        return fileContents.equals(resourceContents);
    }

    public static String saveResourceFileToTemporaryFile(
            final String resourceFileName) throws IOException {
        File file = File.createTempFile("runsql", "tmp");
        InputStream inputStream = TestUtils.class.getResourceAsStream(resourceFileName);
        PrintStream printStream = new PrintStream(file);
        IOUtils.copy(inputStream, printStream);
        inputStream.close();
        printStream.close();
        return file.getAbsolutePath();
    }

    public static boolean isFileEmpty(final String filePath) throws IOException {
        String fileContents;
        try (InputStream fileInputStream = new FileInputStream(new File(filePath))) {
            fileContents = IOUtils.toString(fileInputStream, Charset.defaultCharset());
        }
        return "".equals(fileContents);
    }
}
