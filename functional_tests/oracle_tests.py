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

oracle = DbProperties()
oracle.port = 49161
oracle.container_name = 'runsql-oracle'
oracle.jar_name = 'ojdbc6.jar'
oracle.url = 'jdbc:oracle:thin:@127.0.0.1:{}:xe'.format(oracle.port)
oracle.user = 'system'
oracle.password = 'oracle'
oracle.driver = 'oracle.jdbc.driver.OracleDriver'
oracle.dir = 'oracle'
oracle.name = 'oracle'
oracle.sp_call = 'select sp_test(30) from dual;'


def docker_run_oracle():
    additonal_args = ['--publish', '{0}:1521'.format(oracle.port), '-e', 'ORACLE_ALLOW_REMOTE=true', 'wnameless/oracle-xe-11g']
    return docker_run(oracle.container_name, additonal_args)

def docker_kill_oracle():
    return docker_kill(oracle.container_name)

def call_runsql_oracle(additional_args):
    oracle_args = ['--url', oracle.url,
                     '--user', oracle.user,
                     '--password', oracle.password,
                     '--driver', oracle.driver] + additional_args
    return call_runsql(oracle.jar_name, oracle_args)

def run_oracle_test():
    docker_run_oracle()
    ping_server(call_runsql_oracle, ["--sql", "select * from dual;"])
    # run_generic_test(call_runsql_oracle)
    run_specific_test(oracle, call_runsql_oracle)
    docker_kill_oracle()
    log_message('Done oracle test.')
    return

if __name__ == "__main__":
    run_oracle_test()
