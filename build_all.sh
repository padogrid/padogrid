#/bin/bash

EXECUTABLE="`basename $0`"

if [ "$1" == "-?" ]; then
cat <<EOF
NAME
   $EXECUTABLE - Build all the apps by executing their 'bin_sh/build_app' command

SYNOPSIS
   ./$EXECUTABLE [-?]

   Builds all the apps by executing their 'bin_sh/build_app' command and creates
   the 'all' distribution file that contains the apps that are fully compiled
   and ready to run.

DEFAULT
   ./$EXECUTABLE

EOF
   exit
fi

# TSLv1.2 required for older version of macOS
mvn clean -Dhttps.protocols=TLSv1.2 -DskipTests install

# Get the addon version number
VERSION=`grep "<version>.*<\/version>" pom.xml` 
# Pick the first version tag.
for i in $VERSION; do
   VERSION=$i
   break;
done
VERSION=${VERSION#<version>}
VERSION=${VERSION%<\/version>}
export VERSION

# Untar the distribution file in the build directory.
if [ ! -d build ]; then
   mkdir -p build
fi

if [ -d build/padogrid_${VERSION} ]; then
   rm -Rf build/padogrid_${VERSION}
fi
if [ -d build/padogrid-all_${VERSION} ]; then
   rm -Rf build/padogrid-all_${VERSION}
fi
tar -C build/ -xzf padogrid-deployment/target/assembly/padogrid_${VERSION}.tar.gz

# Build man pages
./create_man_files.sh

# tar up the distribution which now includes man pages
tar -C build -czf padogrid-deployment/target/assembly/padogrid_${VERSION}.tar.gz padogrid_${VERSION}
pushd build > /dev/null 2>&1
zip -q -r ../padogrid-deployment/target/assembly/padogrid_${VERSION}.zip padogrid_${VERSION}
popd > /dev/null 2>&1

# Find all build_app scripts and build them

PRODUCTS="geode hazelcast"
for PRODUCT in $PRODUCTS; do
   pushd build/padogrid_${VERSION}/$PRODUCT > /dev/null 2>&1
   for APP in apps/*; do 
      if [ -f $APP/bin_sh/build_app ]; then
         pushd $APP/bin_sh > /dev/null 2>&1
         chmod 755 ./build_app
         echo "./build_app -clean"
         ./build_app -clean
         popd > /dev/null 2>&1
      fi
   done
   popd > /dev/null 2>&1
done

mv -f build/padogrid_${VERSION}  build/padogrid-all_${VERSION}
tar -C build -czf padogrid-deployment/target/assembly/padogrid-all_${VERSION}.tar.gz padogrid-all_${VERSION}
pushd build > /dev/null 2>&1
zip -q -r ../padogrid-deployment/target/assembly/padogrid-all_${VERSION}.zip padogrid-all_${VERSION}
popd > /dev/null 2>&1

echo ""
echo "The following distrubution files have been generated."
echo ""
echo "1. Cluster Distribution (Light): Some apps need to be built by executing 'bin_sh/build_app'"
echo ""
echo "   padogrid-deployment/target/assembly/padogrid_${VERSION}.tar.gz"
echo "   padogrid-deployment/target/assembly/padogrid_${VERSION}.zip"
echo ""
echo "2. Full Distribution (Heavy): Includes full-blown apps."
echo ""
echo "   padogrid-deployment/target/assembly/padogrid-all_${VERSION}.tar.gz"
echo "   padogrid-deployment/target/assembly/padogrid-all_${VERSION}.zip"
echo ""
