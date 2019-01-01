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

my = DbProperties()
my.port = 5100
my.container_name = 'runsql-mysql-maria'
my.jar_name = 'mysql-connector-java-5.1.40-bin.jar'
my.url = 'jdbc:mysql://127.0.0.1:{}/mysql?useSSL=false'.format(my.port)
my.user = 'root'
my.password = ''
my.driver = 'com.mysql.jdbc.Driver'
my.sp_call = 'call sp_test(30)'
my.dir = 'mysql'


def docker_run_mysql(image):
    additonal_args = ['--publish', '{0}:3306'.format(my.port), '-e', 'MYSQL_ALLOW_EMPTY_PASSWORD=yes', image]
    return docker_run(my.container_name, additonal_args)

def docker_kill_mysql():
    return docker_kill(my.container_name)

def call_runsql_mysql(additional_args):
    mysql_args = ['--url', my.url,
                  '--user', my.user,
                  '--password', my.password,
                  '--driver', my.driver] + additional_args
    return call_runsql(my.jar_name, mysql_args)

def run_test(name, image):
    docker_run_mysql(image)
    ping_server(call_runsql_mysql, ["--sql", "select * from information_schema.columns;"])
    run_generic_test(call_runsql_mysql)
    run_specific_test(my, call_runsql_mysql)
    docker_kill_mysql()
    log_message('Done mysql test.')
    return

def run_mysql_test():
    my.name = 'mysql'
    log_message('Mysql Test')
    run_test('mysql', 'mysql:latest')
    return

def run_maria_test():
    my.name = 'maria'
    log_message('Maria Test')
    run_test('maria', 'mariadb:latest')
    return

if __name__ == "__main__":
    run_maria_test()
    run_mysql_test()
