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

package runsql.impl;

import runsql.Job;
import runsql.JobProducer;
import runsql.JobQueue;
import runsql.Processor;
import runsql.SqlParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.stream.IntStream;

public class RunSqlJobProducer implements JobProducer {
    public static final Job POISON_PILL = new RunSqlJob(null, null);
    private static final Logger LOGGER = LogManager.getLogger();
    private final SqlParser sqlParser;
    private final Processor processor;
    private final JobQueue jobQueue;
    private final int numberOfJobConsumers;

    public RunSqlJobProducer(final SqlParser sqlParser, final Processor processor,
                             final JobQueue jobQueue, final int numberOfJobConsumers) {
        this.sqlParser = sqlParser;
        this.processor = processor;
        this.jobQueue = jobQueue;
        this.numberOfJobConsumers = numberOfJobConsumers;
    }

    @Override
    public void run() {
        produceJobs();
    }

    @Override
    public void produceJobs() {
        try {
            String sql = sqlParser.nextSqlStatement();
            int i = 0;
            while (sql != null) {
                LOGGER.trace("Add sql statement number " + ++i + ", " + sql);
                jobQueue.addJob(new RunSqlJob(processor, sql));
                sql = sqlParser.nextSqlStatement();
            }
            sqlParser.close();
            IntStream.range(0, numberOfJobConsumers).forEach($ -> jobQueue.addJob(POISON_PILL));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
