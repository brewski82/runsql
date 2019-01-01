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

pg = DbProperties()
pg.port = 5444
pg.container_name = 'runsql-postgres'
pg.jar_name = 'postgresql-42.2.2.jar'
pg.url = 'jdbc:postgresql://127.0.0.1:{}/postgres'.format(pg.port)
pg.user = 'postgres'
pg.password = ''
pg.driver = 'org.postgresql.Driver'
pg.dir = 'postgres'
pg.name = 'postgres'
pg.sp_call = 'select sp_test(30)'


def docker_run_postgres():
    additonal_args = ['--publish', '{0}:5432'.format(pg.port), 'postgres']
    return docker_run(pg.container_name, additonal_args)

def docker_kill_postgres():
    return docker_kill(pg.container_name)

def call_runsql_postgres(additional_args):
    postgres_args = ['--url', pg.url,
                     '--user', pg.user,
                     '--password', pg.password,
                     '--driver', pg.driver] + additional_args
    return call_runsql(pg.jar_name, postgres_args)

def run_postgres_test():
    docker_run_postgres()
    ping_server(call_runsql_postgres, ["--sql", "select 1;"])
    run_generic_test(call_runsql_postgres)
    run_specific_test(pg, call_runsql_postgres)
    docker_kill_postgres()
    log_message('Done postgres test.')
    return

if __name__ == "__main__":
    run_postgres_test()
