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
from pathlib import Path

test_dir = Path(__file__).parent
runsql_base = Path(__file__).parent.parent
runsql_pom = runsql_base / 'pom.xml'
mysql_port = 5100
mssql_port = 5101
mssql_password = '4pnonncrrre&!qxay6ypziouiqzwo'
oracle_port = 49161
oracle_password = 'oracle'
jar_driver_path = test_dir / 'jdbc-drivers'
runsql_main_class = 'runsql.RunSqlMain'
