#!/usr/bin/env bash
set -v
set -e

git config user.email "circleci@specto.io"
git config user.name "CircleCI"

PROJECT_VERSION=($(cat gradle.properties | grep version | cut -d '=' -f 2))

echo "Project version: ${PROJECT_VERSION[0]}"
echo "Is release?: ${IS_RELEASE}"
echo "Release version: ${RELEASE_VERSION}"
echo "Next dev version: ${NEXT_DEV_VERSION}"

sudo apt-get -yq update && sudo apt-get -yq install gnupg2
echo ${GPG_PRIVATE_KEY} | base64 --decode -i | gpg2 --import

if [[ "${PROJECT_VERSION[0]}" == *"SNAPSHOT" ]]; then
    echo "Detected snapshot version"

    if [ "${IS_RELEASE}" = true ]; then
        echo "Performing a release"
        ./gradlew release -Prelease.releaseVersion=${RELEASE_VERSION} -Prelease.newVersion=${NEXT_DEV_VERSION}
    else
        echo "Deploying snapshot version"
        ./gradlew uploadArchives
    fi
else
    echo "This commit is a change of release version, so doing nothing (A release was performed by the previous job)"
fi


