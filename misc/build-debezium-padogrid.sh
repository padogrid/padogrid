#!/bin/bash

EXECUTABLE="`basename $0`"

if [ "$1" == "-?" ]; then
cat <<EOF
NAME
   $EXECUTABLE - Build OpenJDK 8 Debezium Kafka specific images
   
SNAPOPSIS
   ./$EXECUTABLE version [-?]

DESCRIPTION
   Builds OpenJDK 8 version of the folllowing images:
   
   padogrid/debezium-connect-base-openjdk8
   padogrid/debezium-kafka-openjdk8
   padogrid/debezium-connect-openjdk8

   To run this script, follow the steps below.

   1. Clone Debezieum.
   2. Checkout the tag version to build.
   3. Place this script in the cloned Debezieum directory.
   4. Run this script with the same tag minor version number, i.e. 1.1, 1.2, etc.

OPTIONS
   version
             Version number, i.e., 1.1, 1.2, etc.

EXAMPLE
   git clone https://github.com/debezium/docker-images.git
   cd docker-images
   git checkout v1.1.1.Final
   # Note that you must always use the minor version number, i.e., 1.1
   # when executing this script.
   ./$EXECUTABLE 1.1
   
EOF
fi

set -eo pipefail

#
# Parameter 1: image name
# Parameter 2: path to component (if different)
#
build_docker_image () {
    IMAGE_NAME=$1;
    IMAGE_PATH=$2;
    
    if [ -z "$IMAGE_PATH" ]; then
        IMAGE_PATH=${IMAGE_NAME};
    fi
    
    IMAGE_PATH="${IMAGE_PATH}/${DEBEZIUM_VERSION}"

    echo ""
    echo "****************************************************************"
    echo "** Validating  padogrid/${IMAGE_NAME}"
    echo "****************************************************************"
    echo ""
    docker run --rm -i hadolint/hadolint:latest < "${IMAGE_PATH}"

    echo "****************************************************************"
    echo "** Building    padogrid/${IMAGE_NAME}:${DEBEZIUM_VERSION}"
    echo "****************************************************************"
    docker build -t "padogrid/${IMAGE_NAME}:latest" "${IMAGE_PATH}"

    echo "****************************************************************"
    echo "** Tag         padogrid/${IMAGE_NAME}:${DEBEZIUM_VERSION}"
    echo "****************************************************************"
    docker tag "padogrid/${IMAGE_NAME}:latest" "padogrid/${IMAGE_NAME}:${DEBEZIUM_VERSION}"
}


if [[ -z "$1" ]]; then
    echo ""
    echo "A version must be specified."
    echo ""
    echo "Usage:  build-debezium <version>";
    echo ""
    exit 1;
fi

DEBEZIUM_VERSION="$1"

#
# Rename jdk11 to jdk8
#

# OS_NAME in uppercase
OS_NAME=`uname`
OS_NAME=`echo "$OS_NAME"|awk '{print toupper($0)}'`

# SED backup prefix
if [[ ${OS_NAME} == DARWIN* ]]; then
   # Mac - space required
   __SED_BACKUP=" 0"
else
   __SED_BACKUP="0"
fi

sed -i${__SED_BACKUP} 's/openjdk11/openjdk8/' kafka/$DEBEZIUM_VERSION/Dockerfile
sed -i${__SED_BACKUP} 's/debezium\/kafka/padogrid\/debezium-kafka-openjdk8/' connect-base/$DEBEZIUM_VERSION/Dockerfile
sed -i${__SED_BACKUP} 's/debezium\/connect-base/padogrid\/debezium-connect-base-openjdk8/' connect/$DEBEZIUM_VERSION/Dockerfile
sed -i${__SED_BACKUP} 's/debezium\/connect-base/padogrid\/debezium-connect-base-openjdk8/' connect/$DEBEZIUM_VERSION/Dockerfile.local

#
# Build Kafka specifics only
#
build_docker_image debezium-kafka-openjdk8 kafka
build_docker_image debezium-connect-base-openjdk8 connect-base
build_docker_image debezium-connect-openjdk8 connect

echo ""
echo "**********************************"
echo "Successfully created Docker images"
echo "**********************************"
echo ""
