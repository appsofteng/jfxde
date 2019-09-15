@echo off
rem -Djavafx.animation.fullspeed=true
rem -Djavafx.animation.framerate
rem -Djavafx.animation.pulse
rem -Dprism.vsync=false

start "" runtime\bin\javaw -Djavafx.animation.fullspeed=true -Djava.util.logging.config.file=conf/logging.properties -Dorg.jboss.logging.provider=jdk --add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls -p lib/mp;modules;apps;"%HOME%/${homeDir}/apps" -cp lib/cp/* -m dev.jfxde.ui
