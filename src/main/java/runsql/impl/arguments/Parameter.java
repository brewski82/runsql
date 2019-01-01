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
 * Defines all parameters for RunSQL.
 */
public enum Parameter {
    URL("U", "url", null),
    USER("u", "user", null),
    PASSWORD("p", "password", null),
    DRIVER("d", "driver", null),
    PROPERTIES_FILE_PATH(null, "propertiesfilepath", "file"),
    PROPERTIES_PREFIX(null, "propertiesprefix", null),
    SQL("s", "sql", null),
    OUTPUT_FILE_PATH("o", "outputfilepath", null),
    ROW_SEPARATOR("r", "rowseparator", null),
    COLUMN_SEPARATOR("c", "columnseparator", null),
    HELP("h", "help", null),
    SPLIT_SQL_STATEMENTS(null, "splitsqlstatements", "t|f"),
    STATEMENT_SEPARATOR(null, "sqlstatementseparator", null),
    STRIP_COMMENTS(null, "stripcomments", "t|f"),
    VALUE_WHEN_NULL(null, "valuewhennull", null),
    QUOTE_VALUE(null, "quotevalue", null),
    ESCAPE_CHARACTER(null, "escapecharacter", null),
    INCLUDE_HEADERS(null, "includeheaders", "t|f"),
    INPUT_FILE_PATH("i", "inputfilepath", "file"),
    FILE_FORMAT("f", "fileformat", null),
    TABLE_NAME(null, "tablename", null),
    ECHO_SQL(null, "echosql", "file"),
    QUOTE_MODE(null, "quotemode", null),
    BATCH_SIZE(null, "batchsize", "N"),
    TRANSACTION_MODE(null, "transactionmode", null),
    BOOLEAN_TRUE_VALUE(null, "booleantruevalue", null),
    BOOLEAN_FALSE_VALUE(null, "booleanfalsevalue", null),
    IMPORT_TABLE(null, "importtable", null),
    IMPORT_COLUMNS(null, "importcolumns", null),
    IMPORT_DRIVER(null, "importdriver", null),
    IMPORT_URL(null, "importurl", null),
    IMPORT_USER(null, "importuser", null),
    IMPORT_PASSWORD(null, "importpassword", null),
    RESULT_SET_FETCH_SIZE(null, "resultsetfetchsize", null),
    IMPORT_PROPERTIES_FILE_PATH(null, "importpropertiesfilepath", "file"),
    IMPORT_PROPERTIES_PREFIX(null, "importpropertiesprefix", null),
    NUMBER_OF_JOBS(null, "numberofjobs", "N");
    private final String name;
    private final String longName;
    private final String argName;

    Parameter(final String name, final String longName, final String argName) {
        this.name = name;
        this.longName = longName;
        this.argName = argName;
    }

    public static Parameter getParameter(final String parameterLongOrShortName) {
        for (Parameter parameter : Parameter.values()) {
            if (parameterLongOrShortName.equals(parameter.name) || parameterLongOrShortName
                    .equals(parameter.longName)) {
                return parameter;
            }
        }
        throw new RuntimeException("Unknown parameter: " + parameterLongOrShortName);
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        return longName;
    }

    public String getArgName() {
        return argName;
    }

    public String getEitherName() {
        return name == null ? longName : name;
    }
}
