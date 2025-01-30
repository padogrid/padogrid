# PadoGrid Release Notes

© 2020-2025 Netcrest Technologies, LLC. All rights reserved.

https://github.com/padogrid

## Version 1.0.2-SNAPSHOT

### Release Date: 01/30/24

- Added comprehensive sets of Grafana dashboards in the `grafana` app for monitoring the GemFire 10.x Prometheus metrics. Similar to the PadoGrid Hazelcast Dashboards (PHD), the PadoGrid GemFire Dashboards (PGFD) includes three (3) sets of dashboards: `Single`, `Dual` and `All`.

[GemFire Grafana Dashboards](https://github.com/padogrid/padogrid/raw/develop/images/grafana-gemfire-all-screenshot.png?raw=true)

- Added support for automatically importing GemFire dashboards to containers in Docker and Kubernetes environments. See [Docker Hub](https://hub.docker.com/r/padogrid/padogrid-grafana) for installation details.
- The `create_cluster` command for GemFire now enables the Prometheus Metrics Endpoint as default for both locators and members. This change impacts only GemFire 10.1 and later versions. The older versions are enabled with the JMX Exporter Endpoint.
- The GemFire/Geode test packages are now in their own `geode-addon-core-tests` module to allow easier deployment.

---

## Version 1.0.1-SNAPSHOT

### Release Date: 12/28/24

- Added support for GemFire `http-service`, which replaces the deprecated property, `jmx-manager-http-port`, in GemFire 10.x.
- Added support for GemFire Management Console (GFMC). GemFire clusters now include the `start_mc`, `stop_mc`, `show_mc` commands for managing GFMC.
- Added support for the GemFire controlled Prometheus metrics. The `create_cluster` command now includes GemFire specific properties to optionally enable GemFire Prometheus properties.
- Added the missing recovery files used by GemFire tools. These files were inadvently left out in the distribution.
- Added `-node-` to GemFire member names for the tools commands to isolate the member log files. Some of the tools commands in the previous releases did not work due to this issue.

---

## Version 1.0.0

### Release Date: 05/24/24

- This is the first hardened major release that includes support for all commands in the previous release with the following deprecated commands removed.

| Removed             | Replacement          |
| ------------------- | -------------------- |
| `make_cluster`      | `create_cluster`     |
| `show_products`     | `show_padogrid`      |
| `uninstall_product` | `uninstall_padogrid` |
| `update_products`   | `update_padogrid`    | 
| `vm_show_products`  | `vm_show_padogrid`   |

- The online bundles and the PadoGrid manual have been updated with the replacement commands. Please use this release or PadoGrid v0.9.33 to run the bundles. If you choose to use a prior release, then you must use the deprecated (removed) commands.
- This release drops support for Jet 3.x and Jet 4.x, which have been sunset by Hazelcast in 2022. PadoGrid now supports Jet for Hazelcast 5.x only.
- Added the `-network` option to `create_docker` for joining an external network.
- Added a workaround to the SnappyData 1.3.x log file path issue which incorrectly sets the log file path relative to the workding directory. With this workaround, PadoGrid now supports all versions of SnappyData.
- Added SnappyData Docker images, `padogrid/snappydata:1.3.1` and `padogrid/snappydata:1.3.1-HF-1`. 
- To align with Hazelast Platform 5.4+, starting PadoGrid 1.0.0, PadoGrid Docker images now include OpenJDK 17.
- [![PadoGrid 1.x](https://github.com/padogrid/padogrid/wiki/images/padogrid-padogrid-1.x.drawio.svg)](https://github.com/padogrid/padogrid/wiki/Platform-PadoGrid-1.x) If this icon is present, then the underlying artifacts have been tested and certified to run on PadoGrid 1.x.

---

## Version 0.9.33

### Release Date: 02/18/24

- Added Hazelcast Jet job dashboards to Grafana.
- Released [PadoGrid Grafana container image.](https://hub.docker.com/repository/docker/padogrid/padogrid-grafana/general) This inaugural release automatically installs [PadoGrid Hazelcast Dashboards (PHD)](https://github.com/padogrid/padogrid/wiki/Hazelcast-Grafana-App) to Grafana instances running on Docker and Kubernetes.
- Removed the `change_version` command. The `update_products` command replaces this command.
- Added support for creating 'stub' apps. A stub app is useful for creating a product-specific custom app.
- Updated `create_script` for creating a Hazelcast distribution independent of PadoGrid.
- Added `create_datasource_prometheus -url` for creating Prometheus datasources.
- Added VirtualBox 7.x support for host-only networks. With this support, the `create_pod` now requires the first host-only interface IP address. The last octet of the IP address is no longer required as it is now extracted from the IP address.
- Reorganized Vagrant VM directories to conform to the standard PadoGrid directory structure. All of PadoGrid specifics are now placed in the `/home/vagrant/Padogrid` directory.
- Deprecated `*_product*` commands in favor of the new `*_padogrid` commands. All `*_product*` commands will be removed in the future.

---

## Version 0.9.32

### Release Date: 11/28/23

- Fixed numerouse Hazelcast dashboard issues mostly pertaining to improper web links and panel contents. The dashboards have been thoroughly tested in both Kubernetes and local environments.
- Fixed `show_products` to highlight the PadoGrid version in the current cluster context.

---

## Version 0.9.31

### Release Date: 11/18/23

- Added a comprehensive suite of Grafana dashboards for Hazelcast. There are now three types of dashboards: single, dual, and all. The single type monitors a `single` cluster. The `dual` type monitors two (2) clusters side-by-side. The `all` type federates all clusters. Futhermore, dashboards can be switched between data sources and clusters.
- Released the [Hazelcast Multi-Cluster Demo](https://github.com/padogrid/bundle-hazelcast-5-cluster-wan-app-granfana) bundle. This bundle automates multi-cluster installtion steps and includes a full set of Grafana dashboards including a WAN discovery plugin dashboard.
- Updated Grafana scripts to support Kubernetes. This update is also independently added in v0.9.30.

---

## Version 0.9.30

### Release Date: 10/23/23

- Overhauled the scripts to keep the product specifics in their respective `bin_sh` directory in the PadoGrid distribution. This enhancement removes duplicate scripts and significantly reduces the build time.
- Added [Hazelcast Grafana dashboards](https://github.com/padogrid/padogrid/wiki/Hazelcast-Grafana-App) providing the following features.
  - Main console resembling the Management Center
  - Additional metrics not found in the Management Center
  - Support for monitoring multiple Hazelcast clusters
  - A workflow simulator for activating dashboards
  - A complete set of scripts for importing, exporting, and creating templates
  - Kubernetes ready
- Enhanced Prometheus and Grafana scripts. You can now view URL and status of individual Prometheus and Granfana instances.
- Updated the apps to run indepent of the cluster context. With this enhancement, you can now run each app without switching to their respective cluster.

----

## Version 0.9.29

### Release Date: 10/01/23

- Added support for multiple plugins per MQTT virtual cluster. With this support, MQTT connectors now stream MQTT data to multiple target endpoints including Databases, Hazelcast, Geode/GemFire, Kafka, etc.
- Fixed MQTT logging issues that logged incorrect endpoint count status.
- Added `predicate` and `sql` support in `test_group` for Hazelcast `perf_test`. With these parameters, any query predicates and SQL statements can be included in test group workflows. See the [Adding Hazelcast Queries](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/hazelcast/apps/perf_test/README.md#8-adding-hazelcast-queries) section of the `perf_test` documentation for details.
- Updated the Hazelcast `grafana` app.
  - Added the *Queries* dashboard for monitoring Hazelcast query metrics in Grafana.
  - Updated the Hazelcast Grafana templates to align with the metrics that are available on Kubernetes. The [Hazelcast Kubernetes Helm Charts](https://github.com/padogrid/bundle-hazelcast-3n4n5-k8s-kubectl_helm) bundle has been updated with Prometheus/Grafana support.
  - Added the *Hazelcast* folder containing the *System Resources* dashboard that mimic the Hazelcast Management Center main page.
- Migrated the old Table and Graph widgets in Grafana dashboard templates to Time Series widgets.
- Hazelcast clusters created using `make_cluster` or `create_cluster` now include the `hazelcast-indexes.xml` and `hazecast-indexes.yaml` files for testing indexes.
- Fixed hazelcast mc and grafana version update issues. Prior to this fix, `update_products -version` hanged for `hazelcast-mc` and `grafana`.
- Added support for the `-product` option to `create_cluster`. With this change, `make_cluster` is now deprecated.

---

## Version 0.9.28

### Release Date: 08/28/23

- Moved from Alpine to Ubuntu for building PadoGrid container images to provide better support for Python. The Ubuntu community provides more readily available Python binaries. The PadoGrid container (`padogrid/padogrid`) image size has grown significantly due to its inclusion of JupyterLab and Python libaries. For those applications that do not need JupyterLab and Python, the `padogrid/padogrid-base` image is now available. It is built on Alpine and has only PadoGrid, Java, and Mosquitto installed providing a small footprint which is ideal for building edge device images.
- Added the `subscriptions` element to MQTT virtual cluster configuration to enable automatic topic filter subscriptions for plugins. This addition frees the `IHaMqttConnectorSubscriber` plugins from explicitly subscribing to topic filters.
- The default QoS is now set to 1 `HaMqttClient` to be consistent with Paho.
- Fixed Mosquitto PID functions to properly search active members. Prior to this fix, the commands such as `show_cluster` were unable to detect active Mosquitto members.

---

## Version 0.9.27

### Release Date: 07/30/23

- Updated support for installing PadoGrid jar packages in the local Maven repo. The `installMavenPadogridJar` function has been updated to automatically install the padogrid-parent `pom.xml` in the local repo to comply with the latest changes in Maven.
- Enhanced `open_vscode` to support all levels of PadoGrid workspace directory. Previously, only the workspace directory level was supported. VS Code can now be launched from RWE, workspace, component root, and component directories.
- Enhanced `HaMqttClient` to support generic plugins. With this enhancement, `HaMqttClient` is now capable of embedding both MQTT and non-MQTT plugins, allowing applications to provide reusable services. The `vc_*` commands have been outfitted with the new plugin harness to seamlessly embed plugins.
- Added the `-tag` and `-release` options to `install_bundle` for installing versioned online bundles.
- Removed the deprecated `build_app` script from the Hazelcast desktop app. Desktop build is no longer supported and required. All online bundles have been updated accordingly.
- Replaced the deprecated terminal settings for `open_vscode`. This update does not affect the existing VS Code workspaces, but it is recommended to migrate to the new settings by removing the `workspace.code-workspace` file and restarting VS Code with `open_vscode` as follows.

```bash
cd_workspace
rm workspace.code-workspace
open_vscode
```

- Updated the MQTT Docker cluster with the latest simulator bundle.

---

## Version 0.9.26

### Release Date: 06/24/23

- Added Docker support for Mosquitto. The `create_docker` command now supports installation of Mosquitto Docker Compose clusters. Each cluster installs the matching version of `padogrid/padogrid-mqtt` image starting from 0.9.26 and includes an `HaMqttClient` configuration file for creating an Archetype 8 virtual cluster. The `padogrid/padogrid-mqtt` image is equipped with an MQTT data feed simulator, [`bundle-none-app-simulator`](https://github.com/padogrid/bundle-none-app-simulator), which continuously pumps out a variety of data in JSON form. See the [Mosquitto Docker Compose](https://github.com/padogrid/padogrid/wiki/Mosquitto-Docker-Compose) section in the manual for details.
- Added a workaround to a Paho bug that throws an NPE instead of `MqttException` when it encounters a bad connection. `HaMqttClient` now catches and handles all exceptions accordingly. This workaround fixes `vc_subscribe` which exits when an NPE is raised by Paho. Paho v1.2.5.
- Added `mosquitto` to `install_bundle`.
- Fixed Jet 3.x/4.x cluster creation issues. Prior to this fix, Jet clusters incorrectly set `CLUSTER_TYPE` to `hazelcast` causing a conflict between IMDG and Jet.
- Fixed an uncaught NPE issue in `HaMqttClient` potentially caused by null clientID.
- Unlike other products, to prevent jar conflicts, `perf_test` for Kafka now places the jar dependencies only in the app's lib directory.

---

## Version 0.9.25

### Release Date: 05/29/23

- With this release, PadoGrid introduces MQTT *virtual clusters*. A virtual cluster is a cluster that can be formed by a client application on the fly by dropping in any endpoints that are accessible. For example, a client application can create a virtual cluster comprised of 1000's of edge brokers or comprised of brokers on the Internet everywhere. A client application can create any number of virtual clusters, bridge them, and dynamically scale out as needed. See [Clustering MQTT](https://github.com/padogrid/padogrid/wiki/Clustering-MQTT) for details.
- Added clustering support for [Eclipse Mosquitto](https://mosquitto.org/), an open source MQTT broker. PadoGrid clusters Mosquitto brokers as standalone servers and provides HA services via the client API, `HaMqttClient`, which wraps the [Paho API](https://www.eclipse.org/paho/) providing seamless application migration. This release supports MQTT v5. See [Mosquitto Overview](https://github.com/padogrid/padogrid/wiki/Mosquitto-Overview) for details.
- Added support for FoS (Failover Service) in creating MQTT virtual clusters. FoS provides four (4) levels of service for clustering bridged MQTT brokers. See [Clustering MQTT](https://github.com/padogrid/padogrid/wiki/Clustering-MQTT) for details.
- Added `publish_cluster` and `subscribe_cluster` for sending/receiving messages to/from virtual clusters. These commands are fully integrated with PadoGrid's MQTT clusters.
- Fixed `show_log` leaving zombie processes running on VMs. This was caused by `ssh -n`. 
- Fixed a version parsing bug in `install_bundle`. If a bundle name has more than 2 versions, then this bug prevented installing the bundle. A workaround is to use the `install_bundle -force` option.
- Fixed Hazelast 3 and 4 scripts that incorrectly set CLASSPATH for excluding PadoGrid lib which includes log4j binaries. Without this fix, PadoGrid fails to start Hazelast 3/4 clusters.
- Updated the `start_padogrid` script for the latest OpenShift Local.

---

## Version 0.9.24

### Release Date: 01/01/23

- Fixed `install_padogrid` to download the correct versions of ARM64 for Grafana and Prometheus. 
- Fixed `install_padogrid -quiet -product` options to override product scanning with the cached product versions during the initial installation time.
- Added logic in `install_padogrid` to exit if the user does not have write permission.
- Fixed Geode `log4j2` issues by replacing its `log4j2.properties` with Hazelcast's.
- Overhauled `vm_*` commands to leverage the `install_padogrid` command for enabling version-based product deployment.
- Added `vm_show_products` for displaying installed and active products in workspace VMs.
- `start_jupyter` now starts in the foreground to handle the JupyterLab's `Ctrl-C` issue. You can start it in the background by appending `&`, which will preserve `Ctrl-C`. Prior to this change, `Ctrl-C` did not get propagated to child processes impacting blocking commands such as `tail -f` which required a combination of `Ctrl-Z` and `kill` to terminate.
- Added the `-bg` option in 'vm_exec` for executing remote comamnds in the background. This option is analogous to the `-f` option in `ssh`.
- `update_products` now includes the `-version` option for updating product versions in non-interactive mode. 
- Refactored VM rountines to be more consistent throughout all products. 
- Added support for OCI (Oracle Cloud Infrastructure). See [PadoGrid on OCI Compute](https://github.com/padogrid/padogrid/wiki/PadoGrid-on-OCI-Compute) for details.

---

## Version 0.9.23

### Release Date: 12/02/22

- Enhanced several commands to ease bundle installations particularly in containers.
- The `install_bundle` command now includes the `-overwrite-workspace` option for overwriting an existing workspace with bundle workspace contents.
- `create_workspace` now includes a workspace `README.md` file, which serves as your workspace document. Its content should be replaced with your workspace descriptions.
- Added `install_padogrid -version` for installing a specific product version.
- Added `install_padogrid -scan [scan_delay_in_seconds]` to delay between URL scans to prevent the GitHub rate meter from rejecting repetitive calls.
- Added `create_workspace -headless` for excluding workspace header artifacts.
- `install_bundle` now requires the `-init` option to trigger the `.init_workspace.sh` script.
- Added the `-init`, `-overwrite-workspace` options to `install_rwe` and `install_bundle`. The `-init` option initializes bundle workspaces and the `-overwrite-workspace` overwrites existing workspaces.

---

## Version 0.9.22

### Release Date: 11/20/22

- Added support for Confluent Platform. You can now install Confluent Platform by running `install_padogrid -product confluent`. Confluent and Kafka share the same cluster commands. The `CLUSTER_TYPE` value of `kraft` has been replaced with `kafka` and `confluent`. PadoGrid now supports Kafka, Confluent Community, and Confluent Commertial versions.
- Updated RWE and workspace commands to properly handle file permissions enforced when multitenancy is enabled.
- Added `perf_test` to Kafka. The `perf_test` for Kafka includes the `test_group` command for ingesting Avro-based blob and mock data into Kafka and the `subscribe_topic` command for listening on topics. It also includes support for ingesting mock data directly into databases. See [Kafka `perf_test` App](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/kafka/apps/perf_test/README.md) for details.
- Excluded `log4j` from the cluster class paths to prevent version conflicts. Due to the recent security updates made by `log4j`, many of the products are fragile to `log4j` versions other than the versions included in their distributions. PadoGrid now includes `log4j` binary for running client apps only.
- Fixed Kafka cluster log file name to `kafkaServer.out`. Both `server.log` and `kafkaServer.out` names were used previously.
- Added bundle support for Kafka and Confluent. See [Kafka Confluent Bundle Catalog](https://github.com/padogrid/catalog-bundles/blob/master/confluent-catalog.md)
- Added support for viewing Jupyter log files in `show_jupyter`. Use the `-port` option to view the specific server's log file or `-all` to view all active server log files.
- Added `-simulate` option in `start_mc` to support simulation of the Hazelcast Management Center bootstrap process. As with members, you can now view the Management Center bootstrap details without actually starting it. 
- Added native support for Prometheus and Grafana. They are now part of the growing list of products supported by PadoGrid. To install them use `install_padogrid` and `update_products`. To start them, first create the `grafana` app and then run `start_prometheus` and `start_grafana` found in the `grafana` app's `bin_sh` directory. For details, please see [Geode Grafana App](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/geode/apps/grafana/README.md) and [Hazelcast Grafana App](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/hazelcast/apps/grafana/README.md).
- Added native support for Derby DB. Like Prometheus and Grafana, Derby DB can now be launched as an app. Please see [Derby App](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/common/apps/derby/README.md) for details.
- Updated `start_mc` to support Hazelcast Management Center 5.2.0+ which now uses Spring bootstrap. You must upgrade to PadoGrid 0.9.22+ for `start_mc` to work with Hazelcast Management Center 5.2.0+.
- Jupyter commands now display URLs in the proper form supporting both `http` and `https`.
- Added auto-completion support to Hazelcast 5 commands.
- Added JupyterLab in the PadoGrid Docker image. Starting PadoGrid 0.9.22, you can now access PadoGrid workspaces from the browser. Please see the [Docker](https://github.com/padogrid/padogrid/wiki/Docker) section in the manual for details.

---

## Version 0.9.21

### Release Date: 10/01/22

- This release has been tested with multi-tenant workspaces. Please see the manual sections [Multitenancy](https://github.com/padogrid/padogrid/wiki/Multitenancy) and [Multitenancy Best Practices](https://github.com/padogrid/padogrid/wiki/Multitenancy-Best-Practices) for details.
- Fixed backward compatibility issues introduced by multitenancy. The previous release introduced support for multitenancy which moved workspace metadata to `~/.padogrid`. This broke the bundles that rely on workspace embedded metadata. Please upgrade to v0.9.21 to remedy this issue.
- Added `groups` in tree views. The `show_workspace` and `show_rwe` commands now include `groups` in their views.
- Updated Jupyter commands to comply with the latest JupyterLab (v3.4.7). JupyterLab is still evolving with many changes that are impacting PadoGrid. Some versions of JupyterLab found to be inconsistent with other versions in terms of import/export support. This release of PadoGrid has been tested with JupyterLab v3.4.7.
- Fixed a delete bug in Geode `DebeziumKafkaSinkTask` that generated NPE.
- Fixed a `test_group` bug in Geode `perf_test` that threw NPE when ingesting mock data into database.
- Fixed the wrong VM workspace name set in `vmenv.sh` by `install_bundle`. `vmenv.sh` was introduced in v0.9.20 which holds VM specific variables. The `VM_PADOGRID_WORKSPACE` variable was incorrectly set if the `-workspace` or `-checkout` option specified for the `install_bundle` commadn.
- Replaced the option `-host` with `-ip` in the `open_jupyter` and `start_jupyter` commands to bind a specific IP address.
- Fixed a primary and data node pod memory sizing issue. Previous versions incorrectly had primary memory set to data memory and vice versa.
- Updated `install_padogrid` to default to the cached product versions downloaded from the PadoGrid's `nightly` release. Prior to this change, the downloadable product versions were scanned resulting a long delay. You can still scan product versions by specifying the `-scan` option. Please see the usage by specifying `-?`.

---

## Version 0.9.20

### Release Date: 09/14/22

- Overhauled support for VM clusters by adding an extensive product validation process and refactoring common routines. The `vm_install`, `vm_sync`, and `vm_test` commands are now generic and support all products. These enhancements are incorporated into workspaces, simplifying multi-tenant workspace management in particular. Please see [Geode/GemFire on AWS EC2 Instances](https://github.com/padogrid/padogrid/wiki/Geode-on-AWS-EC2) and [Hazelcast on AWS EC2](https://github.com/padogrid/padogrid/wiki/Hazelcast-on-AWS-EC2) for examples.
- Added support for multitenancy. You can now sandbox workspaces by user groups. This capability allows the `padogrid` administrator to grant or revoke workspace privileges by adding/removing a user to/from the workspace group. For example, the user `foo` belongs to the `finance` group has access to the workspaces owned by that group. A user can belong to one or more groups and have access to workspaces across multiple groups. All other workspaces owned by groups that the user does not belong to are not viewable or accessible. Please see the [Multitenancy](https://github.com/padogrid/padogrid/wiki/Multitenancy) section in the manual for details.
- Added support for the `padogrid.rwe` marker for VMs and Vagrant pods. The previous version (v0.9.19) added this marker to uniquely identify running processes throughout RWEs. With that change, v0.9.19 is broken. It is unable to detect VM and Vagrant pod processes.
- Fixed Geode/GemFire locator issues in pods. Locators were not properly identified in pod VMs.
- Added support for `LOCATOR_JAVA_OPTS` and `MEMBER_JAVA_OPTS` for Geode/GemFire clusters. These variables can be set in `bin_sh/setenv.sh`.
- Added support for creating empty groups. Prior to this release, the `creat_group` command created at least one (1) cluster. You can now create an empty group and then add existing clusters to the group.
- Updated the Jupyter commands to bind to `0.0.0.0`. It was binding to `localhost` previously if the `-host` option is not specified to `start_jupyter` or `open_jupyter`.
- Added support for `PADOGRID_CHARSET` for displaying nested structures in `unicode`. Set this environment variable to `unicode` for the nested structure displaying commands like `show_rwe` if they display control characters.

```bash
export PADOGRID_CHARSET="unicode"
```

- Added Manager URL in `show_cluster -long` display for Geode/GemFire clusters.
- Fixed `create_workspace` and `create_cluster` that incorrectly always defaulted to the cluster type `geode` that prevented creating GemFire clusters.
- Excluded PadoGrid's slf4j from Geode/GemFire to remove warning messages. PadoGrid now uses slf4j included in Geode/GemFire distributions.
- Added experimental Geode/GemFire split-brain diagnostic scripts ported from [bundle-geode-1-app-perf_test_sb-cluster-sb](https://github.com/padogrid/bundle-geode-1-app-perf_test_sb-cluster-sb). Please see the link for details. 

| Script                                   | Description                                                                  |
| ---------------------------------------- | ---------------------------------------------------------------------------- |
| t_revoke_all_missing_disk_stores         | Iteratively revoke all missing data stores                                   |
| t_show_all_suspect_node_pairs            | Find the specified suspect node from all log files                           |
| t_show_all_unexpectedly_left_members     | Display unexpectedly left members in chronological order                     |
| t_show_all_unexpectedly_shutdown_removal | Find the members that unexpectedly shutdown for removal from the cluster     |
| t_show_cluster_views                     | Display cluster views in chronological order                                 |
| t_show_member_join_requests              | Display member join requests received by the locators in chronological order |
| t_show_membership_service_failure        | Display membership service failure and restarted messages from locators      |
| t_show_missing_disk_stores               | Display missing disk stores                                                  |
| t_show_offline_members                   | Display offline regions per member                                           |
| t_show_quorum_check                      | Display quorum check status if any                                           |
| t_show_recovery_steps                    | Display recovery steps for the specfied type                                 |
| t_show_stuck_threads                     | Find stuck threads                                                           |
| t_show_suspect_node_pair                 | Find the specified suspect node pair from the log files                      |
| t_show_type                              | Determine the network partition type                                         |

---

## Version 0.9.19

### Release Date: 07/09/22

- Removed log4j settings from Geode locators as a workaround to Log4J NPE raised by Geode v1.15.0. Without this fix, locators will not start for Geode v1.15.0.
- Fixed `CLUSTER_TYPE` incorrectly set for geode and gemfire. This fix effectively drops `CLUSTER_TYPE` support for older versions of PadoGrid.
- Extended `none` bundles to include any products.
- Added support for `-force` in `install_bundle` to override required products. If this options is specified, then the bundle installs regardless of whether the required products are installed.
- Added the `-all` option to `install_bundle` for installing all online bundles with a single command.

  ```bash
  # To download and install all bundles (git disabled)
  install_bundle -all -download -workspace
  install_bundle -all -download -workspace -force

  # To checkout and install all bundles (git enabled)
  install_bundle -all -checkout
  install_bundle -all -checkout -force
  ```
- Tidied up scripts by refactoring scripts and added missing scripts.
- Replaced `jps` with `ps` for searching for running processes. `jps` is no longer used in PadoGrid.
- Added the `padogrid.rwe` system property to span the active cluster search to RWEs. With this support, all clusters are now uniquely identifiable across RWEs.
- `clean_cluster` is now available for all products including Hadoop, Spark, and Kafka. 
- Fixed `show_products` which failed to show some active products. It now supports all products including Coherence.
- Added `SPARK_DIST_CLASSPATH` support for Hadoop-free Spark versions. With this support, you can now include your own versions of Hadoop in PadoGrid Spark clusters.
- Added `hazelcast.yaml` config files for all versions of Hazelcast.
- Updated `kill_cluster` and `stop_cluster` to bypass pod clusters.
- Fixed empty workspace status display issues when running `kill_workspace` and `stop_workspace`.
- Added `SSH_CONNECT_TIME` to timeout ssh commands. The default timeout is 2 seconds.

### Known Issues

- This version is broken for running clusters on VMs and Vagrant pods. It does not recognize the new marker that uniquely identifies running processes on VMs and Vagrant pods. Please use v0.9.20+ if you are running PadoGrid clusters on VMs or Vagrant pods.

---

## Version 0.9.18

### Release Date: 06/27/22

- Added initial support for Redis. Redis joins the growing list of data grid products supported by PadoGrid. Redis OSS is installable with [`install_padogrid`](https://raw.githubusercontent.com/padogrid/padogrid/develop/padogrid-deployment/src/main/resources/common/bin_sh/install_padogrid). Note that Redis OSS comes in the form of source code only. You must manually build upon installation by running the **`make`** command. The `install_padogrid` command provides details.
- PadoGrid automates boostrapping of Redis replicas with fully integrate support for distributed workspaces.
- Added Redisson addon for creating Java apps for Redis. Ported `perf_test` using Redisson.
- Added Vagrant VM support for Redis.
- `perf_test` now includes the `create_csv` script for consolidating test results into CSV files.
- `perf_test` now includes the `clean_results` script for removing results files.
- Along with the release, [`bundle-none-imdg-benchmark-tests`](https://github.com/padogrid/bundle-none-imdg-benchmark-tests) is made available for conducting benchmark tests on IMDG products.

---

## Version 0.9.17

### Release Date: 06/15/22

- Fixed `install_bundle` to correctly install a workspace bundle.
- Updated `perf_test` README.md files.
- Added entity relationship (ER) support for Geode `perf_test` that generates customer mock data.
- Added support for **padolite** clusters. This support enables normal Geode/GemFire clusters to accept Pado client connections. The immediate benefit of **padolite** is that the users can now use Pado tools such as Pado Desktop and PadoWeb to navigate and monitor Geode/GemFire clusters. To create a normal cluster with Pado enabled, execute `create_cluster -type padolite`. You can also enable/disable PadoLite by setting the `padolite.enabled` property in the `etc/cluster.properties` file. PadoLite allows connections by any Geode/GemFire clients including Pado clients.
- Added `MultiInitializer` for configuring multiple Geode/GemFire initializers. This addon lifts the single initializer limitation in Geode/GemFire.
- Added support for installing PadoGrid SNAPSHOT releases in [`install_padogrid`](https://raw.githubusercontent.com/padogrid/padogrid/develop/padogrid-deployment/src/main/resources/common/bin_sh/install_padogrid). If your PadoGrid version is older than this release, then you must download the updated `install_padogrid` script as described in the [PadoGrid Manual](https://github.com/padogrid/padogrid/wiki/Quick-Start#install-padogrid). PadoGrid snapshots are now automatically built whenever there are changes made in the `RELEASE_NOTES.md` file. You can download the latest snapshot by running the `install_padogrid` command shown below. Note that `install_padogrid` does not remove the existing snapshot installation. It simply overwrites it. Furthermore, **the downloadable snapshots do not include man pages and Coherence addons.**
- Added support for Hazelcast OSS in building PadoGrid pods. Prior to this, only Hazelcast Enterprise was supported in building PadoGrid pods.
- Fixed PadoGrid pod relevant commands that improperly handled the Management Center.
- Added PadoGrid pod support in `shutdown_cluster`.
- Fixed `show_bundle` to pagenate all bundles in GitHub. Prior to this fix, only the first page of 30 bundles were shown.

```bash
install_padogrid -product padogrid
```

- Added `unstall_product` for uninstalling products. By default, the `uninstall_product` command uninstalls the specified product version only if the product version is not in use. You can override it by specifying the `-force` option.

---

## Version 0.9.16

### Release Date: 05/30/22

- Fixed a Pado enablement bug. Without this fix, WAN enabled Geode/GemFire clusters may not start.
- Fixed `show_products` to include HazelcastDesktop.
- Replaced the Hibernate connection pool from dbcp to c3p0. The previous built-in dbcp pool is not for production use.
- Added initial support for bootstrapping Geode/GemFire clusters with the Spring Container. To use Spring, after creating a Geode/GemFire cluster, run the `bin_sh/build_app` to download the Spring Data GemFire packages into the workspace. Edit the `setenv.sh` file and set SPRING_BOOTSTRAP_ENABLED="true". For details, run `build_app -?` to display the usage.

---

## Version 0.9.15

### Release Date: 05/06/22

- Updated Hazelcast OSS and Enterprise download URLs for `install_padogrid`. Hazelcast has changed the download URLs.
- Updated Coherence port number and http blocker for Maven build.
- Added support for gemfire and coherence products in `update_products`.
- Added timestamps to `nw` data classes for Hibernate to auto-update timestamps in databases. For non-database apps, the timestamps are set when they are created in the JVM.
- Added desktop support for Hazecast 5.x.
- Added ER support for Hazelcast rmap, cache, queue, topic, rtopic, and (i)map. The `test_group` command in the `perf_test` app now includes support for recursively ingesting mock data with entity relationships.
- HazelcastDesktop is now part of PadoGrid. You can install it by running `install_padogrid -product hazelcast-desktop` and update your workspaces by running `update_products -product hazelcast-desktop`. Once installed, run `create_app -app desktop` to create a desktop app instance.

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
