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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Defines SQL statements that should run before or after one or all of the main statements.
 * Primarily used to commit transactions and send batches.
 */
public interface SqlStatementExecutionHook {
    /**
     * Executes at the end of every executed Sql statement.
     *
     * @param connection Database connection.
     * @param statement  The database statement.
     * @throws SQLException For database access errors.
     */
    void afterStatement(final Connection connection, final Statement statement) throws SQLException;

    /**
     * Executes at the end of all executed Sql statements.
     *
     * @param connection Database connection.
     * @param statement  The database statement.
     * @throws SQLException For database access errors.
     */
    void afterAllStatements(final Connection connection,
                            final Statement statement) throws SQLException;

    /**
     * Executes before every executed Sql statement.
     *
     * @param connection Database connection.
     * @param statement  The database statement.
     */
    void beforeStatement(final Connection connection,
                         final Statement statement);

    /**
     * Executes before any Sql statements.
     *
     * @param connection Database connection.
     * @param statement  The database statement.
     */
    void beforeAllStatements(final Connection connection,
                             final Statement statement);

    /**
     * Creates a fresh copy of this object. A fresh copy means a new instance with the same initial
     * values but reset state.
     *
     * @return A new instance based on the object.
     */
    default SqlStatementExecutionHook createFreshCopy() {
        throw new UnsupportedOperationException();
    }
}
