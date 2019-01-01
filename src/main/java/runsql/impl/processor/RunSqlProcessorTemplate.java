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

import runsql.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * This template defines base methods shared by many RunSql processors.
 */
public abstract class RunSqlProcessorTemplate implements Processor {
    private static final Logger LOGGER = LogManager.getLogger();
    protected ResultSetMetaData resultSetMetaData;
    protected ResultSet resultSet;
    protected int columnCount;
    protected int currentColumnNumber;
    protected int currentRowNumber;
    protected int currentSqlType;

    @Override
    public synchronized void process(
            final ResultSet resultSet) throws SQLException, ClassNotFoundException {
        this.resultSet = resultSet;
        resultSetMetaData = resultSet.getMetaData();
        columnCount = resultSetMetaData.getColumnCount();
        currentColumnNumber = 0;
        currentRowNumber = 0;
        processPreRows();
        processRows();
        processPostRows();
    }

    protected void processPreRows() throws SQLException, ClassNotFoundException {
        //
    }

    protected void processRows() throws SQLException {
        while (resultSet.next()) {
            processPreRow();
            processRow();
            processPostRow();
            currentRowNumber++;
        }
    }

    protected void processPostRows() throws SQLException {
        resultSet.close();
    }

    protected void processPreRow() throws SQLException {
        //
    }

    protected void processRow() throws SQLException {
        for (int i = 0; i < columnCount; i++) {
            currentColumnNumber = i + 1;
            currentSqlType = resultSetMetaData.getColumnType(currentColumnNumber);
            processColumn();
        }
    }

    protected void processPostRow() throws SQLException {
        //
    }

    protected abstract void processColumn() throws SQLException;
}
