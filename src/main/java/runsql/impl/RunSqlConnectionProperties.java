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

import runsql.ConnectionProperties;
import runsql.util.TransactionMode;

import java.math.BigInteger;

public class RunSqlConnectionProperties implements ConnectionProperties {
    private final String url;
    private final String user;
    private final String password;
    private final String driver;
    private final TransactionMode transactionMode;
    private final BigInteger batchSize;
    private final BigInteger transactionSize;
    private final boolean isBatchMode;
    private final int resultSetFetchSize;

    private RunSqlConnectionProperties(final Builder builder) {
        url = builder.url;
        user = builder.user;
        password = builder.password;
        driver = builder.driver;
        transactionMode = builder.transactionMode;
        batchSize = builder.batchSize;
        transactionSize = builder.transactionSize;
        isBatchMode = builder.isBatchMode;
        resultSetFetchSize = builder.resultSetFetchSize;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDriver() {
        return driver;
    }

    @Override
    public TransactionMode getTransactionMode() {
        return transactionMode;
    }

    @Override
    public BigInteger getBatchSize() {
        return batchSize;
    }

    @Override
    public BigInteger getTransactionSize() {
        return transactionSize;
    }

    @Override
    public boolean isBatchMode() {
        return isBatchMode;
    }

    @Override
    public int getResultSetFetchSize() {
        return resultSetFetchSize;
    }

    public static class Builder {
        private String url;
        private String user;
        private String password;
        private String driver;
        private TransactionMode transactionMode;
        private BigInteger batchSize;
        private BigInteger transactionSize;
        private boolean isBatchMode;
        private int resultSetFetchSize;

        public Builder setUrl(final String url) {
            this.url = url;
            return this;
        }

        public Builder setUser(final String user) {
            this.user = user;
            return this;
        }

        public Builder setPassword(final String password) {
            this.password = password;
            return this;
        }

        public Builder setDriver(final String driver) {
            this.driver = driver;
            return this;
        }

        public Builder setTransactionMode(final TransactionMode transactionMode) {
            this.transactionMode = transactionMode;
            return this;
        }

        public Builder setBatchSize(final BigInteger batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder setTransactionSize(final BigInteger transactionSize) {
            this.transactionSize = transactionSize;
            return this;
        }

        public Builder isBatchMode(final boolean isBatchMode) {
            this.isBatchMode = isBatchMode;
            return this;
        }

        public Builder setResultSetFetchSize(final int resultSetFetchSize) {
            this.resultSetFetchSize = resultSetFetchSize;
            return this;
        }

        public ConnectionProperties build() {
            return new RunSqlConnectionProperties(this);
        }
    }
}
