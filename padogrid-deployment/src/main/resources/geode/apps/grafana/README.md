# GemFire Grafana App

The `grafana` app provides a simple and quick way to integrate GemFire with Grafana by including several commands for accessing Grafana along with pre-configured GemFire dashboards. For example, you can import the included dashboards into Grafana with the `import_grafana` command and monitor the entire cluster in a single view.

✏️  *Due to the shift in the product roadmap by Broadcom, the PadoGrid GemFire Dashboards (PGFD) supports only GemFire 10.x with GemFire Prometheus enabled.*

✏️  *PGFD is available starting PadoGrid 1.0.2.*

## 1. Installing Grafana App

The Grafana app is part of the `padogrid` distribution. Run the `create_app` to install it in your workspace.

```bash
create_app -product gemfire -app grafana
```

## 2. Enabling/Disabling Prometheus

Support for Prometheus is enabled by default for all GemFire clusters created by the `create_cluster` command. You can enable or disable it by setting the `prometheus.enabled` property in each cluster's `etc/cluster.properties` file as follows:

```properties
# Enable/disable locator Prometheus
locator.prometheus.enabled=true
# Enable/disable member Prometheus
prometheus.enabled=true
```

There are two (2) different endpoints that can be configured to emanate GemFire metrics: *Native Promtheus Metrics Endpoint* and *JMX Exporter Endpoint*. 

✏️  *Note that the metrics from both endpoints are not compatible. The PadoGrid GemFire Dashboards supports only the Native Prometheus Metrics Endpoint.*

### 2.1. Native Prometheus Endpoint

GemFire 10.x natively supports the Prometheus Metrics Endpoint, which emanates thousands of metrics that were only available to VSD before. PadoGrid includes three (3) sets of comprehensive dashboards that touch on all the GemFire metrics in a manageble and organized fashion.

The `create_cluster` command by default enables the native Prometheus Metrics Endpoint for both locators and servers. The following shows the default values of the Prometheus properties defined in the `etc/cluster.properties` file.

```properties
# Enable/disable locator Prometheus endpoint native to GemFire. This feature is
# available starting GemFire 10.1. For older versions, this property is ignored
# and always false.
# If true, then the Prometheus metrics endpoint is enabled by setting the GemFire property,
# gemfire.prometheus.metrics.port to the value derived from locator.prometheus.startPort.
# If false, then the JMX exporter endpoint is enabled.
# Default: false
locator.prometheus.gemfire.enabled=true
# GemFire Prometheus metrics endpoint emission rate: Default, All, None. If unspecified, then Default.
# This property is ignored if locator.prometheus.gemfire.enabled is false.
locator.prometheus.metrics.emission=Default

# Enable/disable member Prometheus
prometheus.enabled=true
# The first member's Prometheus port number. The port number is incremented starting from this number.
prometheus.startPort=8091

# Enable/disable member Prometheus endpoint native to GemFire. This feature is
# available starting GemFire 10.1. For older versions, this property is ignored
# and always false.
# If true, then the Prometheus metrics endpoint is enabled by setting the GemFire property,
# gemfire.prometheus.metrics.port to the value derived from prometheus.startPort.
# If false, then the JMX exporter endpoint is enabled.
# Default: true
prometheus.gemfire.enabled=true
# GemFire Prometheus metrics endpoint emission rate: Default, All, None. If unspecified, then Default.
# This property is ignored if prometheus.gemfire.enabled is false.
prometheus.metrics.emission=Default
```

❗️ To get all the GemFire metrics into Grafana, the emission rate must be set to `All` as follows.

```properties
locator.prometheus.metrics.emission=All
prometheus.metrics.emission=All
```

## 2.2. JMX Exporter Endpoint

Prior to GemFire 10.x, GemFire (and Geode) relied on the JMX exporter maintained by Prometheus. In addtion to the native Prometheus Metrics Endpoint, PadoGrid continues the support for the the JMX Exporter Endpoint. You can learn more about the exporter from the following site:

**URL:** [https://github.com/prometheus/jmx_exporter](https://github.com/prometheus/jmx_exporter)

To enable the JMX Exporter Endpoint, you must disable the following properties in the `etc/clusters.properties` file as shown below. If these properties are disabled, then PadoGrid automatically switches to the JMX Exporter Endpoint.

```bash
locator.prometheus.gemfire.enabled=false
prometheus.gemfire.enabled=false
```

## 3. Required Software

There are three (3) required software components that you must install before you can use the `grafana` app.

- JQ - JSON Processor
- Prometheus
- Grafana
- `curl`

### 3.1. JQ - JSON Processor

The PadoGrid `grafana` app relies on JQ to process JSON objects.

**URL:** [https://stedolan.github.io/jq](https://stedolan.github.io/jq)

Include it in your PATH:

```bash
# Assuming jq is placed in your home bin directory:
export PATH=~/bin:$PATH
```

### 3.2. Prometheus

#### 3.2.1. Installing and Starting Prometheus

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

#### 3.2.3. Monitoring Prometheus

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

### 3.3. Grafana

#### 3.3.1. Installing and Starting Grafana

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

#### 3.3.2. Monitoring Grafana

Once Grafana is running, use your web browser to set the user account as follows:

**URL:** [http://localhost:3000](http://localhost:3000)

```console
User Name: admin
Password: admin
```

The `grafana` app has been preconfigured with the above user name and password. If you have a different account, then you can change them in `bin_sh/setenv.sh`. Note that the included commands require the user with administration privileges
 
### 3.4. Cygwin: `curl`

❗ If you are running this app in the Windows environment then make sure to install **`curl`** from Cygwin. Other implementations may not work properly.

## 4. Installing and Uninstalling Dashboards

### 4.1. Importing Dashboards

The dashboards are organized by Grafana folders and they are stored in the following directory:

```bash
cd_app grafana
ls etc/dashboards
```

The following folders of dashboards are bundled with this distribution.

- **padogrid-perf_test** - A set of dashboards for monitoring the entire cluster and map operations executed by the `perf_test` app.
- **GemFire** - A set of dashboards for monitoring a single GemFire cluster.
- **GemFireDual** - A set of dashboards for comparing two (2) GemFire clusters side-by-side.
- **GemFireAll** - A set of dashboards for federating multiple GemFire clusters.
- **Padogrid** - A set of dashboards for PadoGrid specific dashboards.

If GemFire is running on Kubernetes, then you need to change the default label value to `namespace` or `service` by executing `padogrid_update_cluster_templating` as follows.

```bash
./padogrid_update_cluster_templating -label namespace
```

The above command changes the default label, `job`, to `namespace` so that the GemFire metrics can be filtered by Kubernetes namespace. For non-Kubernetes, `padogrid_update_cluster_templating` is not necessary since the dashboards are predefined with the default label, `job`.

To import all folders, run `import_folder -all` as follows.

```bash
cd bin_sh
./import_folder -all
```

To import the default folder, i.e., `GemFire-perf_test`, run `import_folder` as follows.

```bash
cd bin_sh
./import_folder
```

To import folders individually, specify the `-folder` option.

```bash
# To import the GemFire-perf_test folder in 'etc/dashboards':
./import_folder -folder GemFire-perf_test

# To import the GemFire folder in 'etc/dashboards':
./import_folder -folder GemFire

# To import the GemFireDual folder in 'etc/dashboards':
./import_folder -folder GemFireDual

# To import the GemFireAll folder in 'etc/dashboards':
./import_folder -folder GemFireAll

# To import the Padogrid folder in 'etc/dashboards':
./import_folder -folder Padogrid

# To import all folders in 'etc/dashboards':
./import_folder -all
```

### 4.2. Deleting Dashboards

The `delete_folder` deletes individual folders in Grafana.

```bash
# To delete the GemFire-perf_test in Grafana
./delete_folder -folder GemFire-perf_test

# To delete the GemFire folder in Grafana
./delete_folder -folder GemFire

# To delete the GemFireDual folder in Grafana
./delete_folder -folder GemFireDual

# To delete the GemFireAll folder in Grafana
./delete_folder -folder GemFireAll

# To delete the Padogrid folder in Grafana
./delete_folder -folder Padogrid
```

### 4.3. Replacing Dashboards

The `padogrid_import_folders` command replaces all the dashboards by first deleting them from Grafana by executing `delete_folder` and then importing the JSON files in `etc/dashboards` by executing `import_folder`.

```bash
./padogrid_import_folders
```

## 5. Viewing Dashboards

### 5.1. `perf_test` Dashboards

The `GemFire-perf_test` folder includes the `perf_test` app dashboards. To view data in these dashboards, you must run the `perf_test` ingestion and transaction scripts. The following command creates the default app, `perf_test`, in your workspace.

```bash
create_app
```

For `perf_test` details, see [perf_test README](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/geode/apps/perf_test/README.md).

![perf_test Folder](https://github.com/padogrid/padogrid/blob/develop/images/grafana-screenshot.png?raw=true)

### 5.2. GemFire Dashboards

The `GemFire*` folders contain the main dashboard with the prefix, `00Main`. The main dashboard is the main console for navigating all the dashboards in the respective folder. It has the layout similar to the Management Center as shown below.

![GemFire Folder](https://github.com/padogrid/padogrid/blob/develop/images/grafana-gemfire-all-screenshot.png?raw=true)

To quickly activate the dashboards with data, you can run [`perf_test`](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/geode/apps/perf_test/README.md) with the `group-workflow-*.properties` files.

The GemFire dasboards support multiple clusters. If you are running Prometheus in PadoGrid, then to include multiple clusters, you must add them in the `etc/prometheus.yml` file. You can use the the included [`etc/prometheus-clusters.yml`](etc/prometheus-clusters.yml) as an example. This file configures two (2) GemFire clusters named, `mygemfire` and `mygemfire2`.

## 6. Archiving Dashboards

### 6.1. Exporting Dashboards

You can archive the dashboards in the form of JSON files by exporting them with the `export_folder` command as follows.

```bash
# Export all folders found in Grafana. By default, the dashboards are
# exported in the export/ directory. You can change it in setenv.sh.
./export_folder -all
```

### 6.2. Creating Dashboard Templates

Before you can import the exported dashboards to Grafana, you must convert them templates by executing the `export_to_template` command, which removes the Grafana dependency of non-unique local IDs. The generated templates are portable and can be imported into any instance of Grafana using the `import_folder` command.

```bash
# Convert the exported folders to templates. The templates are placed in
# the templates/ directory. See the usage for details.
./export_to_template
```

## 7. Synchronizing Folders

If you made changes to dashboards from the browser and want to save them in the local file system in the form of templates then execute the `padogrid_sync_folders` command. This command exports all the dashboards in the `GemFire*` folders, creates templates, applies the required Grafana variables to the templates, and re-imports the templates to Grafana.

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

## 8. Other Commands

The `bin_sh` directory contains many useful commands for working with dashboards. You can display the usage of each command by specifying the `-?` option as shown below.

```bash
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
             Folder name. Default: GemFire-perf_test

DEFAULT
   ./create_folder -folder GemFire-perf_test
```

## 9. Teardown

```bash
cd_app grafana/bin_sh
./stop_grafana
./stop_prometheus
```
