@echo off
rem This batch gathers all single scripts together into a single file and optionally replaces the db name (schema) and the MySQL hostname
rem (C) Copyright 2011 Alexander Schulze, Innotrade GmbH
set db_name=jWebSocket
set host_name=localhost
set demo_user=jwsDemo
set app_user=jwsApp
set sys_user=jwsSys
set fart="..\..\tools\fart.exe"

copy /b grantDemo.sql + grantApp.sql + grantSys.sql grantAll.sql

rem %fart% "s/`ria-db`/`%db_name%`/g" 
rem %fart% "s/`localhost`;/`%host_name%`;/g" 
rem %fart% "s/`jwsDemo`/`%demo_user%`/g" 
rem %fart% "s/`jwsApp`/`%app_user%`/g" 
rem %fart% "s/`jwsSys`/`%sys_user%`/g"

set db_name=
set host_name=
set demo_user=
set app_user=
set sys_user=

pause