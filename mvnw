#!/bin/sh
basedir=$(dirname "$0")

if [ -f "$basedir/.mvn/wrapper/maven-wrapper.jar" ]; then
    java -classpath "$basedir/.mvn/wrapper/maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain "$@"
else
    echo "Maven Wrapper JAR not found. Running 'mvn' from PATH..."
    mvn "$@"
fi