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

import runsql.impl.exceptions.RunSqlParseException;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Used to specify the transaction mode when executing sql statements.
 * <p>
 * AUTO: Commit the transaction after every statement.
 * <p>
 * ROLLBACK: Issue a rollback after all statements.
 * <p>
 * N: Commit after running the specified number of statements.
 */
public enum TransactionMode {
    AUTO, ROLLBACK, N;

    public static TransactionMode parseTransactionMode(
            final String transactionMode) throws RunSqlParseException {
        if (transactionMode == null) {
            return null;
        }
        String transactionModeTrimmed = transactionMode.trim();
        if ("auto".equalsIgnoreCase(transactionModeTrimmed)) {
            return AUTO;
        }
        if ("rollback".equalsIgnoreCase(transactionModeTrimmed)) {
            return ROLLBACK;
        }
        if (NumberUtils.isDigits(transactionModeTrimmed)) {
            return N;
        }
        throw new RunSqlParseException("Invalid transaction mode: " + transactionModeTrimmed, null);
    }
}
