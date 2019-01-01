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

package runsql.impl.arguments;

import runsql.Arguments;
import runsql.RunSqlMain;
import runsql.impl.exceptions.RunSqlParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static runsql.impl.arguments.Parameter.BATCH_SIZE;
import static runsql.impl.arguments.Parameter.BOOLEAN_FALSE_VALUE;
import static runsql.impl.arguments.Parameter.BOOLEAN_TRUE_VALUE;
import static runsql.impl.arguments.Parameter.COLUMN_SEPARATOR;
import static runsql.impl.arguments.Parameter.DRIVER;
import static runsql.impl.arguments.Parameter.ECHO_SQL;
import static runsql.impl.arguments.Parameter.ESCAPE_CHARACTER;
import static runsql.impl.arguments.Parameter.FILE_FORMAT;
import static runsql.impl.arguments.Parameter.HELP;
import static runsql.impl.arguments.Parameter.IMPORT_COLUMNS;
import static runsql.impl.arguments.Parameter.IMPORT_DRIVER;
import static runsql.impl.arguments.Parameter.IMPORT_PASSWORD;
import static runsql.impl.arguments.Parameter.IMPORT_PROPERTIES_FILE_PATH;
import static runsql.impl.arguments.Parameter.IMPORT_PROPERTIES_PREFIX;
import static runsql.impl.arguments.Parameter.IMPORT_TABLE;
import static runsql.impl.arguments.Parameter.IMPORT_URL;
import static runsql.impl.arguments.Parameter.IMPORT_USER;
import static runsql.impl.arguments.Parameter.INCLUDE_HEADERS;
import static runsql.impl.arguments.Parameter.INPUT_FILE_PATH;
import static runsql.impl.arguments.Parameter.NUMBER_OF_JOBS;
import static runsql.impl.arguments.Parameter.OUTPUT_FILE_PATH;
import static runsql.impl.arguments.Parameter.PASSWORD;
import static runsql.impl.arguments.Parameter.PROPERTIES_FILE_PATH;
import static runsql.impl.arguments.Parameter.PROPERTIES_PREFIX;
import static runsql.impl.arguments.Parameter.QUOTE_MODE;
import static runsql.impl.arguments.Parameter.QUOTE_VALUE;
import static runsql.impl.arguments.Parameter.RESULT_SET_FETCH_SIZE;
import static runsql.impl.arguments.Parameter.ROW_SEPARATOR;
import static runsql.impl.arguments.Parameter.SPLIT_SQL_STATEMENTS;
import static runsql.impl.arguments.Parameter.SQL;
import static runsql.impl.arguments.Parameter.STATEMENT_SEPARATOR;
import static runsql.impl.arguments.Parameter.STRIP_COMMENTS;
import static runsql.impl.arguments.Parameter.TABLE_NAME;
import static runsql.impl.arguments.Parameter.TRANSACTION_MODE;
import static runsql.impl.arguments.Parameter.URL;
import static runsql.impl.arguments.Parameter.USER;
import static runsql.impl.arguments.Parameter.VALUE_WHEN_NULL;

public class RunSqlArguments implements Arguments {
    public static final int HELP_WIDTH = 160;
    private static final String RUNSQL_PROPERTIES_FILE_ENV_NAME = "RUNSQL_PROPERTIES_FILE";
    private static final String RUNSQL_PROPERTIES_PREFIX_ENV_NAME = "RUNSQL_PROPERTIES_PREFIX";
    protected final Options options = new Options();
    protected final Options connectionOptions = new Options();
    protected final Options sqlOptions = new Options();
    protected final Options outputOptions = new Options();
    protected final Options importOptions = new Options();
    protected final Options formatOptions = new Options();
    protected final Options otherOptions = new Options();
    protected final CommandLineParser commandLineParser = new DefaultParser();
    protected final Properties sourceProperties = new Properties();
    protected final Properties importProperties = new Properties();
    protected final Map<String, String> defaultValuesMap = new HashMap<>();
    private final Properties helpProperties = new Properties();
    protected CommandLine commandLine;

    public RunSqlArguments() {
        try (InputStream inputStream = RunSqlMain.class.getResourceAsStream("RunSql.properties")) {
            helpProperties.load(inputStream);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load RunSql.properties.", e);
        }
        connectionOptions
                .addOption(createArgumentOption(URL, helpProperties.getProperty("options.url")))
                .addOption(createArgumentOption(USER, helpProperties.getProperty("options.user")))
                .addOption(createArgumentOption(PASSWORD,
                                                helpProperties.getProperty("options.password")))
                .addOption(
                        createArgumentOption(DRIVER, helpProperties.getProperty("options.driver")))
                .addOption(createArgumentOption(PROPERTIES_FILE_PATH, helpProperties
                        .getProperty("options.propertiesFilePath")))
                .addOption(createArgumentOption(PROPERTIES_PREFIX, helpProperties
                        .getProperty("options.propertiesPrefix")));
        sqlOptions
                .addOption(createArgumentOption(SQL, helpProperties.getProperty("options.sql")))
                .addOption(createArgumentOption(SPLIT_SQL_STATEMENTS,
                                                helpProperties.getProperty("options.splitSql")))
                .addOption(createArgumentOption(STATEMENT_SEPARATOR,
                                                helpProperties.getProperty("options.sqlSeparator")))
                .addOption(createArgumentOption(STRIP_COMMENTS, helpProperties
                        .getProperty("options.stripComments")))
                .addOption(createArgumentOption(INPUT_FILE_PATH,
                                                helpProperties.getProperty("options.inputFile")))
                .addOption(createArgumentOption(ECHO_SQL,
                                                helpProperties.getProperty("options.echoSql")))
                .addOption(createArgumentOption(BATCH_SIZE,
                                                helpProperties.getProperty("options.batchSize")))
                .addOption(createArgumentOption(TRANSACTION_MODE, helpProperties
                        .getProperty("options.transactionMode")))
                .addOption(createArgumentOption(RESULT_SET_FETCH_SIZE, helpProperties
                        .getProperty("options.resultSetFetchSize")))
                .addOption(createArgumentOption(NUMBER_OF_JOBS, helpProperties
                        .getProperty("options.numberOfJobs")));
        outputOptions
                .addOption(createArgumentOption(OUTPUT_FILE_PATH, helpProperties
                        .getProperty("options.outputFilePath")));
        importOptions
                .addOption(createArgumentOption(IMPORT_TABLE,
                                                helpProperties.getProperty("options.importTable")))
                .addOption(createArgumentOption(IMPORT_COLUMNS, helpProperties
                        .getProperty("options.importColumns")))
                .addOption(createArgumentOption(IMPORT_URL,
                                                helpProperties.getProperty("options.importUrl")))
                .addOption(createArgumentOption(IMPORT_USER,
                                                helpProperties.getProperty("options.importUser")))
                .addOption(createArgumentOption(IMPORT_PASSWORD, helpProperties
                        .getProperty("options.importPassword")))
                .addOption(createArgumentOption(IMPORT_DRIVER,
                                                helpProperties.getProperty("options.importDriver")))
                .addOption(createArgumentOption(IMPORT_PROPERTIES_FILE_PATH, helpProperties
                        .getProperty("options.importPropertiesFilePath")))
                .addOption(createArgumentOption(IMPORT_PROPERTIES_PREFIX, helpProperties
                        .getProperty("options.importPropertiesPrefix")));
        formatOptions
                .addOption(createArgumentOption(ROW_SEPARATOR,
                                                helpProperties.getProperty("options.rowSeparator")))
                .addOption(createArgumentOption(COLUMN_SEPARATOR, helpProperties
                        .getProperty("options.columnSeparator")))
                .addOption(createArgumentOption(VALUE_WHEN_NULL, helpProperties
                        .getProperty("options.valueWhenNull")))
                .addOption(createArgumentOption(QUOTE_VALUE,
                                                helpProperties.getProperty("options.quoteValue")))
                .addOption(createArgumentOption(QUOTE_MODE,
                                                helpProperties.getProperty("options.quoteMode")))
                .addOption(createArgumentOption(ESCAPE_CHARACTER, helpProperties
                        .getProperty("options.escapeCharacter")))
                .addOption(createArgumentOption(INCLUDE_HEADERS, helpProperties
                        .getProperty("options.includeHeaders")))
                .addOption(createArgumentOption(FILE_FORMAT,
                                                helpProperties.getProperty("options.fileFormat")))
                .addOption(createArgumentOption(TABLE_NAME,
                                                helpProperties.getProperty("options.tableName")))
                .addOption(createArgumentOption(BOOLEAN_TRUE_VALUE, helpProperties
                        .getProperty("options.booleanTrueValue")))
                .addOption(createArgumentOption(BOOLEAN_FALSE_VALUE, helpProperties
                        .getProperty("options.booleanFalseValue")));
        otherOptions
                .addOption(Option.builder(HELP.getName()).longOpt(HELP.getLongName())
                                 .desc(helpProperties.getProperty("options.help")).build());
        loadOptions(options, connectionOptions);
        loadOptions(options, sqlOptions);
        loadOptions(options, outputOptions);
        loadOptions(options, importOptions);
        loadOptions(options, formatOptions);
        loadOptions(options, otherOptions);
        // Defaults.
        defaultValuesMap.put(ROW_SEPARATOR.getEitherName(), ArgumentDefaults.DEFAULT_ROW_SEPARATOR);
        defaultValuesMap
                .put(COLUMN_SEPARATOR.getEitherName(), ArgumentDefaults.DEFAULT_COLUMN_SEPARATOR);
        defaultValuesMap.put(STATEMENT_SEPARATOR.getEitherName(),
                             ArgumentDefaults.DEFAULT_SQL_STATEMENT_SEPARATOR);
        defaultValuesMap
                .put(VALUE_WHEN_NULL.getEitherName(), ArgumentDefaults.DEFAULT_VALUE_WHEN_NULL);
        defaultValuesMap
                .put(ESCAPE_CHARACTER.getEitherName(), ArgumentDefaults.DEFAULT_ESCAPE_CHARACTER);
        defaultValuesMap.put(SPLIT_SQL_STATEMENTS.getEitherName(),
                             ArgumentDefaults.DEFAULT_SPLIT_SQL_STATEMENTS);
        defaultValuesMap
                .put(STRIP_COMMENTS.getEitherName(), ArgumentDefaults.DEFAULT_STRIP_COMMENTS);
        defaultValuesMap.put(INCLUDE_HEADERS.getEitherName(), ArgumentDefaults.OUTPUT_COLUMN_NAMES);
        defaultValuesMap.put(FILE_FORMAT.getEitherName(), ArgumentDefaults.DEFAULT_FILE_FORMAT);
        defaultValuesMap.put(QUOTE_MODE.getEitherName(), ArgumentDefaults.DEFAULT_QUOTE_MODE);
        defaultValuesMap.put(BATCH_SIZE.getEitherName(), ArgumentDefaults.DEFAULT_BATCH_SIZE);
        defaultValuesMap
                .put(TRANSACTION_MODE.getEitherName(), ArgumentDefaults.DEFAULT_TRANSACTION_MODE);
        defaultValuesMap.put(BOOLEAN_TRUE_VALUE.getEitherName(),
                             ArgumentDefaults.DEFAULT_BOOLEAN_TRUE_VALUE);
        defaultValuesMap.put(BOOLEAN_FALSE_VALUE.getEitherName(),
                             ArgumentDefaults.DEFAULT_BOOLEAN_FALSE_VALUE);
        defaultValuesMap.put(RESULT_SET_FETCH_SIZE.getEitherName(),
                             ArgumentDefaults.DEFAULT_RESULT_SET_FETCH_SIZE);
        defaultValuesMap
                .put(NUMBER_OF_JOBS.getEitherName(), ArgumentDefaults.DEFAULT_NUMBER_OF_JOBS);
    }

    private static Option createArgumentOption(final Parameter name, final String description) {
        return Option.builder(name.getName()).longOpt(name.getLongName()).desc(description)
                     .argName(name.getArgName()).hasArg().build();
    }

    private static void loadOptions(final Options optionsToAddTo, final Options optionsToGetFrom) {
        optionsToGetFrom.getOptions().forEach(optionsToAddTo::addOption);
    }

    @Override
    public void parse(final String[] args) {
        try {
            commandLine = commandLineParser.parse(options, args);
            if (commandLine.hasOption(PROPERTIES_FILE_PATH.getEitherName())) {
                String propertiesFilePath =
                        commandLine.getOptionValue(PROPERTIES_FILE_PATH.getEitherName());
                FileInputStream fileInputStream = new FileInputStream(propertiesFilePath);
                sourceProperties.load(fileInputStream);
                fileInputStream.close();
            } else if (System.getenv(RUNSQL_PROPERTIES_FILE_ENV_NAME) != null) {
                FileInputStream fileInputStream =
                        new FileInputStream(System.getenv(RUNSQL_PROPERTIES_FILE_ENV_NAME));
                sourceProperties.load(fileInputStream);
            }
            if (commandLine.hasOption(HELP.getEitherName())) {
                printHelp();
            }
            if (commandLine.hasOption(IMPORT_PROPERTIES_FILE_PATH.getEitherName())) {
                String propertiesFilePath =
                        commandLine.getOptionValue(IMPORT_PROPERTIES_FILE_PATH.getEitherName());
                FileInputStream fileInputStream = new FileInputStream(propertiesFilePath);
                importProperties.load(fileInputStream);
                fileInputStream.close();
            }
        } catch (final ParseException e) {
            printHelp();
            throw new RunSqlParseException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getValue(final String parameter) {
        return getValue(parameter, isImportParameter(parameter));
    }

    @Override
    public String[] getValues(final String parameter) {
        return commandLine.getOptionValues(parameter);
    }

    @Override
    public boolean hasOption(final String parameter) {
        return commandLine.hasOption(parameter) || getSourcePropertyValue(getParameter(parameter),
                                                                          isImportParameter(
                                                                                  parameter)) != null;
    }

    private String getSourcePropertyValue(final Parameter parameter,
                                          final boolean useImportPrefix) {
        String propertiesFilePrefix = commandLine.getOptionValue(PROPERTIES_PREFIX.getEitherName());
        if (propertiesFilePrefix == null) {
            propertiesFilePrefix = System.getenv(RUNSQL_PROPERTIES_PREFIX_ENV_NAME);
            if (propertiesFilePrefix == null) {
                propertiesFilePrefix = "";
            }
        }
        if (useImportPrefix) {
            String importPropertiesPrefix =
                    commandLine.getOptionValue(IMPORT_PROPERTIES_PREFIX.getEitherName());
            if (importPropertiesPrefix == null) {
                importPropertiesPrefix = propertiesFilePrefix;
            }
            String importProperty =
                    importProperties.getProperty(importPropertiesPrefix + parameter.getLongName());
            if (importProperty != null) {
                return importProperty;
            }
            String importPropertyFromSource =
                    sourceProperties.getProperty(importPropertiesPrefix + parameter.getLongName());
            if (importPropertyFromSource != null) {
                return importPropertyFromSource;
            }
            return null;
        }
        String longOptionNameProperty =
                sourceProperties.getProperty(propertiesFilePrefix + parameter.getLongName());
        if (longOptionNameProperty != null) {
            return longOptionNameProperty;
        }
        return sourceProperties.getProperty(propertiesFilePrefix + parameter.getEitherName());
    }

    private Parameter getParameter(final String parameter) {
        return Parameter.getParameter(parameter);
    }

    private boolean isImportParameter(final String parameterName) {
        Parameter parameter = getParameter(parameterName);
        switch (parameter) {
            case IMPORT_DRIVER:
            case IMPORT_URL:
            case IMPORT_USER:
            case IMPORT_PASSWORD:
                return true;
            default:
                return false;
        }
    }

    private void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(null);
        helpFormatter.setWidth(HELP_WIDTH);
        helpFormatter.setSyntaxPrefix("");
        helpFormatter
                .printHelp("RunSql", helpProperties.getProperty("help.header"), connectionOptions,
                           "", false);
        helpFormatter.printHelp("\nSQL Statement Options", sqlOptions, false);
        helpFormatter.printHelp("\nOutput Options", outputOptions, false);
        helpFormatter.printHelp("\nImport Options", importOptions, false);
        helpFormatter.printHelp("\nFormat Options", formatOptions, false);
        helpFormatter
                .printHelp("\nOther", "", otherOptions, helpProperties.getProperty("help.footer"),
                           false);
    }

    private String getValue(final String parameterName, final boolean useImportPrefix) {
        Parameter parameter = getParameter(parameterName);
        String value = commandLine.getOptionValue(parameter.getEitherName(),
                                                  getSourcePropertyValue(parameter,
                                                                         useImportPrefix));
        if (value == null) {
            if (isImportParameter(parameterName)) {
                switch (parameter) {
                    case IMPORT_DRIVER:
                        return getValue(DRIVER.getEitherName(), true);
                    case IMPORT_URL:
                        return getValue(URL.getEitherName(), true);
                    case IMPORT_USER:
                        return getValue(USER.getEitherName(), true);
                    case IMPORT_PASSWORD:
                        return getValue(PASSWORD.getEitherName(), true);
                    default:
                }
            } else {
                return defaultValuesMap.get(parameter.getEitherName());
            }
        }
        return value;
    }
}
