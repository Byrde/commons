#!/bin/bash

sbt -Dname="$NAME" -Dorganization="$ORGANIZATION" -DscalaVersion="$TRAVIS_SCALA_VERSION" -Dversion="${MAJOR_VERSION}.${MINOR_VERSION}.$TRAVIS_BUILD_NUMBER" 'set credentials += Credentials("Cloudsmith API", "maven.cloudsmith.io", "token", "${CLOUDSMITH_API_KEY}")' publish