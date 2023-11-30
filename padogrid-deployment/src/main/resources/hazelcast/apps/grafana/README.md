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

---

#### 5.2.1. PadoGrid 0.9.22+

PadoGrid 0.9.22 includes integrated support for Prometheus and Grafana, greatly simplifying the installation and management steps.

Install Prometheus using `install_padogrid` and `update_products`:

```bash
install_padogrid -product prometheus
update_products -product prometheus
```

Start Prometheus from the `grafana` app:

```bash
cd_app grafana/bin_sh
./start_prometheus
```

---

#### 5.2.2. PadoGrid 0.9.21 or Older Versions

*If you are using PadoGrid 0.9.21 or older, then we recommend upgrading PadoGrid to the latest version and follow the instructions in the previous section.*

Download and install Prometheus:

**URL:** [https://prometheus.io/download](https://prometheus.io/download/)

If you have enabled Prometheus for Management Center, then you must also include the Management Center as one of the targets in the Prometheus configuration files.

```bash
cd_app grafana
vi etc/prometheus.yml
```
Using our example in the [3. Enabling Prometheus for Management Center](#3-Enabling-Prometheus-for-Management-Center) section, add `localhost:2222` in the `targets` parameter.

```yaml
...
  scrape_configs:
...
      static_configs:
        - targets: [localhost:2222, localhost:8091, localhost:8092, localhost:8093, localhost:8094, localhost:8095, localhost:8096, localhost:8097, localhost:8098, localhost:8099, localhost:8100]
...
```

To run Prometheus, include its home directory in your `PATH` and run the `prometheus` executable as follows:

**Unix:**

```bash
export PATH=$PATH:<path to Prometheus installation directory>

# Using relative path:
cd_app grafana
prometheus --config.file=etc/prometheus.yml

# Using absolute path
prometheus --config.file=$PADOGRID_WORKSPACE/apps/grafana/etc/prometheus.yml
```

**Cygwin:**

```bash
export PATH=$PATH:<path to Prometheus installation directory>

# Using relative path:
cd_app grafana
prometheus.exe --config.file=$(cygpath -wp etc/prometheus.yml)

# Using absolute path
prometheus --config.file=$(cygpath -wp "$PADOGRID_WORKSPACE/apps/grafana/etc/prometheus.yml")
```

---

#### 5.2.3. Monitoring Prometheus

You can monitor Prometheus from your browser:

**URL:** [http://localhost:9090](http://localhost:9090)

To view a complete list of metrics:

- All avalable metrics: [http://localhost:9090/api/v1/label/__name__/values]( http://localhost:9090/api/v1/label/__name__/values)
- Metadata: http://localhost:9090/api/v1/metadata
- Prometheus specifics: [http://localhost:9090/metrics](http://localhost:9090/metrics)
- Federated:

```bash
curl -G http://localhost:9090/federate -d 'match[]={__name__!=""}'
```

### 5.3. Grafana

---

#### 5.3.1. PadoGrid 0.9.22+

Install Grafana  using `install_padogird` and `update_products`:

```bash
# Install Grafana Enterprise
install_padogrid -product grafana-enterprise
update_products -product grafana-enterprise

# Or install Grafana OSS
install_padogrid -product grafana-oss
update_products -product grafana-oss
```

Start Grafana from the `grafana` app:

```bash
cd_app grafana/bin_sh
./start_grafana
```

---

#### 5.3.2. PadoGrid 0.9.21 or Older Versions

Download and install Grafana:

**URL:** [https://grafana.com/grafana/download](https://grafana.com/grafana/download)

Include Grafana in your `PATH` and run the following (`GRAFANA_HOME` is the Grafana installation root directory path):

**Unix**

```bash
export GRAFANA_HOME=<grafana-installation-directory>
export PATH=$PATH:$GRAFANA_HOME/bin
grafana-server -homepath $GRAFANA_HOME
```

**Cygwin**

```bash
export GRAFANA_HOME=<grafana-installation-directory>
export PATH=$PATH:$GRAFANA_HOME/bin
grafana-server -homepath $(cygpath -wp "$GRAFANA_HOME")
```

---

#### 5.3.3. Monitoring Grafana

Once Grafana is running, use your web browser to set the user account as follows:

**URL:** [http://localhost:3000](http://localhost:3000)

```console
User Name: admin
Password: admin
```

The `grafana` app has been preconfigured with the above user name and password. If you have a different account, then you can change them in `setenv.sh`. Note that the included commands require the user with administration privileges
 
### 5.4. Cygwin: `curl`

❗️ If you are running this app in the Windows environment then make sure to install **`curl`** from Cygwin. Other implementations may not work properly.

## 6. Importing Dashboards

The dashboards are organized by Grafana folders and they can be found in the following directory:

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

### 6.1.  App: `perf_test`

The `Hazelcast-perf_test` folder includes the `perf_test` app dashboards. To view data in these dashboards, you must run the `perf_test` ingestion and transaction scripts. The following command creates the default app, `perf_test`, in your workspace.

```bash
create_app
```

For `perf_test` details, see [perf\_test README](https://github.com/padogrid/padogrid/blob/develop/padogrid-deployment/src/main/resources/hazelcast/apps/perf_test/README.md).

### 6.2. Hazelcast Dashboards

The `Hazelcast*` folders contain the main dashboard with the prefix, `00Main`. The main dashboard is the main console for navigating all the dashboards in the respective folder. It has the layout similar to the Management Center as shown in the screen shot in Section [10.2 Hazelcast Folder](#102-hazelcast-folder).

To quickly activate the dashboards with data, you can run [`perf_test`](../perf_test/README.md) with the `group-workflow-*.properties` files.

The Hazelcast dasboards support multiple clusters. If you are running Prometheus in PadoGrid, then to include multiple clusters, you must add them in the `etc/prometheus.yml` file. You can use the the included [`etc/prometheus-clusters.yml`](etc/prometheus-clusters.yml) as an example. This file configures two (2) Hazelast clusters named, 'myhz' and 'myhz2'.

## 7. Exporting Dashboards

You can also export your dashboards to use them as backup or templates by executing the `export_folder` command.

```bash
# Export all folders found in Grafana. By default, the dashboards are 
# exported in the export/ directory. You can change it in setenv.sh.
./export_folder -all
```

## 8. Creating Dashboard Templates

You must convert the exported dashboards to templates by executing the `export_to_template` command before you can import them back to Grafana. This is due to the Grafana dependency of non-unique local IDs. The generated templates are portable and can be imported into any instance of Grafana using the `import_folder` command.

```bash
# Convert the exported folders to templates. The templates are placed in
# the templates/ directory. See the usage for details.
./export_to_template
```

### 8.1. Synchronizing `Hazelcast*` Folders

## 8. Synchronizing Folders

If you made changes to dashboards from the browser and want to save them in the local file system in the form of templates then execute the `padogrid_sync_folders` command. This command exports all of the dashboards in the `Hazelcast*` folders, creates templates, applies the required Grafana variables to the templates, and re-imports the templates to Grafana.

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

## 9. Other Commands

The `bin_sh` directory contains many useful commands for working with dashboards. You can display the usage of each command by specifying the `-?` option as shown below.

```bash
./create_folder -?
Usage:
   ./create_folder [-folder <folder-name>] [-?]

   Creates the specified Grafana folder.

Default: ./create_folder -folder Hazelcast-perf_test
```

## 10. Screenshots

### 10.1. padogrid_perf_test Folder

![perf_test Folder](https://github.com/padogrid/padogrid/blob/develop/images/grafana-screenshot.png?raw=true)

### 10.2. Hazelcast Folder

![Hazelcast Folder](https://github.com/padogrid/padogrid/blob/develop/images/grafana-hazelcast-screenshot.png?raw=true)

## 11. Teardown

### 11.1. PadoGrid 0.9.22+

```bash
cd_app grafana/bin_sh
./stop_grafana
./stop_prometheus
```

### 11.2. PadoGrid 0.9.21 or Older Versions

Fnd the Prometheus and Grafana process IDs and send the TERM signal.

```bash
ps -efwww | grep grafana-server
kill -15 <grafana-pid>

ps -efwww | grep prometheus
kill -15 <prometheus-pid>
```
