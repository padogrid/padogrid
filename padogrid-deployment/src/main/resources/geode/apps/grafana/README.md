# Geode Grafana App

The `grafana` app provides a simple and quick way to integrate Geode with Grafana by including several commands for accessing Grafana along with pre-configured Geode dashboards. For example, you can import the included dashboards into Grafana with the `import_grafana` command and monitor the entire cluster in a single view.

## 1. Installing Grafana App

The Grafana app is part of the `padogrid` distribution. Run the `create_app` to install it in your workspace.

```bash
create_app -product geode -app grafana
```

## 2. Enabling/Disabling Prometheus

Support for Prometheus is enabled by default for all Geode clusters created by the `create_cluster` command. You can enable or disable it by setting the `prometheus.enabled` property in each cluster's `etc/cluster.properties` file as follows:

```properties
# etc/cluster.properties
# By default, Prometheus is enabled.
prometheus.enabled=true
```

## 3. JMX Exporter Agent

Grafana is supported via the JMX exporter provided by Prometheus. It is already included in the distribution and fully integrated with Geode out of the box. You can learn more about the exporter from the following site:

**URL:** [https://github.com/prometheus/jmx_exporter](https://github.com/prometheus/jmx_exporter)


## 4. Required Software

There are three (3) required software components that you must install before you can use the `grafana` app.

- JQ - JSON Processor
- Prometheus
- Grafana
- `curl`

### 4.1. JQ - JSON Processor

The PadoGrid `grafana` app relies on JQ to process JSON objects.

**URL:** [https://stedolan.github.io/jq](https://stedolan.github.io/jq)

Include it in your PATH:

```bash
# Assuming jq is placed in your home bin directory:
export PATH=~/bin:$PATH
```

### 4.2. Prometheus

---

#### 4.2.1. PadoGrid 0.9.22+

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

#### 4.2.2. PadoGrid 0.9.21 or Older Versions

*If you are using PadoGrid 0.9.21 or older, then we recommend upgrading PadoGrid to the latest version and follow the instructions in the previous section.*

Download and install Prometheus:

**URL:** [https://prometheus.io/download](https://prometheus.io/download/)

To run Prometheus, include its home directory in your `PATH` and run the `prometheus` executable as follows:

**Unix:**

```bash
# Using relative path:
cd_app grafana
prometheus --config.file=etc/prometheus.yml

# Using absolute path
prometheus --config.file=$PADOGRID_WORKSPACE/apps/grafana/etc/prometheus.yml
```

**Cygwin:**

```bash
# Using relative path:
cd_app grafana
prometheus.exe --config.file=$(cygpath -wp etc/prometheus.yml)

# Using absolute path
prometheus --config.file=$(cygpath -wp "$PADOGRID_WORKSPACE/apps/grafana/etc/prometheus.yml")
```

---

#### 4.2.3. Monitoring Prometheus

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

### 4.3. Grafana

---

#### 4.3.1. PadoGrid 0.9.22+

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

#### 4.3.2. PadoGrid 0.9.21 or Older Versions

Download and install Grafana:

**URL:** [https://grafana.com/grafana/download](https://grafana.com/grafana/download)

Include Grafana in your `PATH` and run the following (`GRAFANA_HOME` is the Grafana installation root directory path):

**Unix:**

```bash
export GRAFANA_HOME=<grafana-installation-directory>
export PATH=$PATH:$GRAFANA_HOME/bin
grafana-server -homepath $GRAFANA_HOME
```

**Cygwin:**

```bash
export GRAFANA_HOME=<grafana-installation-directory>
export PATH=$PATH:$GRAFANA_HOME/bin
grafana-server -homepath $(cygpath -wp "$GRAFANA_HOME")
```

---

#### 4.3.3. Monitoring Grafana

Once Grafana is running, use your web browser to set the user account as follows:

**URL:** [http://localhost:3000](http://localhost:3000)

```console
User Name: admin
Password: admin
```

The `grafana` app has been preconfigured with the above user name and password. If you have a different account, then you can change them in `bin_sh/setenv.sh`. Note that the included commands require the user with administration privileges
 
### 4.4. Cygwin: `curl`

❗ If you are running this app in the Windows environment then make sure to install **`curl`** from Cygwin. Other implementations may not work properly.

## 5. Importing Dashboards

The dashboards are organized by Grafana folders and they can be found in the following directory:

```bash
cd_app grafana
ls etc/dashboards
```

The following folders of dashboards are bundled with this distribution.

- **padogrid-perf_test** - A set of dashboards for monitoring the entire cluster and map operations executed by the `perf_test` app.

To import the default folder, i.e., `padogrid-perf_test`, first, make sure Grafana is running, and run the `import_folder` command as folllows:

```bash
cd_app grafana/bin_sh
./import_folder
```

To import other folders, specify the `-folder` or `-all` option.

```bash
# To import a folder in 'etc/dashboards'
./import_folder -folder padogrid-perf_test

# To imporal all folders in 'etc/dashboards'
./import_folder -all
```

### 5.1. App: `perf_test`

The `padogrid-perf_test` folder includes the `perf_test` app dashboards. To view data in these dashboards, you must run the `perf_test`'s `test_ingestion` and `test_tx` scripts.

For `perf_test1` details, see [perf_test README.md](../perf_test/README.md).

## 6. Exporting Dashboards

You can also export your dashboards to use them as backup or templates by executing the `export_folder` command.

```bash
# Export all folders found in Grafana. By default, the dashboards are 
# exported in the export/ directory. You can change it in setenv.sh.
./export_folder -all
```

## 7. Creating Dashboard Templates

You must convert the exported dashboards to templates by executing the `export_to_template` command before you can import them back to Grafana. This is due to the Grafana dependency of non-unique local IDs. The generated templates are portable and can be imported into any instance of Grafana using the `import_folder` command.

```bash
# Convert the exported folders to templates. The templates are placed in
# the templates/ directory. See the usage for details.
./export_to_template
```

## 8. Other Commands

The `bin_sh` directory contains several other useful commands. You can display the usage of each command by specifying the `-?` option as shown below.

```bash
./create_folder -?
Usage:
   ./create_folder [-folder <folder-name>] [-?]

   Creates the specified Grafana folder.

Default: ./create_folder -folder padogrid-perf_test
```

## 9. Screenshots

![Grafana Screenshot](https://github.com/padogrid/padogrid/blob/develop/images/grafana-screenshot.png?raw=true)

## 10. Teardown

### 10.1. PadoGrid 0.9.22+

```bash
cd_app grafana/bin_sh
./stop_grafana
./stop_prometheus
```

### 10.2. PadoGrid 0.9.21 or Older Versions

Fnd the Prometheus and Grafana process IDs and send the TERM signal.

```bash
ps -efwww | grep grafana-server
kill -15 <grafana-pid>

ps -efwww | grep prometheus
kill -15 <prometheus-pid>
```
