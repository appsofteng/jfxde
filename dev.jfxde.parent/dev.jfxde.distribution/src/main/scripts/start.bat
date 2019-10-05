@echo off
rem -Djavafx.animation.fullspeed=true
rem -Djavafx.animation.framerate
rem -Djavafx.animation.pulse
rem -Dprism.vsync=false
rem -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044

cd %~dp0
"%JAVA_HOME%\bin\java" -Djavafx.animation.fullspeed=true -Djava.util.logging.config.file=conf/logging.properties -Dorg.jboss.logging.provider=jdk --add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls -p lib/mp;modules;apps;"%HOME%/${homeDir}/apps" -cp lib/cp/* -m dev.jfxde.ui
