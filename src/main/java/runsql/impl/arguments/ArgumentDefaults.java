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

/**
 * Default values for command line arguments.
 */
public class ArgumentDefaults {
    public static final String DEFAULT_ROW_SEPARATOR = System.lineSeparator();
    public static final String DEFAULT_COLUMN_SEPARATOR = "\t";
    public static final String DEFAULT_SQL_STATEMENT_SEPARATOR = ";";
    public static final String DEFAULT_VALUE_WHEN_NULL = "null";
    public static final String DEFAULT_ESCAPE_CHARACTER = "\\";
    public static final String DEFAULT_SPLIT_SQL_STATEMENTS = "t";
    public static final String DEFAULT_STRIP_COMMENTS = "t";
    public static final String OUTPUT_COLUMN_NAMES = "f";
    public static final String DEFAULT_FILE_FORMAT = "none";
    public static final String DEFAULT_QUOTE_MODE = "necessary";
    public static final String DEFAULT_BATCH_SIZE = "1";
    public static final String DEFAULT_TRANSACTION_MODE = "auto";
    public static final String DEFAULT_BOOLEAN_TRUE_VALUE = "true";
    public static final String DEFAULT_BOOLEAN_FALSE_VALUE = "false";
    public static final String DEFAULT_RESULT_SET_FETCH_SIZE = "0";
    public static final String DEFAULT_NUMBER_OF_JOBS = "1";
}
