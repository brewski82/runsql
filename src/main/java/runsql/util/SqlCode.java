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

/**
 * Utilities for generating SQL code.
 */
public final class SqlCode {
    public static String createInsertSql(final String insertTableName,
                                         final String[] insertColumnNames,
                                         final int numberOfColumnsToInsert) {
        assert numberOfColumnsToInsert > 0;
        assert insertTableName != null;
        StringBuilder stringBuilder = new StringBuilder("insert into ");
        stringBuilder.append(insertTableName);
        stringBuilder.append(" ");
        if (insertColumnNames != null) {
            assert insertColumnNames.length == numberOfColumnsToInsert;
            stringBuilder.append("(");
            for (int i = 0; i < insertColumnNames.length; i++) {
                stringBuilder.append(insertColumnNames[i]);
                if (i < insertColumnNames.length - 1) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(") ");
        }
        stringBuilder.append("values (");
        for (int i = 0; i < numberOfColumnsToInsert; i++) {
            stringBuilder.append("?");
            if (i < numberOfColumnsToInsert - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
