#/bin/bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
. $SCRIPT_DIR/.argenv.sh

EXECUTABLE="`basename $0`"

if [ "$1" == "-?" ]; then
cat <<EOF
NAME
   $EXECUTABLE - Build padogrid along with all required files such as Unix man pages

SYNOPSIS
   ./$EXECUTABLE [-all] [-man] [-coherence] [-?]

   Builds padogrid without man pages and Coherence by default.
    
   Unlike build_all.sh, it does not build apps. Note that by default it builds
   man pages which make take a few minutes to complete. To skip building
   man pages specify the 'skipMan' option.

   To include man pages specify the '-man' option. Note that generating man pages 
   may take a few minutes to complete. 

   By default, the Coherence module is not included due to the lack of public Maven
   packages. You must manually install the Coherence package as described
   in the following file before specifying the '-coherence' option to include
   the Coherence module in the build.

   coherence-addon-core/README.md

NOTES
   The man pages are generated using the command usage outputs obtained by executing each
   command with the '-?' option. The '-?' option may output error messages for those commands
   that run Java executables if they are not in the class path. You can ignore the error
   messages and build an incomplete list of man pages or you can set CLASSPATH.

OPTIONS
   -all
             If specified, then builds all modules and generates man pages.

   -man
             If specified, then generate man pages for all modules.

   -coherence
             If specified, then includes the coherence moudle in the build.
             Note that you may need to install Coherence manually in the local 
             Maven repository for this option to work. Please see the following
             file for details.

             coherence-addon-core/README.md

DEFAULT
   ./$EXECUTABLE

EOF
   exit
fi

# Set DEBUG to "true" to skip mvn build if the build directory
# has already been created.
DEBUG="false"

if [ "$DEBUG" == "false" ]; then
   # TSLv1.2 required for older versions of macOS
   if [ "$COHERENCE_SPECIFIED" == "true" ]; then
      mvn clean -Dhttps.protocols=TLSv1.2 -DskipTests install -Pcoherence
   else
      mvn clean -Dhttps.protocols=TLSv1.2 -DskipTests install 
   fi
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

#PADOGRID_ENV_BASE_PATH=`pwd`/build
#PADOGRID_HOME=$PADOGRID_ENV_BASE_PATH/padogrid_$VERSION

if [ "$MAN_SPECIFIED" == "true" ]; then
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
   if [ "$COHERENCE_SPECIFIED" == "true" ]; then
      ./create_man_files.sh -coherence
   else
      ./create_man_files.sh
   fi

   # tar up the distribution which now includes man pages
   tar -C build -czf padogrid-deployment/target/assembly/padogrid_${VERSION}.tar.gz padogrid_${VERSION}
   if [ "$(which zip)" == "" ]; then
      echo "Unable to create a zip file due to missing 'zip' command."
   else
      pushd build > /dev/null 2>&1
      zip -q -r ../padogrid-deployment/target/assembly/padogrid_${VERSION}.zip padogrid_${VERSION}
      popd > /dev/null 2>&1
   fi
fi

echo ""
echo "The following distrubution files have been generated."
echo ""
echo "Cluster Distribution (Light): Some apps need to be built by executing 'bin_sh/build_app'"
echo ""
echo "   padogrid-deployment/target/assembly/padogrid_${VERSION}.tar.gz"
if [ "$(which zip)" != "" ]; then
   echo "   padogrid-deployment/target/assembly/padogrid_${VERSION}.zip"
fi
echo ""
