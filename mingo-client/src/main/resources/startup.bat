@echo off & setlocal enabledelayedexpansion
title mingo-client
cd %~dp0

set LIB_JARS=""
cd ..\lib
for %%i in (*) do set LIB_JARS=!LIB_JARS!;..\lib\%%i
cd ..\bin

java -Dapp.home=../ -Xms64m -Xmx512m -classpath ..\conf;%LIB_JARS% com.ido.mingo.client.ClientStarter
goto end