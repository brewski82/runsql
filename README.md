# RunSQL

RunSQL is a command line utility that executes SQL statements on all
major databases that support JDBC connectivity.

## Building

To build RunSQL, you will need:

1. Java Development Kit (JDK) 8 or higher.
2. [Apache Maven](https://maven.apache.org/)

Then in a terminal, navigate to the RunSQL source directory and run:
```
mvn package
```
This will compile a jar file with all the dependencies in the target
directory.

On linux environments, you can also execute `bin/build` which runs the
above command and copies the jar to the dist directory. Then you
should be able to run `dist/linux/bin/runsql -h` and see the help
output.

## Installing

After building, copy the appropriate directory under dist to the
directory you wish to install runsql. For example, if you are on linux
and want to install to the /opt/RunSQL directory, run `cp -r
dist/linux/* /opt/RunSQL/`.

RunSQL must know the location of any JDBC drivers you plan to use. You have four options:

1. Set the environment variable `RUNSQL_JDBC_DIR` to a directory containing your JDBC driver(s). RunSQL will include all jar files found in the directory.
2. Place the JDBC driver file (or a symbolic link to it) in the RunSQL/lib directory. RunSQL adds any jars found in the lib directory to the classpath.
3. Set the environment variable RUNSQL_JDBC_PATH to the path of the JDBC file.
4. (Advanced) Edit the bin/runsql script to point to your driver.

Optional: Set the environment variables `RUNSQL_PROPERTIES_FILE` and
`RUNSQL_PROPERTIES_PREFIX`. These provide convenient access to database
credentials.

If you would like to call RunSQL without specifying the full path: `export PATH="/opt/RunSQL/bin:$PATH"`

To test installation, run `/opt/RunSQL/bin/runsql --help` and it
should display RunSQL's help.

## Getting started

RunSQL has many parameters. Call RunSQL with `--help` for details on all the options. The most important parameters are url, user, password and driver. The url is the database's JDBC url. The driver is the fully qualified name of the driver's class. Let's assume you have a PostgreSQL database with a table called 'person' and want to select from it. Run the following command:

```
runsql --url jdbc:postgresql://127.0.0.1:5432/postgres --user postgres --password "" \
--driver org.postgresql.Driver --sql "select * from person;"
```

A shortcut to 'select *' is the `--tablename` parameter. RunSQL uses `--tablename` for multiple purposes. By default it selects every column from the table.

```
runsql --url jdbc:postgresql://127.0.0.1:5432/postgres --user postgres --password "" \
--driver org.postgresql.Driver --tablename person
```

## Best Practices

Passing connection information for each RunSQL call grows
tiresome. Instead, save connection information in a properties file
and pass the file's path to RunSQL. For example, if the file
/tmp/connections.properties contains the following:

```
url=jdbc:postgresql://127.0.0.1:5432/postgres
user=postgres
password=
driver=org.postgresql.Driver
```

call RunSQL like this:

```
runsql --propertiesfilepath /tmp/connection.properties --tablename person
```

Use different prefixes to store multiple connections in a single properties file. For example, modify the contents of /tmp/connection.properties to:

```
database_a.url=jdbc:postgresql://127.0.0.1:5432/postgres
database_a.user=postgres
database_a.password=
database_a.driver=org.postgresql.Driver

database_b.url=jdbc:postgresql://127.0.0.1:5432/databaseb
database_b.user=postgres
database_b.password=
database_b.driver=org.postgresql.Driver
```

and run the same query as follows:

```
runsql --propertiesfilepath /tmp/connection.properties --propertiesprefix database_a. --tablename person
```

Since passing in the path and prefix of the properties can also grow tiresome, set the RUNSQL_PROPERTIES_FILE and RUNSQL_PROPERTIES_PREFIX environment variables:

```
export RUNSQL_PROPERTIES_FILE=/tmp/connection.properties
export RUNSQL_PROPERTIES_PREFIX="database_a."
runsql --tablename person
```

## Usage FAQ

* How can I create a CSV report?

Use the `--fileformat csv` option. By default RunSQL prints to
standard output. To print to a file, either redirect the output or use
the `--outputfilepath` option.

* How can I create a pipe delimited report?

Here is an example of using RunSQL's format options for pipe delimited output:

```
runsql --tablename person --columnseparator '|' --includeheaders t --quotemode always --quotevalue '"'
```

* What is the difference betwen MSDOSCSV and normal CSV?

The MSDOSCSV fileformat option uses Excel specific formatting so
numbers display with leading zeros. It also forces the output to use
Window's style line feeds.

* Can I execute multiple SQL statements?

Yes! You can include multiple statements in the `--sql` argument or
use multiple `--sql` arguments:

```
runsql --sql "select first_name from person; select last_name from person;"

runsql --sql "select first_name from person;" --sql "select last_name from person;"
```

* How do I run a bunch of SQL statements contained in a file?

You can use the `--inputfilepath` option for that:

```
runsql --inputfilepath /tmp/tmp.sql
```

* I want to run a SQL file containing database specific stored procedures but I am getting parsing errors. What do I do?

Suppose you have the following contents in the file /tmp/tmp.sql:

```
CREATE FUNCTION sp_test(return_value integer) RETURNS integer AS $$
DECLARE
    quantity integer := 30;
BEGIN
    RAISE NOTICE 'Quantity here is %', quantity;
    RETURN return_value;
END;
$$ LANGUAGE plpgsql;
```

By default RunSQL splits sql statements by the semicolon character and
executes each statement individually, which causes this parsing
error. Tell RunSQL not to split statements like this:

```
runsql --splitsqlstatements f --inputfilepath /tmp/tmp.sql
```

You can control the character used to split SQL statements with the `--sqlstatementseparator` option.

* Can I run SQL from standard input?

Yes, by using the hyphen character for the `--inputfilepath` option:

```
echo "select * from person;" | runsql --inputfilepath -
```

* How can I quickly backup data in a table?

Use the `--fileformat inserts` option:

```
runsql --tablename person --fileformat inserts
```

or to use a different table name:

```
runsql --tablename person_first_name_only --fileformat inserts --sql 'select first_name from person'
```

## License

GPLv3. See [COPYING](COPYING).
