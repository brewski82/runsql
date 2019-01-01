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

import java.sql.SQLException;

/**
 * A {@link runsql.Processor} that generates insert statements based on the result set.
 */
public class RunSqlInsertsProcessor extends RunSqlPrintStreamProcessor {
    protected final String tableName;

    public RunSqlInsertsProcessor(final Builder builder) {
        super(builder);
        tableName = builder.tableName;
    }

    @Override
    protected void processPreRow() throws SQLException {
        printStream.print("insert into " + tableName + " (");
        for (int i = 1; i <= columnCount; i++) {
            currentColumnNumber = i;
            printStream.print(resultSetMetaData.getColumnName(i));
            printColumnSeparator();
        }
        printStream.print(") values (");
    }

    @Override
    protected void processPostRow() {
        printStream.print(");");
        printStream.print(rowSeparator);
    }

    public static class Builder extends RunSqlPrintStreamProcessor.Builder {
        private String tableName;

        public Builder tableName(final String tableName) {
            this.tableName = tableName;
            return this;
        }

        @Override
        public RunSqlInsertsProcessor build() {
            return new RunSqlInsertsProcessor(this);
        }
    }
}
