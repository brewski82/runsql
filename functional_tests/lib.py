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

import sys
import subprocess
import datetime
import time
import os
import tempfile
import filecmp
from functional_tests.environment import *
from contextlib import redirect_stderr

base_dir = os.path.dirname(__file__)

class DbProperties:
    pass

def log_message(message):
    """Prints message in uniform manner."""
    print('{}: {}'.format(datetime.datetime.today(), message))
    return

def make_temp_file(prefix):
    handle, path = tempfile.mkstemp(prefix=prefix)
    return path

def docker_run(container_name, additional_args):
    log_message('Start container ' + container_name + '.')
    args = ['docker', 'run', '--name', container_name, '--rm', '--detach']
    args = args + additional_args
    return subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                          check=True)

def docker_kill(container_name):
    return subprocess.run(['docker', 'kill', container_name],
                          stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                          check=True)

jar_path = None
def get_jar_path(refresh=False):
    global jar_path
    if jar_path and not refresh:
        return jar_path
    args = ['mvn', '--file', str(runsql_pom), '-q', '-Dexec.executable=echo',
            '-Dexec.args=${project.version}', '--non-recursive',
            'exec:exec']
    result = subprocess.check_output(args).decode('UTF-8')[:-1]
    jar_path = runsql_base / ('target/runsql-' + result + '-jar-with-dependencies.jar')
    return jar_path

def package_runsql():
    log_message('Building runsql...')
    subprocess.run(['mvn', '--file', runsql_pom, '-q', 'clean', 'package']
                   , stdout=subprocess.PIPE, stderr=subprocess.PIPE
                   , check=True)
    log_message('Done building runsql.')
    return

def call_runsql(jar_name, additional_args=[]):
    class_path = str(get_jar_path()) + ':' + str(jar_driver_path) + '/' + jar_name
    args = ['java', '-cp', class_path, runsql_main_class] + additional_args
    return subprocess.run(args, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

def ping_server(call_fn, additional_args):
    max_attempts = 12
    for attempt in range(max_attempts):
        log_message('Ping attempt {}'.format(attempt))
        try:
            call_fn(additional_args)
            log_message('Server is ready.')
            return
        except subprocess.CalledProcessError:
            time.sleep(3)
    raise Exception('Unable to reach server.')

def compare_files(expected_file_path, actual_file_path):
    if not filecmp.cmp(expected_file_path, actual_file_path):
        with open(actual_file_path, 'r') as myfile:
            data=myfile.read().replace('\n', '')
            print('')
            print('File contents:')
            print(data)
            print('')
        raise Exception('Output file {} differs from expected file {}.'.format(expected_file_path, actual_file_path))
    return

def run_generic_test(call_fn, exp_dir = '/generic/'):
    input_file_path = base_dir + '/generic/generic-tables-and-data.sql'
    expected_results = base_dir + exp_dir + 'expected-results.txt'
    log_message('Create generic table and data.')
    call_fn(['-inputfilepath', input_file_path])
    output_file = tempfile.NamedTemporaryFile(mode='w+', prefix='generic-test-output')
    log_message('Selecting generic data into file ' + output_file.name)
    call_fn(['-sql', 'select * from generic_person;',
             '-outputfilepath', output_file.name])
    log_message('Comparing results to baseline file.')
    if not filecmp.cmp(expected_results, output_file.name):
        raise Exception('Files {} and {} differ.'.format(expected_results, output_file.name))
    log_message('Testing batch and transactional inserts.')
    batch_args = ['--inputfilepath', base_dir + '/generic/batch-inserts.sql']
    call_fn(batch_args + ['--batchsize', '3'])
    call_fn(batch_args + ['--transactionmode', '3'])
    call_fn(batch_args + ['--transactionmode', 'rollback', '--batchsize', '10'])
    output_file = tempfile.NamedTemporaryFile(mode='w+', prefix='generic-test-output-batch')
    call_fn(['-sql', 'select * from generic_person order by a_char, a_varchar;',
             '-outputfilepath', output_file.name])
    log_message('Comparing results to baseline.')
    expected_results = base_dir + exp_dir + 'expected-results-transaction.txt'
    compare_files(expected_results, output_file.name)
    log_message('Done generic test.')
    return

def run_specific_test(props, call_fn):
    log_message('Create table and data.')
    sql_dir = base_dir + '/' + props.dir + '/'
    input_file_path = sql_dir + props.name + '-create-table-and-insert-data.sql'
    call_fn(['--inputfilepath', input_file_path])
    log_message('Select data and compare results.')
    output_file_path = make_temp_file(props.name + '-')
    call_fn(['--sql', 'select * from person;', '--outputfilepath', output_file_path])
    compare_files(sql_dir + props.name + '-expected-results.txt', output_file_path)
    output_file_path = make_temp_file(props.name + '-csv-')
    call_fn(['--sql', 'select * from person;', '--outputfilepath',
             output_file_path, '--fileformat', 'csv'])
    compare_files(sql_dir + props.name + '-expected-results.csv', output_file_path)
    log_message('Database function tests.')
    call_fn(['-inputfilepath', sql_dir + props.name + '-stored-procedure.sql',
                       '-splitsqlstatements', 'f'])
    output_file_path = make_temp_file(props.name + '-sp')
    call_fn(['-sql', props.sp_call, '--outputfilepath', output_file_path])
    return compare_files(sql_dir + props.name + '-expected_results_db_function.txt',
                         output_file_path)
