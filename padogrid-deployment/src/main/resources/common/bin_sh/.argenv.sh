QUERY=
REGION=
PREV=

for i in "$@"
do
   if [ "$PREV" == "-query" ]; then
      QUERY=$i
   # this must be the last check
   elif [ "$PREV" == "-region" ]; then
      REGION=$i
   fi
   PREV=$i
done
