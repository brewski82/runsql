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

import runsql.TestUtils;
import runsql.util.QuoteMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RunSqlCsvMsDosProcessorTest {
    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSetMetaData resultSetMetaData;

    @Test
    public void testProcess() throws SQLException, ClassNotFoundException {
        TestUtils.initResultSetMock(resultSet, resultSetMetaData);
        Mockito.when(resultSetMetaData.getColumnCount()).thenReturn(8);
        {
            final int colNum = 8;
            Mockito.when(resultSetMetaData.getColumnType(colNum)).thenReturn(Types.VARCHAR);
            final String colName = "numAsString";
            Mockito.when(resultSetMetaData.getColumnName(colNum)).thenReturn(colName);
            final String returnVal = "321.12";
            Mockito.when(resultSet.getString(colName)).thenReturn(returnVal);
            Mockito.when(resultSet.getString(colNum)).thenReturn(returnVal);
            Mockito.when(resultSet.getObject(colName)).thenReturn(returnVal);
            Mockito.when(resultSet.getObject(colNum)).thenReturn(returnVal);
        }
        RunSqlCsvMsDosProcessor.Builder builder = new RunSqlCsvMsDosProcessor.Builder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        builder.printStream(printStream);
        builder.columnSeparator(",");
        builder.escapeString("\\");
        builder.quoteString("'");
        builder.outputColumnNames(false);
        builder.quoteMode(QuoteMode.NEVER);
        builder.rowSeparator("\n");
        builder.trueValue("true");
        builder.falseValue("false");
        {
            RunSqlPrintStreamProcessor runSqlPrintStreamProcessor = builder.build();
            runSqlPrintStreamProcessor.process(resultSet);
            final String expectedDataRow =
                    "1,joe,123.34,true,2010-02-01,12:00:00,2010-05-20 10:09:00.0,='321.12'\n";
            assertEquals(expectedDataRow, byteArrayOutputStream.toString());
        }
    }
}
