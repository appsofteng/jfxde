@echo off
rem -Djavafx.animation.fullspeed=true
rem -Djavafx.animation.framerate
rem -Djavafx.animation.pulse
rem -Dprism.vsync=false

"%JAVA_HOME%\bin\java" -Djavafx.animation.fullspeed=true -Djava.util.logging.config.file=conf/logging.properties -Dorg.jboss.logging.provider=jdk --add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls -p lib;modules;apps;"%HOME%/${appsDir}" -cp libcp/* -m dev.jfxde.ui/dev.jfxde.ui.Main
