# Apps

This folder contains Redis client apps that demonstrate padogrid components.

## List of Apps

1. [perf_test](perf_test/) - Performance analysis and troubleshooting test tools.

## Running Apps in Workspaces

All the apps have been preconfigured to run against the default clusters created by the `create_cluster` command. If you change the cluster port numbers or have a multi-homed machine, then you may need to reflect the changes in apps configuration files for the apps to run properly. See each app's `README.md` file for instructions.

## Running against another cluster environment

If you need to run the apps against a cluster that has *not* been launched from the PadoGrid environment, then you must include jars from both `lib/` and `test/` folders as follows:

Unix:
```
CLASSPATH=$PADOGRID_HOME/plugins/*:$PADOGRID_HOME/lib/*:$PADOGRID_HOME/redis/plugins/*:$PADOGRID_HOME/redis/lib/*:$CLASSPATH
```

Windows:
```
set CLASSPATH=$PADOGRID_HOME/plugins/*;$PADOGRID_HOME/lib/*;$PADOGRID_HOME/redis/plugins/*;$PADOGRID_HOME/redis/lib/*;$CLASSPATH
```
