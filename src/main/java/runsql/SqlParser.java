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

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Parses Sql statements from a variety of Reader sources. The parser handles SQL comments and
 * optionally splitting up multiple sql statements.
 */
public interface SqlParser {
    List<Reader> getReaders();

    void addReader(Reader reader);

    String nextSqlStatement() throws IOException;

    /**
     * Closes all Reader objects.
     */
    void close();
}
