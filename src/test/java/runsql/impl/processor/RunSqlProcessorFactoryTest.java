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
import runsql.impl.arguments.RunSqlArguments;
import runsql.impl.exceptions.RunSqlParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunSqlProcessorFactoryTest {
    @Test
    public void testProcessSpecialChars() {
        assertNull(RunSqlProcessorFactory.processSpecialChars(null));
        assertNull(RunSqlProcessorFactory.processSpecialChars(""));
        assertNull(RunSqlProcessorFactory.processSpecialChars("   "));
        assertEquals("abc", RunSqlProcessorFactory.processSpecialChars("abc"));
        assertEquals("\n", RunSqlProcessorFactory.processSpecialChars("\\n"));
        assertEquals("\t", RunSqlProcessorFactory.processSpecialChars("\\t"));
        assertEquals("\b", RunSqlProcessorFactory.processSpecialChars("\\b"));
        assertEquals("\r", RunSqlProcessorFactory.processSpecialChars("\\r"));
        assertEquals("\f", RunSqlProcessorFactory.processSpecialChars("\\f"));
        assertEquals("\n\f", RunSqlProcessorFactory.processSpecialChars("\\n\\f"));
        assertEquals("\nabc\f", RunSqlProcessorFactory.processSpecialChars("\\nabc\\f"));
    }

    @Test
    public void testCreateProcessor() {
        {
            Arguments arguments = new RunSqlArguments();
            arguments.parse(new String[]{"--fileformat", "csv"});
            Processor processor = RunSqlProcessorFactory.createProcessor(arguments, null);
            assertTrue(processor instanceof RunSqlPrintStreamProcessor);
        }
        {
            Arguments arguments = new RunSqlArguments();
            arguments.parse(new String[]{"--fileformat", "msdoscsv"});
            Processor processor = RunSqlProcessorFactory.createProcessor(arguments, null);
            assertTrue(processor instanceof RunSqlCsvMsDosProcessor);
        }
        {
            Arguments arguments = new RunSqlArguments();
            arguments.parse(new String[]{"--fileformat", "inserts", "--tablename", "table"});
            Processor processor = RunSqlProcessorFactory.createProcessor(arguments, null);
            assertTrue(processor instanceof RunSqlInsertsProcessor);
        }
    }

    @Test
    public void testBadInserts() {
        Arguments arguments = new RunSqlArguments();
        arguments.parse(new String[]{"--fileformat", "inserts"});
        assertThrows(RunSqlParseException.class,
                     () -> RunSqlProcessorFactory.createProcessor(arguments, null));
    }

    @Test
    public void testBadFileFormat() {
        Arguments arguments = new RunSqlArguments();
        arguments.parse(new String[]{"--fileformat", "x"});
        assertThrows(RunSqlParseException.class,
                     () -> RunSqlProcessorFactory.createProcessor(arguments, null));
    }
}
