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

/**
 * Main interface for RunSql.
 */
@FunctionalInterface
public interface RunSql {
    /**
     * Main entrypoint into RunSql.
     *
     * @param args Command line args.
     * @return Zero if successful, non-zero otherwise.
     */
    int run(String[] args);
}
