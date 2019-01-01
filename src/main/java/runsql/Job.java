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
 * Holds information to execute a job.
 * <p>
 * {@link JobProducer}s create Job instances and put them on the {@link JobQueue} for one or more
 * {@link JobConsumer}s.
 */
public interface Job {
    /**
     * Each job must contain SQL to run.
     *
     * @return The sql to be executed.
     */
    default String getSql() {
        return "";
    }

    /**
     * The Processor that processes the results of the executed SQL.
     *
     * @return The processor which handles the result of executing the sql.
     */
    default Processor getProcessor() {
        return null;
    }
}
