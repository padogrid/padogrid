# PadoGrid Release Notes

©2020-2022 Netcrest Technologies, LLC. All rights reserved.
https://github.com/padogrid

## Version 0.9.15-SNAPSHOT

### Release Date: 05/02/22

- Updated Hazelcast OSS and Enterprise download URLs for `install_padogrid`. Hazelcast has changed the download URLs.
- Updated Coherence port number and http blocker for Maven build.
- Added support for gemfire and coherence products in `update_products`.
- Added timestamps to `nw` data classes for Hibernate to auto-update timestamps in databases. For non-database apps, the timestamps are set when they are created in the JVM.
- Added desktop support for Hazecast 5.x
- Added ER support for Hazelcast rmap, cache, queue, topic, rtopic, and (i)map. The `test_group` command in the `perf_test` app now includes support for recursively ingesting mock data with entity relationships.

---

## Version 0.9.14

### Release Date: 03/04/22

- Upgraded library versions including log4j that fixes security issues.
- Fixed `find_padogrid` to properly handle component types.
- Fixed `install_bundle` which set the wrong current workspace name when the `-download` option is specified.
- `show_jupyter` now executes `jupyter notebook list` instead of `jupyter lab list` which no longer works.

---

## Version 0.9.13

### Release Date: 01/16/22

- Added support for installing downloaded zip bundles. You can now download online bundles in the form of zip files and install them using `install_bundle`.
- Added support for installing PadoGrid without products. Previously, PadoGrid required at least one supported product locally installed before it can be operational. Starting this release, products are not required when installing PadoGrid and creating workspaces. This means you can now install and run applications on Kubernetes, Docker, and Vagrant VMs without having to locally install the corresponding products. Unfortunately, however, this also means the workspaces created with the previous releases are no longer compatible. You must manually migrate the existing workspaces by following the instructions provided in the [Migrating Workspaces](https://github.com/padogrid/padogrid/wiki/Migrating-Workspaces) of the PadoGrid manual.

----

## Version 0.9.12

### Release Date: 11/10/21

- Added support for stanalone cache servers in Geode/GemFire. The `start_member` script now includes `-standalone` option to start standalone members requiring no locators. The `standalone.enabled` property is also available in the `etc/cluster.properties` file. This options is particularly useful for launching standalone servers in edge devices that are unable to reach locators.
- Added installation support for Pado, PadoDesktop, and PadoWeb. This support is also available in the 0.9.11 release build.
- Fixed a bug in `stop_*` and `kill_*` commands that failed to execute remote commands in Vagrant pod VMs.
- Added the `subscribe_topic` script in the Hazelcast `perf_test` app for listening on topic messages.
- By default, `read_cache` and `subscribe_topic` now fail if the specified data structure name does not exist in the cluster. You can create non-existent data structures by specifying the `-create-map` or `-create-topic` option.
- Fixed a Java 11+ debug enablement issue. Previous versions supported only Java 8.

----

## Version 0.9.11

### Release Date: 09/25/21

- Updated most of the online bundles to support Hazelcast 5.x.
- Added support for Hazelcast 5.x. Hazelcast 5.x unifies IMDG and Jet into a single product. With this support, PadoGrid also combines IMDG and Jet specifics into a single library. PadoGrid supports Hazelcast 3.x, 4.x, and 5.x as well as Jet 3.x and 4.x.
- For Hazelcast 5.x, the cluster type is now always "hazelcast". The "imdg" and "jet" cluster types are no longer supported for Hazelcast 5.x clusters.
- Added Hazelcast app instance names. You can now see PadoGrid apps in the management center as `PadoGrid-perf_test` and `hazelcast-desktop`.
- Refactored initialization scripts.
- Added PadoWeb support that includes the new commands, `start_padoweb`, `stop_padoweb`, and `show_padoweb`. These commands are accessible from the Geode/GemFire clusters only. The `update_products` command now includes support for PadoWeb. PadoWeb provides IBiz web services to Pado clients.
- Added preliminary support for PadoDesktop which can now be installed by running `create_app -app padodesktop`.
- The Hazelcast management center (mc) and padoweb commands now pertain to their relevant clusters. For example, `start_mc -cluster mygeode` will fail if the specified cluster, `mygeode`, is not a Hazelcast cluster.
- Fixed Vagrant VM member numbering issues. The member numbers were incorrectly incremented which led to non-default port numbers and missing working directories.
- Fixed the `start_mc` command that ignored HTTPS configuration. This was due to a typo in the script.

----

## Version 0.9.10

### Release Date: 08/26/21

- Fixed a bug that improperly initialized PadoGrid. In the previous release, if an app is run immediately after the rwe `initenv.sh` is directly sourced in from `.bashrc`, for example, then the app does not recognize the cluster product and fails with a "not found" error. This fix only applies to new RWEs created by this release. If you have existing RWE's then you must append the following at the end of their `initenv.sh` file.

```bash
if [ -f "$PADOGRID_WORKSPACE/.workspace/workspaceenv.sh" ]; then
   . "$PADOGRID_WORKSPACE/.workspace/workspaceenv.sh"
fi
if [ -f "$PADOGRID_WORKSPACE/clusters/$CLUSTER/.cluster/clusterenv.sh" ]; then
   . "$PADOGRID_WORKSPACE/clusters/$CLUSTER/.cluster/clusterenv.sh"
fi
export CLUSTER
export CLUSTER_TYPE
export POD
export PRODUCT
```
- Fixed a Linux bug in `update_products` that printed the following message.
  ```console
  sed: can't read 0: No such file or directory
  ```

----

## Version 0.9.9

### Release Date: 08/16/21

- Added preliminary support for Kafka. Kafka support is limited to the new RAFT mode and hence requires Kafka 2.8.0 or a later version. Kafka support is limited to local clusters.
- Added preliminary support for Hadoop running in the semi-pseudo mode with support for mutiple data nodes. Hadoop support is limited to local clusters.
- Added java support in `update_products`, which is now preferred over `change_version`.
- Reassigned default ports to prevent port conficts between clusters. See [**Default Port Numbers**](https://github.com/padogrid/padogrid/wiki/Default-Port-Numbers) for details.
- Added full support for Jupyter and VS Code. They are no longer expermimental. Please see the [Integrated Tools](https://github.com/padogrid/padogrid/wiki/Integrated-Tools) section for details.
- The `list_*` commands now support `-rwe` for listing components in a specific RWE and workspace.

----

## Version 0.9.8

### Release Date: 07/24/21

- `padogrid -?` now displays a complete list of commands grouped by components with short descriptions.
- Added the `-product` option to `create_docker`,`create_k8s`, and `create_app` to target a specific product.
- Added support for managing cluster groups. With this support, a group of clusters can be created and managed using the new `_group` commands. These commands are particularly useful for managing Pado (federated grids) or dependent clusters. Heterogeneous products are allowed in a group.
- Added the entry point, `init_bundle.sh`, for intializing bundle during installation. If this script exists in the root directory of the bundle then the `install_bundle` triggers it upon completion of bundle installation. Note that this applies to workspace bundles only, i.e., bundles must be installed by using `install_bundle -checkout` or `install_bundle -workspace -download`.
- Added the `open_vscode` command that integrates PadoGrid workspaces with Visual Studio Code.
- Docker cluster support is now compliant with Hazelcast 4.x changes.

----

## Version 0.9.7

### Release Date: 06/27/21

- Added experimental Jupyter commands for creating a PadoGrid/Jupyter workspace integrated environment. These commands are subject to change in future releases.
- Added support for installing bundles with multiple products and versions. The new bundle installation mechanism introduces the `required_products.txt` file for listing product versions. You must now provide a complete list of products and their versions that are required to install and run the bundle in this file. The previous file name based, single product installation method has been deprecated and its support will eventually be dropped. 
- Added `update_products` for interatively updating product versions.
- Fixed the logic that incorrectly set the cluster type when creating a Geode/GemFire cluster.
- Product paths are now correctly reset for workspaces hosting heterogeneous products. Prior to this fix, the switched workspace continue to use the previous workspace's products even though it may not have defined them.

----

## Version 0.9.6

### Release Date: 05/31/21

- Added support for managing Pado. In anticipation of the upcoming release of Pado that runs on the latest versions of Geode and GemFire, PadoGrid now includes support for managing Pado grids.
- Added `make_cluster` for creating a cluster with a product of your choice. Unlike `create_cluster` which is product specific, `make_cluster` allows you to specify any of the supported products.
- Added `show_products` for displaying installed product versions with the current workspace version hightlighted.
- Added the `install_padogrid` command for automatically installing one or more products. By default, it runs in the interactive mode, providing product version lists from which you can select products to install. For auto-installation, run `install_padogrid -quiet` which non-interactively installs PadoGrid along with all the latest downlodable products. Note that it also creates a new RWE if you are installing PadoGrid for the first time in the specified PadoGrid environment base path.
- Added `switch_pod` and `pwd_pod` for pod context switching.
- Added support for running heterogeneous cluster products in a single workspace. With this support, you can now create clusters for any products in a local workspace and run them concurrently, provided that there are no port conflicts. Please see the [**Default Port Numbers**](https://github.com/padogrid/padogrid/wiki/Default-Port-Numbers) section in the manual for details.
- Added lifecycle management support for Spark which joins the growing list of clustering products natively supported by PadoGrid out of the box. This release supports the Spark's "standalone" deployment option.
- The pod VMs are now configured without the Avahi network discovery service by default. To enable Avahi, specify the `-avahi` option when executing the `create_pod` command.
- Added pod management commands, `show_pod`, `list_pods`, `switch_pod`, `cd_pod`. With these commands, you can now manage pods like other first-class components, i.e., clusters, workspaces and RWEs.
- Clusters created in the VM-enabled workspaces can now seamlessly run with or without pods. If a cluster is attached to a pod, then it automatcially inherits the workspace's VM configuration, allowing you to manage clusters from either the host OS or any of the guest OS VMs.
- Vagrant VMs can now be logged in without password. Examples: `ssh vagrant@pnode.local`, `ssh vagrant@node-01.local`, etc.
- Vagrant pods are now configured as VMs.

### Known Issues

- For cluster management, Cygwin support is limited to Hazelcast and Coherence. The other products may not work due to limitations and bugs in their respective scripts. To support the remaining products, PadoGrid will include extended product scripts in the next lrease. Note that non-cluster features are fully supported for all products on Cygwin.
- This release may not be fully compatible with the previous releases. You might encounter cluster and auto-completion issues if your workspaces were created prior to this release. This is due to the addition of the new support for hosting heterogeneous products per workspace. To avoid this problem, please migrate your workspaces to this release by following the instructions provided in the [**Migrating Workspaces**]((https://github.com/padogrid/padogrid/wiki/Migrating-Workspaces) section of the PadoGrid manual.
- `start_pod` cannot be restarted pods. The pod managment facility is currently undergoing strutural changes to provide additional commands that can be executed outside of the pods. You will need to remove and rebuild the pod until this bug is fixed.

----

## Version 0.9.5

### Release Date: 03/18/21

- Changed local settings of Vagrant pods to run as VMs.
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
