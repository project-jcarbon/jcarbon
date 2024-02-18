#!/bin/bash

if [ -z "${JAVA_HOME}" ]
then 
    export JAVA_HOME=$(dirname $(dirname $(readlink -f /usr/bin/java)))
fi

if [ -z "${JAVA_HOME}" ]
then 
    echo 'no JAVA_HOME could be found! you may need to manually set it'
    exit
fi

mvn clean package install
sudo LD_LIBRARY_PATH="${PWD}/src/native/target:${LD_LIBRARY_PATH}" \
    java -cp src/jrapl/target/jrapl-1.0-jar-with-dependencies.jar jrapl.SmokeTest
