# Apps

This folder contains Hazelcast client apps that demonstrate padogrid components.

## List of Apps

1. [perf_test](perf_test/) - Performance analysis and troubleshooting test tools. Measure transaction latencies and monitor GC activities in conjunction with the Grafana app.
2. [Grafana](grafana/) - Out-of-the-box support for Grafana and Prometheus. Monitor the entire Hazelcast cluster(s) in a single view.
3. [Desktop](desktop/) - Automatically install hazlecast-desktop and view data in the distributed maps by executing ad-hoc HQL queries.
4. [jet_demo](jet_demo/) - A Jet demo of submitting jobs in a Jet cluster.

## Running Apps in Workspaces

All the apps have been preconfigured to run against the default Geode clusters created by the `create_cluster` command. If you change the cluster port numbers or have a multi-homed machine, then you may need to reflect the changes in apps configuration files for the apps to run properly. See each app's `README.md` file for instructions.

## Running against another cluster environment

If you need to run the apps against a cluster that has *not* been launched from the PadoGrid environment, then you must include jars from both `lib/` and `test/` folders as follows:

Unix:
```
CLASSPATH=$PADOGRID_HOME/plugins/*:$PADOGRID_HOME/lib/*:$PADOGRID_HOME/hazelcast/plugins/*:$PADOGRID_HOME/hazelcast/lib/*:$CLASSPATH
```

Windows:
```
set CLASSPATH=$PADOGRID_HOME/plugins/*;$PADOGRID_HOME/lib/*;$PADOGRID_HOME/hazelcast/plugins/*;$PADOGRID_HOME/hazelcast/lib/*;$CLASSPATH
```
