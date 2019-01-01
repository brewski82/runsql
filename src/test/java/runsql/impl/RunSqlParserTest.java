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

import runsql.SqlParser;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RunSqlParserTest {
    @Test
    public void testSimpleSqlStatement() throws Exception {
        StringReader stringReader = new StringReader("select * from table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
    }

    @Test
    public void testEmbeddedComment() throws Exception {
        StringReader stringReader = new StringReader("select * from /* comment */ table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from  table", string);
    }

    @Test
    public void testDoNotStripEmbeddedComment() throws Exception {
        StringReader stringReader = new StringReader("select * from /* comment */ table;");
        SqlParser sqlParser = new RunSqlParser(false, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from /* comment */ table", string);
    }

    @Test
    public void testTwoSqlStatements() throws Exception {
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(new StringReader("select * from table_1;select * from table_2;"));
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table_1", string);
        String string2 = sqlParser.nextSqlStatement();
        assertEquals("select * from table_2", string2);
    }

    @Test
    public void testTwoSqlReadersWithTwoStatements() throws Exception {
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(new StringReader("select * from table_1;select * from table_2;"));
        sqlParser.addReader(new StringReader("select * from table_3;select * from table_4;"));
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table_1", string);
        String string2 = sqlParser.nextSqlStatement();
        assertEquals("select * from table_2", string2);
        String string3 = sqlParser.nextSqlStatement();
        assertEquals("select * from table_3", string3);
        String string4 = sqlParser.nextSqlStatement();
        assertEquals("select * from table_4", string4);
        String string5 = sqlParser.nextSqlStatement();
        assertNull(string5);
    }

    @Test
    public void testTwoSqlReaders() throws Exception {
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(new StringReader("select * from table_1;"));
        sqlParser.addReader(new StringReader("select * from table_2;"));
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table_1", string);
        String string2 = sqlParser.nextSqlStatement();
        assertEquals("select * from table_2", string2);
    }

    @Test
    public void testTwoSqlReadersNo() throws Exception {
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(new StringReader("select * from table_1;"));
        sqlParser.addReader(new StringReader("select * from table_2;"));
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table_1", string);
        String string2 = sqlParser.nextSqlStatement();
        assertEquals("select * from table_2", string2);
    }

    @Test
    public void testSimpleSqlStatementNoSemiColon() throws Exception {
        StringReader stringReader = new StringReader("select * from table");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
    }

    @Test
    public void testMultipleSqlStatementNoSemiColon() throws Exception {
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(new StringReader("select * from table_1"));
        sqlParser.addReader(new StringReader("select * from table_2"));
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table_1", string);
        String string2 = sqlParser.nextSqlStatement();
        assertEquals("select * from table_2", string2);
    }

    @Test
    public void testNoStatements() throws Exception {
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        String string = sqlParser.nextSqlStatement();
        assertNull(string);
    }

    @Test
    public void testStrangeSeparator() throws Exception {
        StringReader stringReader = new StringReader("select * from table$$$$");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{'$', '$', '$', '$'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
    }

    @Test
    public void testMultiLineComment() throws Exception {
        StringReader stringReader =
                new StringReader("select * from /* comment \n on \n multiple \n lines \n*/ table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from  table", string);
    }

    @Test
    public void testMultiLineCommentWithMultiLineComment() throws Exception {
        StringReader stringReader = new StringReader(
                "select * from /* comment \n on; \n multiple \n /****** lines\n*\n/ \n*/ table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from  table", string);
    }

    @Test
    public void testEndWithNull() throws Exception {
        StringReader stringReader = new StringReader("select * from table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testEndWithNullNoSeparator() throws Exception {
        StringReader stringReader = new StringReader("select * from table");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testEndWithNullMultipleStatements() throws Exception {
        StringReader stringReader = new StringReader("select * from table;select 1;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        sqlParser.nextSqlStatement();
        sqlParser.nextSqlStatement();
        String string = sqlParser.nextSqlStatement();
        assertNull(string);
    }

    @Test
    public void testMultiLineCommentAtEnd() throws Exception {
        StringReader stringReader = new StringReader("select * from table;/* end comment */");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testMultiLineCommentAtEndWhitespace() throws Exception {
        StringReader stringReader = new StringReader("select * from table; /* end comment */");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testMultiLineCommentAtEndWhitespaceNoStrip() throws Exception {
        StringReader stringReader = new StringReader("select * from table; /* end comment */");
        SqlParser sqlParser = new RunSqlParser(false, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSimpleSingleLineComment() throws Exception {
        StringReader stringReader = new StringReader("select * from table;-- comment");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSimpleSingleLineCommentWithSpace() throws Exception {
        StringReader stringReader = new StringReader("select * from table; -- comment");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSimpleSingleLineCommentNoSeparator() throws Exception {
        StringReader stringReader = new StringReader("select * from table-- comment");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSimpleSingleLineCommenNoSeparatortWithSpace() throws Exception {
        StringReader stringReader = new StringReader("select * from table -- comment");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table ", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSimpleSingleLineCommentNoStrip() throws Exception {
        StringReader stringReader = new StringReader("select * from table;-- comment");
        SqlParser sqlParser = new RunSqlParser(false, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSimpleSingleLineCommentWithSpaceNoStrip() throws Exception {
        StringReader stringReader = new StringReader("select * from table; -- comment");
        SqlParser sqlParser = new RunSqlParser(false, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSimpleSingleLineCommentNoSeparatorNoStrip() throws Exception {
        StringReader stringReader = new StringReader("select * from table-- comment");
        SqlParser sqlParser = new RunSqlParser(false, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table-- comment", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSimpleSingleLineCommentNoSeparatortWithSpaceNoStrip() throws Exception {
        StringReader stringReader = new StringReader("select * from table -- comment");
        SqlParser sqlParser = new RunSqlParser(false, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table -- comment", string);
        String string2 = sqlParser.nextSqlStatement();
        assertNull(string2);
    }

    @Test
    public void testSingleLineCommentWithNewLine() throws Exception {
        StringReader stringReader = new StringReader("select * -- a comment \nfrom table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * \nfrom table", string);
    }

    @Test
    public void testSingleLineCommentWithNewLineAndNoSpace() throws Exception {
        StringReader stringReader = new StringReader("select *-- a comment \nfrom table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select *\nfrom table", string);
    }

    @Test
    public void testMixedComments() throws Exception {
        StringReader stringReader =
                new StringReader("select *-- a comment /* more */ -- \nfrom table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select *\nfrom table", string);
    }

    @Test
    public void testMixedCommentsNoStrip() throws Exception {
        StringReader stringReader =
                new StringReader("select *-- a comment /* more */ -- \nfrom table;");
        SqlParser sqlParser = new RunSqlParser(false, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select *-- a comment /* more */ -- \nfrom table", string);
    }

    @Test
    public void testMixedCommentsNoStripWithSeparator() throws Exception {
        StringReader stringReader =
                new StringReader("select *-- a comment /* more; */ -- \nfrom table;");
        SqlParser sqlParser = new RunSqlParser(false, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select *-- a comment /* more; */ -- \nfrom table", string);
    }

    @Test
    public void testNoSplit() throws Exception {
        StringReader stringReader =
                new StringReader("select * from table;/* comment */select * from sometable;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, false);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table;/* comment */select * from sometable;", string);
    }

    @Test
    public void testNoSplitMultipleReaders() throws Exception {
        StringReader stringReader =
                new StringReader("select * from table;/* comment */select * from sometable;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, false);
        sqlParser.addReader(stringReader);
        sqlParser.addReader(
                new StringReader("select * from table;/* comment */select * from sometable;"));
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from table;/* comment */select * from sometable;", string);
        String string2 = sqlParser.nextSqlStatement();
        assertEquals("select * from table;/* comment */select * from sometable;", string2);
        String string3 = sqlParser.nextSqlStatement();
        assertNull(string3);
    }

    @Test
    public void testReaderCloseNoError() throws Exception {
        StringReader stringReader =
                new StringReader("select * from table;/* comment */select * from sometable;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, false);
        sqlParser.addReader(stringReader);
        List<Reader> readers = sqlParser.getReaders();
        for (Reader r : readers) {
            r.close();
        }
        sqlParser.close();
    }

    @Test
    public void testQuotes() throws Exception {
        StringReader stringReader = new StringReader("select 'hi ; there' from table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select 'hi ; there' from table", string);
    }

    @Test
    public void testQuotesEscaped() throws Exception {
        StringReader stringReader = new StringReader("select 'hi ; ''there' from table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select 'hi ; ''there' from table", string);
    }

    @Test
    public void testQuotesMixed() throws Exception {
        StringReader stringReader = new StringReader("select 'hi ; \"there' from table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select 'hi ; \"there' from table", string);
    }

    @Test
    public void testDoubleQuotes() throws Exception {
        StringReader stringReader = new StringReader("select \"hi ; there\" from table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select \"hi ; there\" from table", string);
    }

    @Test
    public void testDoubleQuotesEscaped() throws Exception {
        StringReader stringReader = new StringReader("select \"hi ; \\\"there\" from table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select \"hi ; \\\"there\" from table", string);
    }

    @Test
    public void testDoubleQuotesMixed() throws Exception {
        StringReader stringReader = new StringReader("select \"hi ; ''there\" from table;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select \"hi ; ''there\" from table", string);
    }

    @Test
    public void testSimpleInsert() throws Exception {
        StringReader stringReader = new StringReader(
                "insert into person values (3, 'firstthree', 'lasttwo', '2000-02-01', true, null);");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{';'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals(
                "insert into person values (3, 'firstthree', 'lasttwo', '2000-02-01', true, null)",
                string);
        String stringNull = sqlParser.nextSqlStatement();
        assertNull(stringNull);
    }

    @Test
    public void testPipeSplit() throws Exception {
        StringReader stringReader =
                new StringReader("select * from person|select last_name from person;");
        SqlParser sqlParser = new RunSqlParser(true, new char[]{'|'}, true);
        sqlParser.addReader(stringReader);
        String string = sqlParser.nextSqlStatement();
        assertEquals("select * from person", string);
        String string2 = sqlParser.nextSqlStatement();
        assertEquals("select last_name from person;", string2);
    }
}
