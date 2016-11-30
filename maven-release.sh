#!/usr/bin/env bash
set -v
set -e

git config --global user.email "circleci@specto.io"
git config --global user.name "CircleCI"

PROJECT_VERSION=$(mvn -q \
    -Dexec.executable="echo" \
    -Dexec.args='${project.version}' \
    --non-recursive \
    org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)

echo "Project version: ${PROJECT_VERSION}"
echo "Is release?: ${IS_RELEASE}"
echo "Release version: ${RELEASE_VERSION}"
echo "Next dev version: ${NEXT_DEV_VERSION}"

if [[ "${PROJECT_VERSION}" == *"SNAPSHOT" ]]; then
    echo "Detected snapshot version"

    if [ "${IS_RELEASE}" = true ]; then
        sudo apt-get -yq update && sudo apt-get -yq install gnupg2
        echo ${GPG_PRIVATE_KEY} | base64 --decode -i | gpg2 --import
        echo "Preparing a release"
        mvn -B -Dtag=${RELEASE_VERSION} release:prepare -DreleaseVersion=${RELEASE_VERSION} -DdevelopmentVersion=${NEXT_DEV_VERSION}
        echo "Performing new release"
        mvn -B -s settings.xml release:perform
        mvn release:clean
    else
        echo "Deploying snapshot version"
        mvn -B -s settings.xml clean deploy
    fi
else
    echo "This commit is a change of release version, so doing nothing (A release was performed by the previous job)"
fi


