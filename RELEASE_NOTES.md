# PadoGrid Release Notes

----

## Version 0.9.5-SNAPSHOT

### Release Date: 03/13/21

- Fixed the command completion ending issue. The command completion now blocks if all options have been exhausted.
- Added support for naming workspaces for `install_bundle` and `create_bundle`. Using the `-workspace` option you can now set a workspace name when you install or create a bundle.
- Tidied up command auto-completion with additional support for changing directory for `cd_*` and `switch_*` commands. You can now drill into the target directory by hitting the \<tab\> key. 
- `show_bundle` now prints the bundle URLs. You can click on the URLs to view the bundle instructions in the web browser. macOS: Command-Mouse, Windows: Control-Mouse.
- Added PDX/Avro code generator for Geode/GemFire.
- Added Kryo/Avro support for Geode/GemFire.
- Merged Debezium JSON support between Hazelcast and Geode.
- Added `find_padogrid` for searching files in PadoGrid. You can now perform a global search on your RWEs for any files that are part of workspace installations.
- Added support for DB ingestion and updated `test_group` with `clear` support in Geode.

----

## Version 0.9.4

### Release Date: 01/10/21

- Fixed a VM synchronization and vm key bug.
- RWE now selects the first workspace found as the default workspace if the default workspace is not defined.
- Added Jet core module that includes support for Kafka stream aggregator. The aggregator is a Jet sink job capable of transforming Kafka events from multiple topics into an object graph based on temporal entity relationships.
- `perf_test` now supports data structures other than Hazelcast IMap. Supported data structures are IMap, ReplicatedMap, ICache, ITopic, ReplicatedTopic, and IQueue.
- `perf_test` now supports the `sleep` operation for simulating workflows.
- Added integrated support for Pado. You can now create a Pado cluster using the `create_cluster -type pado` command, which creates a Pado cluster that includes the ETL, security and test tools.
- Pado Desktop is also available for managing Pado clusters.
  
----

## Version 0.9.3

### Release Date: 09/23/20

- Added support for all JDK version. Further tests required.
- Added support for generating '-all' dependencies. Each product module now has a `padogrid-<product>-all-*.jar` file containing a complete set compiled classes. You can now include just a single `-all` jar file in your class path.
- Added the `-workspace` option to `install_bundle` for checking out bundles in the form of workspaces. With this option, you can now develop and test online bundles from workspaces and check in your changes directly from there.
- Added support for Hazelcast ReplicatedMap in the Debezium connector.
  
----

## Version 0.9.2

### Release Date: 07/25/20

- Added AVRO and Kryo code generators for Hazelcast.
- Added cluster support for SnappyData/ComputeDB.
- Added cluster support for Coherence.
- Added support for Gitea. You can now create catalogs in private Gitea repos
- Added Minishift support for Hazelcast.
- Added OpenShift support for Hazelcast.
- Added Jet 4.x support for the `jet_demo` app.

----

## Version 0.9.1

### Release Date: 04/20/20

- Initial consolidated release.
- Most of the merged features have been tested and fully operational.
- In need of additional clean up in the area of docs, VMs, and common commands.
- This version serves as the base version for supporting additional products other than Geode/GemFire and Hazelcast/Jet.
