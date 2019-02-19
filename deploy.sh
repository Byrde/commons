#!/bin/bash

echo "Deploying Cloudsmith SBT Example"

# Create .credentials file
cat <<EOF >/home/travis/.sbt/.credentials
realm=Cloudsmith API
host=maven.cloudsmith.io
user=token
password=${CLOUDSMITH_API_KEY}
EOF

# Set Workdir
cd "${TRAVIS_BUILD_DIR}"

sbt -Dname="$NAME" -Dorganization="$ORGANIZATION" -DscalaVersion="$TRAVIS_SCALA_VERSION" -Dversion="${MAJOR_VERSION}.${MINOR_VERSION}.$TRAVIS_BUILD_NUMBER" 'set credentials += Credentials("Cloudsmith API", "maven.cloudsmith.io", "token", "${CLOUDSMITH_API_KEY}")' aetherDeploy