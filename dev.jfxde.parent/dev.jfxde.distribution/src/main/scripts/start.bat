@echo off
rem -Djavafx.animation.fullspeed=true
rem -Djavafx.animation.framerate
rem -Djavafx.animation.pulse
rem -Dprism.vsync=false
rem -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044

cd %~dp0
"%JAVA_HOME%\bin\java" %* @options