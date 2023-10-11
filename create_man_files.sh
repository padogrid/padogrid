#!/usr/bin/env bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
. $SCRIPT_DIR/.argenv.sh

EXECUTABLE="`basename $0`"

if [ "$1" == "-?" ]; then
cat <<EOF
NAME
   $EXECUTABLE - Creates Unix man files in the padogrid distribution

SYNOPSIS
   ./$EXECUTABLE [-coherence] [-?]

   Creates Unix man files in the padogrid distribution. This command is executed by the
   'build_*.sh' commands. Do not execute it directly.

NOTES
   The man pages are generated using the command usage outputs obtained by executing each
   command with the '-?' option. The '-?' option may output error messages for those commands
   that run Java executables if they are not in the class path. You can ignore the error
   messages and build an incomplete list of man pages or you can set CLASSPATH.

OPTIONS
   -coherence
             If specified, then in addition to other modules it also builds
             Coherence man pages. Note that you may need to install Coherence
             manually in the local Maven repository for this option to work.
             Please see the follwoing file for details.

             coherence-addon-core/README.md

EOF
   exit
fi

# OS_NAME in uppercase
OS_NAME=`uname`
OS_NAME=`echo "$OS_NAME"|awk '{print toupper($0)}'`

TMP_DIR=build-tmp
MAN_TOP_DIR=share/man
MAN_DIR=$MAN_TOP_DIR/man1

function trimString
{
    local var="$1"
    var="${var##*( )}"
    var="${var%%*( )}"
    echo -n "$var"
}


CREATION_DATE="`date "+%m/%d/%Y"`"

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

BASE_DIR=`pwd`

PATH="$BASE_DIR/build/padogrid_${VERSION}/bin_sh:$PATH"
__PATH=$PATH


PRODUCTS="common geode hazelcast mosquitto redis snappydata spark kafka hadoop none"

if [ "$COHERENCE_SPECIFIED" == "true" ]; then
   PRODUCTS="$PRODUCTS coherence"
fi

for __PRODUCT in $PRODUCTS; do
   # Build man pages
   echo "Building man pages: $__PRODUCT..."

   # We set PATH to the product bin directory so that the printSeeAlso 
   # function can properly build SEE ALSO.
   if [ "$__PRODUCT" == "common" ]; then
      pushd build/padogrid_${VERSION} > /dev/null 2>&1
      PATH="$BASE_DIR/build/padogrid_${VERSION}/hazelcast/bin_sh:$__PATH"
   else
      pushd build/padogrid_${VERSION}/$__PRODUCT > /dev/null 2>&1
      PATH="$BASE_DIR/build/padogrid_${VERSION}/$__PRODUCT/bin_sh:$__PATH"
   fi
   if [ ! -d $TMP_DIR ]; then
      mkdir -p $TMP_DIR
   fi
   if [ ! -d $MAN_DIR ]; then
      mkdir -p $MAN_DIR
   fi
   COMMANDS=""
   for i in bin_sh/*; do
      if [ "$i" == "bin_sh/cp_sub" ] || [ "$i" == "bin_sh/tools" ] || [[ "$i" == *".sh" ]]; then
         continue;
      fi
      COMMANDS="$COMMANDS $i"
   done
   if [ -d "bin_sh/cp_sub" ]; then
      for i in bin_sh/cp_sub/*; do
         if [[ "$i" == *".sh" ]]; then
            continue;
         fi
         COMMANDS="$COMMANDS $i"
      done
   fi
   if [ -d "bin_sh/tools" ]; then
      for i in bin_sh/tools/*; do
         if [[ "$i" == *".sh" ]]; then
            continue;
         fi
         COMMANDS="$COMMANDS $i"
      done
   fi
   for i in $COMMANDS; do 
      COMMAND_NAME="`basename $i`"
      # Skip pado executable. Requires PADO_HOME and man format.
      if [ "$COMMAND_NAME" == "pado" ]; then
        continue;
      fi
      $i -? -man > $TMP_DIR/${COMMAND_NAME}.txt
      MAN_FILE=$MAN_DIR/${COMMAND_NAME}.1
   
      echo ".TH \"$COMMAND_NAME\" \"1\" \"$CREATION_DATE\" \"padogrid $VERSION\" \"PadoGrid Manual\"" > $MAN_FILE
   
      section=""
      cluster_in_progress=false
      while IFS= read -r line; do
         if [ "$line" == "WORKSPACE" ]; then
            section="WORKSPACE"
         elif [ "$line" == "NAME" ]; then
            section="NAME"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "SYNOPSIS" ]; then
            section="SYNOPSIS"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "DESCRIPTION" ]; then
            section="DESCRIPTION"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "OPTIONS" ]; then
            section="OPTIONS"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "NOTES" ]; then
            section="NOTES"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "DEFAULT" ]; then
            section="DEFAULT"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "FILES" ]; then
            section="FILES"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "PROPERTIES" ]; then
            section="PROPERTIES"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "EXAMPLES" ]; then
            section="EXAMPLES"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "SEE ALSO" ]; then
            section="SEE ALSO"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "ALIASES" ]; then
            section="ALIASES"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "COMMANDS" ]; then
            section="COMMANDS"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "WARNING" ]; then
            section="WARNING"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "IMPORTANT" ]; then
            section="IMPORTANT"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "CAUTION" ]; then
            section="CAUTION"
            echo ".SH $section" >> $MAN_FILE
            continue
         elif [ "$line" == "SUMMARY" ]; then
            section="SUMMARY"
            echo ".SH $section" >> $MAN_FILE
            continue
         fi
   
         # trim string
         line=`trimString "$line"`
         if [ "$section" == "WORKSPACE" ]; then
            continue
         elif [ "$section" == "NAME" ]; then
            echo "$line" >> $MAN_FILE
         elif [ "$section" == "SYNOPSIS" ]; then
            echo "$line" >> $MAN_FILE
         elif [ "$section" == "DESCRIPTION" ]; then
            if [[ $line == minikube* ]]; then
               echo ".IP" >> $MAN_FILE
               echo ".nf" >> $MAN_FILE
               echo "\f[C]" >> $MAN_FILE
               echo "$line" >> $MAN_FILE
            elif [[ $line == gke* ]]; then
               echo "$line" >> $MAN_FILE
               echo "\f[]" >> $MAN_FILE
               echo ".fi" >> $MAN_FILE
               echo ".PP" >> $MAN_FILE
            else
               echo "$line" >> $MAN_FILE
            fi
         elif [ "$section" == "OPTIONS" ]; then
            if [[ $line == \-* ]] || [ "$line" == "app_name" ] || [ "$line" == "cluster_name" ] || [ "$line" == "workspace_name" ]; then  
               echo ".TP" >> $MAN_FILE
               echo ".B $line" >> $MAN_FILE
            elif [[ $line == clusters/* ]] || [[ $line == pods/* ]] || [[ $line == apps/* ]] || [[ $line == minkube* ]]; then
               if [ "$cluster_in_progress" == "false" ]; then
                  cluster_in_progress=true
                  echo ".RS" >> $MAN_FILE
                  echo ".RE" >> $MAN_FILE
                  echo ".IP" >> $MAN_FILE
                  echo ".nf" >> $MAN_FILE
                  echo "\f[C]" >> $MAN_FILE
               fi
               echo "$line" >> $MAN_FILE
            elif [[ $line == https://* ]]; then
               echo ".IP" >> $MAN_FILE
               echo ".nf" >> $MAN_FILE
               echo "\f[C]" >> $MAN_FILE
               echo "$line" >> $MAN_FILE
               echo "\f[]" >> $MAN_FILE
               echo ".fi" >> $MAN_FILE
            else
               if [ "$cluster_in_progress" == "true" ]; then
                  cluster_in_progress=false
                  echo "\f[]" >> $MAN_FILE
                  echo ".fi" >> $MAN_FILE
               fi
               echo "$line" >> $MAN_FILE
            fi
         else
            echo "$line" >> $MAN_FILE
         fi
   
      done < "$TMP_DIR/${COMMAND_NAME}.txt"
   done
   
   WHATIS_CREATED="false"
   if [[ ${OS_NAME} == DARWIN* ]]; then
      if [ -f /usr/libexec/makewhatis ]; then
         /usr/libexec/makewhatis $MAN_TOP_DIR/
         WHATIS_CREATED="true"
      fi   
   elif [ "`which mandb`" != "" ]; then
      mandb $MAN_TOP_DIR/
      WHATIS_CREATED="true"
   fi
   if [ -d $TMP_DIR ]; then
      rm -r $TMP_DIR
   fi
   if [ "$WHATIS_CREATED" == "false" ]; then
      echo ""
      echo "create_man_files:"
      echo "WARNING: Unable to create whatis database due to 'makewhatis' not available on this OS."
      echo ""
   fi
   popd > /dev/null 2>&1
 done
