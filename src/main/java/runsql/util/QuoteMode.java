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

import runsql.impl.arguments.Parameter;
import runsql.impl.exceptions.RunSqlParseException;

/**
 * When printing results, controls when to wrap each value in quotes.
 * <p>
 * ALWAYS will wrap the value in quotes unconditionally.
 * <p>
 * NEVER will never wrap the value in quotes.
 * <p>
 * TEXT will only wrap text values in quotes.
 * <p>
 * NECESSARY will only wrap text values in quotes if they contain the provided column separator.
 */
public enum QuoteMode {
    ALWAYS, NEVER, TEXT, NECESSARY;

    public static QuoteMode getQuoteMode(final String quoteModeString) throws RunSqlParseException {
        for (QuoteMode quoteMode : QuoteMode.values()) {
            if (quoteMode.toString().equalsIgnoreCase(quoteModeString)) {
                return quoteMode;
            }
        }
        throw new RunSqlParseException(
                String.format("Invalid %s: %s.", Parameter.QUOTE_MODE.getName(), quoteModeString),
                null);
    }
}
