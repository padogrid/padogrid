# Hazelcast Grafana App

The `grafana` app provides a simple and quick way to integrate Hazelcast with Grafana by including several commands for accessing Grafana along with pre-configured Hazelcast dashboards. For example, you can import the included dashboards into Grafana with the `import_grafana` command and monitor the entire cluster in a single view.

## 1. Installing Grafana App

The Grafana app is part of the padogrid distribution. Run the `create_app` to install it in your workspace.

```bash
create_app -product hazelcast -app grafana
```

## 2. Enabling/Disabling Prometheus

Support for Prometheus is enabled by default for all Hazelcast clusters created by the `create_cluster` command. You can enable or disable it by setting the `prometheus.enabled` property in each cluster's `etc/cluster.properties` file as follows:

```properties
# etc/cluster.properties
# By default, Prometheus is enabled.
prometheus.enabled=true
```

## 3. Enabling Prometheus for Management Center

By default, the clusters created by `create_cluster` also enable **Clustered JMX** for Management Center. To enable Prometheus for Management Center, however, you must explicitly configure the Management Center as shown below.

```bash
# Change directory to the cluster's directory
cd_cluster
vi bash_sh/setenv.sh
```

Add the following in the `bin_sh/setenv.sh` file. Note that you also need to configure Prometheus to include the Management Center as one of its targets as described in the [5.2. Prometheus](#52-prometheus) section.

```bash
# Port number can be arbitrary but the same port number must
# be included in the Prometheus configuration file.
MC_JAVA_OPTS="-Dhazelcast.mc.prometheusExporter.enabled=true \
              -Dhazelcast.mc.prometheusExporter.port=2222"
```

Restart the Management Center.

```bash
stop_mc
start_mc
```

## 4. JMX Exporter Agent

Grafana is supported via the JMX exporter provided by Prometheus. It is already included in the distribution and fully integrated with Hazelcast out of the box. You can learn more about the exporter from the following site:

**URL:** [https://github.com/prometheus/jmx_exporter](https://github.com/prometheus/jmx_exporter)

## 5. Required Software

There are three (3) required software components that you must install before you can use the `grafana` app.

- JQ - JSON Processor
- Prometheus
- Grafana
- `curl`

### 5.1. JQ - JSON Processor

The PadoGrid `grafana` app relies on JQ to process JSON objects.

**URL:** [https://stedolan.github.io/jq](https://stedolan.github.io/jq)

Include it in your PATH:

```bash
# Assuming jq is placed in your home bin directory:
export PATH=~/bin:$PATH
```

### 5.2. Prometheus

#### 5.2.1. Installing and Starting Prometheus

PadoGrid includes integrated support for Prometheus and Grafana, greatly simplifying the installation and management steps.

Install Prometheus using `install_padogrid` and `update_padogrid`:

```bash
install_padogrid -product prometheus
update_padogrid -product prometheus
```

Start Prometheus from the `grafana` app:

```bash
cd_app grafana/bin_sh
./start_prometheus
```

#### 5.2.2. Monitoring Prometheus

You can monitor Prometheus from your browser:

**URL:** [http://localhost:9090](http://localhost:9090)

To view a complete list of metrics:

- All avalable metrics: [http://localhost:9090/api/v1/label/__name__/values]( http://localhost:9090/api/v1/label/__name__/values)
- Metadata: http://localhost:9090/api/v1/metadata
- Prometheus specifics: [http://localhost:9090/metrics](http://localhost:9090/metrics)
- Federated:

```bash
curl -Gs http://localhost:9090/federate -d 'match[]={__name__!=""}'
```

### 5.3. Grafana

---

#### 5.3.1. Installing and Starting Grafana

Install Grafana  using `install_padogird` and `update_padogrid`:

```bash
# Install Grafana Enterprise
install_padogrid -product grafana-enterprise
update_padogrid -product grafana-enterprise

# Or install Grafana OSS
install_padogrid -product grafana-oss
update_padogrid -product grafana-oss
```

Start Grafana from the `grafana` app:

```bash
cd_app grafana/bin_sh
./start_grafana
```

#### 5.3.2. Monitoring Grafana

Once Grafana is running, use your web browser to set the user account as follows:

**URL:** [http://localhost:3000](http://localhost:3000)

```console
User Name: admin
Password: admin
```

The `grafana` app has been preconfigured with the above user name and password. If you have a different account, then you can change them in `setenv.sh`. Note that the included commands require the user with administration privileges
 
### 5.4. Cygwin: `curl`

❗️ If you are running this app in the Windows environment then make sure to install **`curl`** from Cygwin. Other implementations may not work properly.

## 6. Installing and Uninstalling Dashboards

### 6.1. Importing Dashboards

The dashboards are organized by Grafana folders and they are stored in the following directory:

```bash
cd_app grafana
ls etc/dashboards
```

The following dashboard folders are included in this distribution.

- **Hazelcast-perf_test** - A set of dashboards for monitoring the metrics specific to the `perf_test` app.
- **Hazelcast** - A set of dashboards for monitoring a single Hazelcast cluster.
- **HazelcastDual** - A set of dashboards for comparing two (2) Hazelcast clusters side-by-side.
- **HazelcastAll** - A set of dashboards for federating multiple Hazelcast clusters.
- **Padogrid** - A set of dashboards for PadoGrid specific dashboards.

If Hazelcast is running on Kubernetes, then you need to change the default label value to `namespace` or `service` by executing `padogrid_update_cluster_templating` as follows.

```bash
./padogrid_update_cluster_templating -label namespace
```

The above command changes the default label, `job`, to `namespace` so that the Hazelcast metrics can be filtered by Kubernetes namespace. For non-Kubernetes, `padogrid_update_cluster_templating` is not necessary since the dashboards are predefined with the default label, `job`.
 
To import all folders, run `import_folder -all` as follows.

```bash
cd bin_sh
./import_folder -all
```

To import the default folder, i.e., `Hazelcast-perf_test`, run `import_folder` as follows.

```bash
cd bin_sh
./import_folder
```

To import folders individually, specify the `-folder` option.

```bash
# To import the Hazelcast-perf_test folder in 'etc/dashboards':
./import_folder -folder Hazelcast-perf_test

# To import the Hazelcast folder in 'etc/dashboards':
./import_folder -folder Hazelcast

# To import the HazelcastDual folder in 'etc/dashboards':
./import_folder -folder HazelcastDual

# To import the HazelcastAll folder in 'etc/dashboards':
./import_folder -folder HazelcastAll

# To import the Padogrid folder in 'etc/dashboards':
./import_folder -folder Padogrid

# To import all folders in 'etc/dashboards':
./import_folder -all
```

### 6.2. Deleting Dashboards

The `delete_folder` deletes individual folders in Grafana.

```bash
# To delete the Hazelcast-perf_test in Grafana
./delete_folder -folder Hazelcast-perf_test

# To delete the Hazelcast folder in Grafana
./delete_folder -folder Hazelcast

# To delete the HazelcastDual folder in Grafana
./delete_folder -folder HazelcastDual

# To delete the HazelcastAll folder in Grafana
./delete_folder -folder HazelcastAll

# To delete the Padogrid folder in Grafana
./delete_folder -folder Padogrid
```

### 6.3. Replacing Dashboards

The `padogrid_import_folders` command replaces all the dashboards by first deleting them from Grafana by executing `delete_folder` and then importing the JSON files in `etc/dashboards` by executing `import_folder`.

```bash
./padogrid_import_folders
```

## 7. Viewing Dashboards

### 7.1. `perf_test` Dashboards

The `Hazelcast-perf_test` folder includes the `perf_test` app dashboards. To view data in these dashboards, you must run the `perf_test` ingestion and transaction scripts. The following command creates the default app, `perf_test`, in your workspace.

```bash
create_app
```

For `perf_test` details, see [perf_test README](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/hazelcast/apps/perf_test/README.md).

![perf_test Folder](https://github.com/padogrid/padogrid/blob/develop/images/grafana-screenshot.png?raw=true)

### 7.2. Hazelcast Dashboards

The `Hazelcast*` folders contain the main dashboard with the prefix, `00Main`. The main dashboard is the main console for navigating all the dashboards in the respective folder. It has the layout similar to the Management Center as shown below.

![Hazelcast Folder](https://github.com/padogrid/padogrid/blob/develop/images/grafana-hazelcast-screenshot.png?raw=true)

To quickly activate the dashboards with data, you can run [`perf_test`](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/hazelcast/apps/perf_test/README.md) with the `group-workflow-*.properties` files.

The Hazelcast dasboards support multiple clusters. If you are running Prometheus in PadoGrid, then to include multiple clusters, you must add them in the `etc/prometheus.yml` file. You can use the the included [`etc/prometheus-clusters.yml`](etc/prometheus-clusters.yml) as an example. This file configures two (2) Hazelast clusters named, `myhz` and `myhz2`.

## 8. Archiving Dashboards

### 8.1. Exporting Dashboards

You can archive the dashboards in the form of JSON files by exporting them with the `export_folder` command as follows.

```bash
# Export all folders found in Grafana. By default, the dashboards are 
# exported in the export/ directory. You can change it in setenv.sh.
./export_folder -all
```

### 8.2. Creating Dashboard Templates

Before you can import the exported dashboards to Grafana, you must convert them templates by executing the `export_to_template` command, which removes the Grafana dependency of non-unique local IDs. The generated templates are portable and can be imported into any instance of Grafana using the `import_folder` command.

```bash
# Convert the exported folders to templates. The templates are placed in
# the templates/ directory. See the usage for details.
./export_to_template
```

## 9. Synchronizing Folders

If you made changes to dashboards from the browser and want to save them in the local file system in the form of templates then execute the `padogrid_sync_folders` command. This command exports all the dashboards in the `Hazelcast*` folders, creates templates, applies the required Grafana variables to the templates, and re-imports the templates to Grafana.

```bash
# Using 'job':
./padogrid_sync_folders -label job
# To make dashboards editable
./padogrid_sync_folders -label job -editable

# For Kubernetes:
./padogrid_sync_folders -label namespace
# To make dashboards editable
./padogrid_sync_folders -label namespace -editable
```

## 10. Other Commands

The `bin_sh` directory contains many useful commands for working with dashboards. You can display the usage of each command by specifying the `-?` option as shown below.

```bash
./create_folder -?

WORKSPACE
   /opt/padogrid/workspaces/myrwe/myws

NAME
   ./create_folder - Create the specfied Grafana folder

SYNOPSIS
   ./create_folder [-folder folder_name] [-?]

DESCRIPTION
   Creates the specfied Grafana folder.

OPTIONS
   -folder folder_name
             Folder name. Default: Hazelcast-perf_test

DEFAULT
   ./create_folder -folder Hazelcast-perf_test
```

## 11. Teardown

```bash
cd_app grafana/bin_sh
./stop_grafana
./stop_prometheus
```
