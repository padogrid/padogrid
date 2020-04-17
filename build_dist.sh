#/bin/bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
. $SCRIPT_DIR/.argenv.sh

EXECUTABLE="`basename $0`"

if [ "$1" == "-?" ]; then
cat <<EOF
NAME
   $EXECUTABLE - Build padogrid along with all required files such
                 as Unix man pages

SYNOPSIS
   ./$EXECUTABLE [-skipMan] [-?]

   Builds padogrid along with all required files such as Unix man pages.
   Unlike build_all.sh, it does not build apps.

OPTIONS
   -skipMan
             If specified, then skips building man pages

DEFAULT
   ./$EXECUTABLE

EOF
   exit
fi

# Set DEBUG to "true" to skip mvn build if the build directory
# has already been created.
DEBUG="false"

if [ "$DEBUG" == "false" ]; then
   # TSLv1.2 required for older version of macOS
   mvn clean -Dhttps.protocols=TLSv1.2 -DskipTests install
fi

# Get the addon version number
VERSION=`grep "<version>.*<\/version>" pom.xml` 
# Pick the first version tag.
for i in $VERSION; do
   VERSION=$i
   break;
done
VERSION=${VERSION#<version>}
VERSION=${VERSION%<\/version>}

if [ "$SKIP_MAN" == "false" ]; then
   # Untar the distribution file in the build directory.
   if [ ! -d build ]; then
      mkdir -p build
   fi

   if [ "$DEBUG" == "false" ]; then
   if [ -d build/padogrid_${VERSION} ]; then
      rm -Rf build/padogrid_${VERSION}
   fi
   if [ -d build/padogrid-all_${VERSION} ]; then
      rm -Rf build/padogrid-all_${VERSION}
   fi
   tar -C build/ -xzf padogrid-deployment/target/assembly/padogrid_${VERSION}.tar.gz
   fi

   # Build man pages
   echo "Building man pages... This may take some time to complete."
   ./create_man_files.sh

   # tar up the distribution which now includes man pages
   tar -C build -czf padogrid-deployment/target/assembly/padogrid_${VERSION}.tar.gz padogrid_${VERSION}
   pushd build > /dev/null 2>&1
   zip -q -r ../padogrid-deployment/target/assembly/padogrid_${VERSION}.zip padogrid_${VERSION}
   popd > /dev/null 2>&1
fi

echo ""
echo "The following distrubution files have been generated."
echo ""
echo "Cluster Distribution (Light): Some apps need to be built by executing 'bin_sh/build_app'"
echo ""
echo "   padogrid-deployment/target/assembly/padogrid_${VERSION}.tar.gz"
echo "   padogrid-deployment/target/assembly/padogrid_${VERSION}.zip"
echo ""
