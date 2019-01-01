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

mssql = DbProperties()
mssql.port = 5101
mssql.container_name = 'runsql-mssql'
mssql.jar_name = 'sqljdbc42.jar'
mssql.url = 'jdbc:sqlserver://127.0.0.1:{}'.format(mssql.port)
mssql.user = 'sa'
mssql.password = '4pnONNcrrrE&!qXay6YpZIOuiqzWO'
mssql.driver = 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
mssql.dir = 'mssql'
mssql.name = 'mssql'
mssql.sp_call = 'execute sp_test 30;'


def docker_run_mssql():
    additonal_args = ['--publish', '{0}:1433'.format(mssql.port), '-e', 'ACCEPT_EULA=Y',
                      '-e', 'SA_PASSWORD={}'.format(mssql.password), 'microsoft/mssql-server-linux']
    return docker_run(mssql.container_name, additonal_args)

def docker_kill_mssql():
    return docker_kill(mssql.container_name)

def call_runsql_mssql(additional_args):
    mssql_args = ['--url', mssql.url,
                  '--user', mssql.user,
                  '--password', mssql.password,
                  '--driver', mssql.driver] + additional_args
    return call_runsql(mssql.jar_name, mssql_args)

def run_mssql_test():
    docker_run_mssql()
    ping_server(call_runsql_mssql, ["--sql", "select * from sys.tables;"])
    run_generic_test(call_runsql_mssql)
    run_specific_test(mssql, call_runsql_mssql)
    docker_kill_mssql()
    log_message('Done mssql test.')
    return

if __name__ == "__main__":
    run_mssql_test()
