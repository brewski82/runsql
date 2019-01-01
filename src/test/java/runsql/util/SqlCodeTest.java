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

package runsql.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SqlCodeTest {
    @Test
    public void testOneColumnNoNames() {
        String result = SqlCode.createInsertSql("table", null, 1);
        assertEquals("insert into table values (?)", result);
    }

    @Test
    public void testMultipleColumnsNoNames() {
        String result = SqlCode.createInsertSql("table", null, 3);
        assertEquals("insert into table values (?, ?, ?)", result);
    }

    @Test
    public void testOneColumnNames() {
        String result = SqlCode.createInsertSql("table", new String[]{"col1"}, 1);
        assertEquals("insert into table (col1) values (?)", result);
    }

    @Test
    public void testMultipleColumnsNames() {
        String result = SqlCode.createInsertSql("table", new String[]{"col1", "col2"}, 2);
        assertEquals("insert into table (col1, col2) values (?, ?)", result);
    }

    @Test
    public void testBadTableName() {
        assertThrows(AssertionError.class, () -> SqlCode.createInsertSql(null, null, 1));
    }

    @Test
    public void testBadNumberOfColumnsToInsert() {
        assertThrows(AssertionError.class, () -> SqlCode.createInsertSql("table", null, 0));
    }

    @Test
    public void testBadNumberOfColumnsToInsertWithColName() {
        assertThrows(AssertionError.class,
                     () -> SqlCode.createInsertSql("table", new String[]{"col1", "col2"}, 3));
    }
}
