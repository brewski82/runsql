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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RunSqlInsertsProcessorTest {
    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSetMetaData resultSetMetaData;

    @Test
    public void testProcess() throws SQLException, ClassNotFoundException {
        TestUtils.initResultSetMock(resultSet, resultSetMetaData);
        RunSqlInsertsProcessor.Builder builder = new RunSqlInsertsProcessor.Builder();
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
        builder.tableName("mytab");
        {
            RunSqlPrintStreamProcessor runSqlPrintStreamProcessor = builder.build();
            runSqlPrintStreamProcessor.process(resultSet);
            final String expectedDataRow =
                    "insert into mytab (id,name,amount,bool,date,time,timestamp) values " +
                            "(1,joe,123.34,true,2010-02-01,12:00:00,2010-05-20 10:09:00.0);\n";
            assertEquals(expectedDataRow, byteArrayOutputStream.toString());
        }
    }
}
