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

import runsql.impl.RunSqlImpl;
import runsql.impl.exceptions.RunSqlParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main entry point class for running RunSQL from the command line.
 */
public class RunSqlMain {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(final String[] args) throws RunSqlParseException {
        LOGGER.trace("RunSql Main begin.");
        int returnStatus = callRunSql(args);
        LOGGER.trace("RunSql Main end.");
        System.exit(returnStatus);
    }

    public static int callRunSql(final String[] args) throws RunSqlParseException {
        RunSql runSql = new RunSqlImpl();
        return runSql.run(args);
    }
}
