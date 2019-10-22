@echo off
cd %~dp0
"%JAVA_HOME%\bin\java" %* @options-debug @options > javafx.log 2>&1