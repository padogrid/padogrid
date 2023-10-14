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

RWE="rwe-build"
WORKSPACE="myws"
PADOGRID_ENV_BASE_PATH="$BASE_DIR/build/Padogrid"
PADOGRID_WORKSPACES_HOME="$PADOGRID_ENV_BASE_PATH/workspaces/$RWE"
PRODUCT="none"
PADOGRID_HOME="$PADOGRID_ENV_BASE_PATH/products/padogrid_${VERSION}"
PADOGRID_WORKSPACE="$PADOGRID_WORKSPACES_HOME/myws"

TMP_ENV_FILE=/tmp/$EXECUTABLE-$(date "+%m%d%y%H%M%S").sh
echo "PADOGRID_HOME=\"$PADOGRID_HOME\"" > $TMP_ENV_FILE
echo "PADOGRID_ENV_BASE_PATH=\"$PADOGRID_ENV_BASE_PATH\"" >> $TMP_ENV_FILE
echo "PADOGRID_WORKSPACES_HOME=\"$PADOGRID_WORKSPACES_HOME\"" >> $TMP_ENV_FILE
echo "PADOGRID_WORKSPACE=\"$PADOGRID_WORKSPACE\"" >> $TMP_ENV_FILE
echo "PRODUCT=\"none\"" >> $TMP_ENV_FILE
echo "JAVA_HOME=\"$JAVA_HOME\"" >> $TMP_ENV_FILE

PATH="$PADOGRID_HOME/bin_sh:$PATH"
__PATH=$PATH

if [ -d "$PADOGRID_WORKSPACES_HOME" ]; then
   rm -rf "$PADOGRID_WORKSPACES_HOME"
fi

$PADOGRID_HOME/bin_sh/create_rwe -rwe $RWE -workspace $WORKSPACE -quiet -env $TMP_ENV_FILE
rm $TMP_ENV_FILE

. $PADOGRID_WORKSPACES_HOME/initenv.sh -quiet

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
      pushd $PADOGRID_HOME > /dev/null 2>&1
      PATH="$PADOGRID_HOME/hazelcast/bin_sh:$__PATH"
   else
      pushd $PADOGRID_HOME/$__PRODUCT > /dev/null 2>&1
      PATH="$PADOGRID_HOME/$__PRODUCT/bin_sh:$__PATH"
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
   prev_first_word=""
   summary_in_progress="false"
   summary_section=""
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
         elif [ "$section" == "SUMMARY" ]; then
            # padogrid's SUMMARY needs to be custom indented due to
            # sub-bullets.
            first_word=$(echo $line | awk '{print $1}')
            case $first_word in
               Prefixes|Postfixes|APP|BUNDLE|CLUSTER|DATANODE|DOCKER|GROUP|K8S|LEADER|LOCATOR|MASTER|MEMBER|NAMENODE|POD|RWE|VM|WORKER|WORKSPACE|TOOLS|CP|Virtual|MISCELLANEOUS)
                  echo ".SS $line" >> $MAN_FILE
                  summary_section="$first_word"
                  prev_first_word=""
                  summary_in_progress="true"
                  ;;
               *)
                  if [ "$prev_first_word" != "" ]; then
                     line="                    $line"
                     prev_first_word=""
                     echo "$line" >> $MAN_FILE
                  elif [ ${#first_word} -ge 27 ]; then
                     prev_first_word=$first_word
                     if [ "$summary_in_progress" == "true" ] && [ "$first_word" != "" ]; then
                        echo ".TP" >> $MAN_FILE
                        echo "$line" >> $MAN_FILE
                     fi
                  else
                     if [ "$summary_in_progress" == "true" ] && [ "$first_word" != "" ] && [ "$summary_section" != "Postfixes" ]; then
                        echo ".TP" >> $MAN_FILE
                     fi
                     echo "$line" >> $MAN_FILE
                  fi
                  ;;
            esac
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
