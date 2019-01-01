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

from functional_tests.lib import *
import os

sqlite = DbProperties()
sqlite.container_name = 'runsql-sqlite'
sqlite.jar_name = 'sqlite-jdbc-3.15.1.jar'
sqlite.user = ''
sqlite.password = ''
sqlite.driver = 'org.sqlite.JDBC'

def call_runsql_sqlite(additional_args):
    sqlite_args = ['--url', sqlite.url,
                  '--user', sqlite.user,
                  '--password', sqlite.password,
                  '--driver', sqlite.driver] + additional_args
    return call_runsql(sqlite.jar_name, sqlite_args)

def run_sqlite_test():
    sqlite.db_file = make_temp_file('sqlite-db-')
    sqlite.url = 'jdbc:sqlite:{}?date_string_format=yyyy-MM-dd'.format(sqlite.db_file)
    run_generic_test(call_runsql_sqlite, '/sqlite/')
    os.remove(sqlite.db_file)
    log_message('Done sqlite test.')
    return

if __name__ == "__main__":
    run_sqlite_test()
