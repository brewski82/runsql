@echo off
SETLOCAL ENABLEEXTENSIONS
set parentdir=%~dp0..
set libdir=%parentdir%\lib
set cp=%libdir%\*
IF DEFINED RUNSQL_JDBC_PATH (set cp=%cp%;%RUNSQL_JDBC_PATH%)
IF DEFINED RUNSQL_JDBC_DIR (set cp=%cp%;%RUNSQL_JDBC_DIR%/*)

java -cp "%libdir%\*" runsql.RunSqlMain %*
