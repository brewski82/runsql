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

package runsql;

import runsql.impl.arguments.Parameter;
import runsql.impl.exceptions.RunSqlParseException;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigInteger;

/**
 * Parses command line arguments and provides the means to inspect them.
 */
public interface Arguments {
    /**
     * Converts a command line argument to a Boolean.
     *
     * @param booleanString The input string to convert to a boolean
     * @return a boolean or null if the input string is null
     * @throws RunSqlParseException If the class cannot parse the argument
     */
    static Boolean parseBoolean(final String booleanString) {
        if (booleanString == null) {
            return null;
        }
        if ("t".equalsIgnoreCase(booleanString.trim())) {
            return true;
        }
        if ("f".equalsIgnoreCase(booleanString.trim())) {
            return false;
        }
        throw new RunSqlParseException("Unrecognized boolean option: " + booleanString, null);
    }

    /**
     * Parses a positive number.
     *
     * @param integerString The number to parse.
     * @return The parsed number.
     * @throws RunSqlParseException When the supplied string is not a positive number.
     */
    static BigInteger parsePositiveInteger(final String integerString) {
        if (integerString == null) {
            return null;
        }
        if (NumberUtils.isDigits(integerString)) {
            BigInteger bigInteger = NumberUtils.createBigInteger(integerString);
            if (bigInteger.signum() == 1) {
                return bigInteger;
            }
        }
        throw new RunSqlParseException("Invalid number: " + integerString, null);
    }

    /**
     * Parses a number.
     *
     * @param integerString The number to parse.
     * @return The parsed number.
     * @throws RunSqlParseException When the supplied string is not a number.
     */
    static BigInteger parseInteger(final String integerString) throws RunSqlParseException {
        if (integerString == null) {
            return null;
        }
        if (NumberUtils.isDigits(integerString)) {
            return NumberUtils.createBigInteger(integerString);
        }
        throw new RunSqlParseException("Invalid number: " + integerString, null);
    }

    /**
     * Parses the command line arguments. After parsing, you can query the arguments.
     *
     * @param args The command line arguments to parse
     * @throws RunSqlParseException When the user passes a bad argument.
     */
    void parse(String[] args);

    /**
     * Returns the argument for {@link Parameter}, if any, as a String.
     *
     * @param parameter The parameter to get the value for.
     * @return The value for the given {@link Parameter}.
     */
    String getValue(String parameter);

    /**
     * Returns the arguments for {@link Parameter}, if any, as an array of Strings.
     *
     * @param parameter The parameter to get the values for.
     * @return A String array of values for the given {@link Parameter}.
     */
    String[] getValues(String parameter);

    /**
     * Determines if an argument was provided for {@link Parameter}.
     *
     * @param parameter The parameter to check.
     * @return True if an argument was provided for the {@link Parameter}, else false
     */
    boolean hasOption(String parameter);
}
