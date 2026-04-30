#!/usr/bin/env sh
gradle_home=$(cd "$(dirname "$0")" && pwd)

CLASSPATH=$gradle_home/gradle/wrapper/gradle-wrapper.jar

exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
