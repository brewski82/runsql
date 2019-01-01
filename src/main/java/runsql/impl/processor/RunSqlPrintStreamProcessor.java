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

import runsql.util.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.PrintStream;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

/**
 * The main {@link runsql.Processor} for printing results to a file or screen.
 */
public class RunSqlPrintStreamProcessor extends RunSqlProcessorTemplate {
    protected final PrintStream printStream;
    protected final String nullValue;
    protected final String columnSeparator;
    protected final String rowSeparator;
    protected final String quoteString;
    protected final String escapeString;
    protected final String trueValue;
    protected final String falseValue;
    protected final boolean outputColumnNames;
    private final QuoteMode quoteMode;

    public RunSqlPrintStreamProcessor(final Builder builder) {
        printStream = builder.printStream;
        nullValue = builder.nullValue;
        columnSeparator = builder.columnSeparator;
        rowSeparator = builder.rowSeparator;
        quoteString = builder.quoteString;
        escapeString = builder.escapeString;
        outputColumnNames = builder.outputColumnNames;
        quoteMode = builder.quoteMode;
        trueValue = builder.trueValue;
        falseValue = builder.falseValue;
    }

    @Override
    protected void processPreRows() throws SQLException {
        if (outputColumnNames) {
            for (int i = 1; i <= columnCount; i++) {
                currentColumnNumber = i;
                printString(resultSetMetaData.getColumnName(i));
                printColumnSeparator();
            }
            processPostRow();
        }
    }

    @Override
    protected void processPostRow() throws SQLException {
        super.processPostRow();
        print(rowSeparator);
    }

    @Override
    protected void processColumn() throws SQLException {
        Object object = resultSet.getObject(currentColumnNumber);
        if (object == null) {
            printNullValue();
        } else {
            switch (currentSqlType) {
                case Types.BOOLEAN:
                case Types.BIT:
                    printString(resultSet.getBoolean(currentColumnNumber) ? trueValue : falseValue);
                    break;
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    printString(Arrays.toString(resultSet.getBytes(currentColumnNumber)));
                    break;
                default:
                    printString(object.toString());
            }
        }
        printColumnSeparator();
    }

    protected void printNullValue() {
        print(nullValue);
    }

    void printString(final String string) {
        boolean printQuotes = false;
        switch (quoteMode) {
            case NEVER:
                printQuotes = false;
                break;
            case ALWAYS:
                printQuotes = StringUtils.isNotEmpty(quoteString);
                break;
            case NECESSARY:
                if (StringUtils.isNotEmpty(quoteString) && StringUtils
                        .isNotEmpty(columnSeparator)) {
                    printQuotes = StringUtils.contains(string, columnSeparator);
                }
                break;
            case TEXT:
                printQuotes = !NumberUtils.isParsable(string);
                break;
        }
        if (printQuotes) {
            print(quoteString);
            if (StringUtils.contains(string, quoteString) && StringUtils.isNotEmpty(escapeString)) {
                print(StringUtils.replace(string, quoteString, escapeString + quoteString));
            } else {
                print(string);
            }
            print(quoteString);
        } else {
            String stringToPrint = string;
            if (StringUtils.isNotEmpty(escapeString)) {
                if (StringUtils.contains(stringToPrint, columnSeparator)) {
                    stringToPrint = StringUtils.replace(stringToPrint, columnSeparator,
                                                        escapeString + columnSeparator);
                }
                if (quoteMode != QuoteMode.NEVER && StringUtils
                        .isNotEmpty(quoteString) && StringUtils
                        .contains(stringToPrint, quoteString)) {
                    stringToPrint = StringUtils
                            .replace(stringToPrint, quoteString, escapeString + quoteString);
                }
            }
            print(stringToPrint);
        }
    }

    protected void printColumnSeparator() {
        if (currentColumnNumber < columnCount) {
            print(columnSeparator);
        }
    }

    protected void print(final Object object) {
        printStream.print(object);
    }

    public static class Builder {
        private PrintStream printStream;
        private String nullValue;
        private String columnSeparator;
        private String rowSeparator;
        private String quoteString;
        private String escapeString;
        private boolean outputColumnNames;
        private QuoteMode quoteMode;
        private String trueValue;
        private String falseValue;

        public Builder printStream(final PrintStream printStream) {
            this.printStream = printStream;
            return this;
        }

        public Builder nullValue(final String nullValue) {
            this.nullValue = nullValue;
            return this;
        }

        public Builder columnSeparator(final String columnSeparator) {
            this.columnSeparator = columnSeparator;
            return this;
        }

        public Builder rowSeparator(final String rowSeparator) {
            this.rowSeparator = rowSeparator;
            return this;
        }

        public Builder quoteString(final String quoteString) {
            this.quoteString = quoteString;
            return this;
        }

        public Builder escapeString(final String escapeString) {
            this.escapeString = escapeString;
            return this;
        }

        public Builder outputColumnNames(final boolean outputColumnNames) {
            this.outputColumnNames = outputColumnNames;
            return this;
        }

        public Builder quoteMode(final QuoteMode quoteMode) {
            this.quoteMode = quoteMode;
            return this;
        }

        public Builder trueValue(final String trueValue) {
            this.trueValue = trueValue;
            return this;
        }

        public Builder falseValue(final String falseValue) {
            this.falseValue = falseValue;
            return this;
        }

        public RunSqlPrintStreamProcessor build() {
            return new RunSqlPrintStreamProcessor(this);
        }
    }
}
