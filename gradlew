#!/bin/sh

# Gradle startup script for POSIX generated from the standard Gradle wrapper template.
APP_HOME=$( cd "${0%/*}" > /dev/null 2>&1 && pwd -P )
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
