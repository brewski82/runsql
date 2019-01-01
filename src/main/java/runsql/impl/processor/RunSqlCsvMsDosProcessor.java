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

import org.apache.commons.lang3.math.NumberUtils;

import java.sql.Types;

/**
 * A special case {@link runsql.Processor} for generating ms-dos compatible csv files. It
 * prefixes the equal symbol for numbers which should display leading zeroes. While useful for
 * viewing data it is best not to use this format for machine consumption.
 */
public class RunSqlCsvMsDosProcessor extends RunSqlPrintStreamProcessor {

    public RunSqlCsvMsDosProcessor(final Builder builder) {
        super(builder);
    }

    @Override
    protected void printString(final String string) {
        if (isStringSqlType() && NumberUtils.isParsable(string)) {
            print("=" + quoteString + string + quoteString);
        } else {
            super.printString(string);
        }
    }

    boolean isStringSqlType() {
        switch (currentSqlType) {
            case Types.LONGVARCHAR:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return true;
            default:
                return false;
        }
    }

    public static class Builder extends RunSqlPrintStreamProcessor.Builder {
        @Override
        public RunSqlCsvMsDosProcessor build() {
            return new RunSqlCsvMsDosProcessor(this);
        }
    }
}
