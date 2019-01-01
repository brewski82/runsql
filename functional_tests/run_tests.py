#!/usr/bin/env python3
#
# Copyright 2019 William Bruschi - williambruschi.net
#
# This file is part of runsql.
#
# runsql is free software: you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# runsql is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with runsql.  If not, see <https://www.gnu.org/licenses/>.
#

from functional_tests import *

def run_all_tests():
    mysql_maria_tests.run_maria_test()
    mssql_tests.run_mssql_test()
    mysql_maria_tests.run_mysql_test()
    oracle_tests.run_oracle_test()
    postgres_tests.run_postgres_test()
    sqlite_tests.run_sqlite_test()


if __name__ == "__main__":
    run_all_tests()
