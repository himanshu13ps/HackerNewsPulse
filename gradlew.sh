#!/bin/bash

# Attempt to set APP_HOME
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG="$(dirname "$PRG")/$link"
    fi
done
SAVED="$(pwd)"
cd "$(dirname "$PRG")/" >/dev/null
APP_HOME="$(pwd -P)"
cd "$SAVED" >/dev/null

# Attempt to set JAVA_HOME if it's not already set.
if [ -z "$JAVA_HOME" ] ; then
    if [ "$(uname)" = "Darwin" ] ; then
        [ -x '/usr/libexec/java_home' ] && export JAVA_HOME=$(/usr/libexec/java_home)
    fi
    if [ -z "$JAVA_HOME" ] ; then
        if [ "$(uname)" = "Linux" ] ; then
            if [ -x /usr/bin/java ] ; then
                java_link=$(readlink -f /usr/bin/java)
                java_home=$(dirname $(dirname ${java_link}))
                export JAVA_HOME=${java_home}
            fi
        fi
    fi
    if [ -z "$JAVA_HOME" ] ; then
        java_exe_path=$(which java)
        if [ -n "$java_exe_path" ] ; then
            java_home=$(dirname $(dirname $(readlink -f ${java_exe_path})))
            export JAVA_HOME=${java_home}
        fi
    fi
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVA_HOME/bin/java" "$@" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"