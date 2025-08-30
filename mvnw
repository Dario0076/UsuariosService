#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper
# ----------------------------------------------------------------------------
MVN_HOME="$(cd "$(dirname "$0")"; pwd)"
MAVEN_WRAPPER_JAR="$MVN_HOME/.mvn/wrapper/maven-wrapper.jar"
if [ -f "$MAVEN_WRAPPER_JAR" ]; then
  JAVA_EXE="java"
  if [ -n "$JAVA_HOME" ]; then
    JAVA_EXE="$JAVA_HOME/bin/java"
  fi
  "$JAVA_EXE" -jar "$MAVEN_WRAPPER_JAR" "$@"
else
  echo "The Maven Wrapper JAR could not be found. Please run 'mvn -N io.takari:maven:wrapper' to generate it."
  exit 1
fi
