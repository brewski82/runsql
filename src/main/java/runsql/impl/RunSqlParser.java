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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class RunSqlParser implements SqlParser {
    private static final char[] SINGLE_LINE_COMMENT = {'-', '-'};
    private static final char[] MULTI_LINE_COMMENT_START = {'/', '*'};
    private static final char[] MULTI_LINE_COMMENT_END = {'*', '/'};
    private final boolean stripComments;
    private final boolean keepStatementsTogether;
    private final char[] sqlSeparator;
    private final List<Reader> readers = new ArrayList<>();
    private Reader reader;
    private int readerIndex;
    private int aInt;
    private int sqlSeparatorIndex;
    private int multiLineCommentIndex;
    private int singleLineCommentIndex;
    private char aChar;
    private char quoteChar;
    private boolean isConstituentCharacter = true; // i.e. non-escape char
    private StringBuilder stringBuilder;

    public RunSqlParser(final boolean stripComments, final char[] sqlSeparator,
                        final boolean separateStatements) {
        this.stripComments = stripComments;
        this.sqlSeparator = sqlSeparator;
        keepStatementsTogether = !separateStatements;
    }

    @Override
    public List<Reader> getReaders() {
        return readers;
    }

    @Override
    public void addReader(final Reader reader) {
        readers.add(reader);
    }

    @Override
    public String nextSqlStatement() throws IOException {
        // Check if we are done reading sql from our list of Readers.
        if (isCurrentReaderDone()) {
            readerIndex++;
        }
        if (readerIndex >= readers.size()) {
            return null;
        }
        reader = readers.get(readerIndex);
        // If we do not have to split up multiple sql statements, simply return the contents of
        // the reader.
        if (keepStatementsTogether) {
            readerIndex++;
            return IOUtils.toString(reader);
        }
        aInt = 0;
        sqlSeparatorIndex = 0;
        multiLineCommentIndex = 0;
        singleLineCommentIndex = 0;
        stringBuilder = new StringBuilder();
        boolean noStatements = true;
        while ((aInt = reader.read()) > 0) {
            aChar = (char) aInt;
            if (isPossibleMultiLineCommentStart()) {
                multiLineCommentIndex++;
            } else {
                multiLineCommentIndex = 0;
            }
            if (isPossibleSingleLineCommentStart()) {
                singleLineCommentIndex++;
            } else {
                singleLineCommentIndex = 0;
            }
            if (isMultiLineCommentStart()) {
                readToEndOfMultiLineComment();
                continue;
            }
            if (isSingleLineCommentStart()) {
                readToEndOfSingleLineComment();
                continue;
            }
            if (isPossibleSqlSeparator()) {
                sqlSeparatorIndex++;
            } else {
                sqlSeparatorIndex = 0;
            }
            if (isSqlSeparator()) {
                stringBuilder.delete(stringBuilder.length() - (sqlSeparator.length - 1),
                                     stringBuilder.length());
                break;
            }
            if (isSqlCode()) {
                noStatements = false;
            }
            if (isQuoteStart()) {
                readToEndOfQuote();
                continue;
            }
            stringBuilder.append(aChar);
        }
        if (isCurrentReaderDone()) {
            reader.close();
            if (!foundSql()) {
                return nextSqlStatement();
            }
        }
        if (StringUtils.isWhitespace(stringBuilder) || noStatements) {
            return null;
        }
        return stringBuilder.toString();
    }

    @Override
    public void close() {
        for (Reader r : readers) {
            try {
                r.close();
            } catch (final IOException e) {
                // Ignore
            }
        }
    }

    private boolean isSqlCode() {
        if (multiLineCommentIndex > 0) {
            return false;
        }
        if (singleLineCommentIndex > 0) {
            return false;
        }
        if (sqlSeparatorIndex > 0) {
            return false;
        }
        return !Character.isWhitespace(aChar);
    }

    private boolean isPossibleMultiLineCommentStart() {
        return aChar == MULTI_LINE_COMMENT_START[multiLineCommentIndex];
    }

    private boolean isMultiLineCommentStart() {
        return multiLineCommentIndex > 0 && aChar == MULTI_LINE_COMMENT_START[multiLineCommentIndex - 1] && multiLineCommentIndex == MULTI_LINE_COMMENT_START.length;
    }

    private boolean isPossibleMultiLineCommentEnd() {
        return aChar == MULTI_LINE_COMMENT_END[multiLineCommentIndex];
    }

    private boolean isMultiLineCommentEnd() {
        return multiLineCommentIndex > 0 && aChar == MULTI_LINE_COMMENT_END[multiLineCommentIndex - 1] && multiLineCommentIndex == MULTI_LINE_COMMENT_END.length;
    }

    private boolean isPossibleSingleLineCommentStart() {
        return aChar == SINGLE_LINE_COMMENT[singleLineCommentIndex];
    }

    private boolean isSingleLineCommentStart() {
        return singleLineCommentIndex > 0 && aChar == SINGLE_LINE_COMMENT[singleLineCommentIndex - 1] && singleLineCommentIndex == SINGLE_LINE_COMMENT.length;
    }

    private boolean isSingleLineCommentEnd() {
        return aChar == '\n';
    }

    private boolean isPossibleSqlSeparator() {
        return aChar == sqlSeparator[sqlSeparatorIndex];
    }

    private boolean isSqlSeparator() {
        return sqlSeparatorIndex > 0 && aChar == sqlSeparator[sqlSeparatorIndex - 1] && sqlSeparatorIndex == sqlSeparator.length;
    }

    private boolean isCurrentReaderDone() {
        return aInt < 0;
    }

    private boolean foundSql() {
        return stringBuilder.length() > 0;
    }

    private void readToEndOfMultiLineComment() throws IOException {
        if (stripComments) {
            stringBuilder.delete(stringBuilder.length() - (MULTI_LINE_COMMENT_START.length - 1),
                                 stringBuilder.length());
        } else {
            stringBuilder.append(aChar);
        }
        multiLineCommentIndex = 0;
        while ((aInt = reader.read()) > 0) {
            aChar = (char) aInt;
            if (!stripComments) {
                stringBuilder.append(aChar);
            }
            if (isPossibleMultiLineCommentEnd()) {
                multiLineCommentIndex++;
            } else {
                multiLineCommentIndex = 0;
            }
            if (isMultiLineCommentEnd()) {
                multiLineCommentIndex = 0;
                return;
            }
        }
    }

    private void readToEndOfSingleLineComment() throws IOException {
        if (stripComments) {
            stringBuilder.delete(stringBuilder.length() - (SINGLE_LINE_COMMENT.length - 1),
                                 stringBuilder.length());
        } else {
            stringBuilder.append(aChar);
        }
        singleLineCommentIndex = 0;
        while ((aInt = reader.read()) > 0) {
            aChar = (char) aInt;
            if (!stripComments || isSingleLineCommentEnd()) {
                stringBuilder.append(aChar);
            }
            if (isSingleLineCommentEnd()) {
                singleLineCommentIndex = 0;
                return;
            }
        }
    }

    private boolean isQuoteStart() {
        if (aChar == '\'' || aChar == '"') {
            quoteChar = aChar;
            return true;
        }
        return false;
    }

    private void readToEndOfQuote() throws IOException {
        stringBuilder.append(aChar);
        while ((aInt = reader.read()) > 0) {
            aChar = (char) aInt;
            stringBuilder.append(aChar);
            if (aChar == quoteChar && isConstituentCharacter) {
                return;
            }
            isConstituentCharacter = !isEscapeCharacter();
        }
    }

    private boolean isEscapeCharacter() {
        return quoteChar == '"' && aChar == '\\';
    }
}
