#!/bin/sh
set -e

APP_HOME="$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)"
APP_NAME=""
APP_BASE_NAME=${0##*/}
case $(uname -s) in
    Darwin*) GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}" ;;
    *) GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}" ;;
esac
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
