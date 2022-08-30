# Geode Grafana App

The `grafana` app provides a simple and quick way to integrate Geode with Grafana by including several commands for accessing Grafana along with pre-configured Geode dashboards. For example, you can import the included dashboards into Grafana with the `import_grafana` command and monitor the entire cluster in a single view.

## Enabling/Disabling Prometheus/Grafana

Support for Prometheus/Grafana is enabled by default for all Geode clusters created by the `create_cluster` command. You can enable or disable it by setting the `prometheus.enabled` property in each cluster's `etc/cluster.properties` file as follows:

```properties
# etc/cluster.properties
# By default, Prometheus/Grafana is enabled.
prometheus.enabled=true
```

## JMX Exporter Agent

Grafana is supported via the JMX exporter provided by Prometheus. It is already included in the distribution and fully integrated with Geode out of the box. You can learn more about the exporter from the following site:

**URL:** [https://github.com/prometheus/jmx_exporter](https://github.com/prometheus/jmx_exporter)

## Installing Grafana App

The Grafana app is part of the `padogrid` distribution. Run the `create_app` to install it in your workspace.

```bash
create_app -app grafana
```

## Required Software

There are three (3) required software components that you must install before you can use the `grafana` app.

1. JQ - JSON Processor
2. Prometheus
2. Grafana

### JQ - JSON Processor

The `grafana` app relies on JQ to process JSON objects.

**URL:** [https://stedolan.github.io/jq](https://stedolan.github.io/jq)

Include it in your PATH:

```bash
# Assuming jq is placed in your home bin directory:
export PATH=~/bin:$PATH
```

### Prometheus

Download and install Prometheus:

**URL:** [https://prometheus.io/download](https://prometheus.io/download/)

Include Prometheus home directory in your `PATH` and run the following:

**Unix:**
```bash
# Using relative path:
cd_app grafana
prometheus --config.file=etc/prom-geode.yml

# Using absolute path
prometheus --config.file=$PADOGRID_WORKSPACE/apps/grafana/etc/prom-geode.yml
```

**Cygwin:**

```bash
# Using relative path:
cd_app grafana
prometheus.exe --config.file=$(cygpath -wp etc/prom-geode.yml)

# Using absolute path
prometheus --config.file=$(cygpath -wp "$PADOGRID_WORKSPACE/apps/grafana/etc/prom-geode.yml")
```

You can monitor Prometheus from your browser:

**URL:** [http://localhost:9090](http://localhost:9090)

To view a complete list of metrics:

- All avalable metrics: [http://localhost:9090/api/v1/label/__name__/values]( http://localhost:9090/api/v1/label/__name__/values)
- Metadata: http://localhost:9090/api/v1/metadata
- Prometheus specifics: [http://localhost:9090/metrics](http://localhost:9090/metrics)
- Federated:

### Grafana

Download and install Grafana:

**URL:** [https://grafana.com/grafana/download](https://grafana.com/grafana/download)

Include Grafana `bin` directory in your `PATH` and run `grafana-server`:

**Unix:**

```bash
export GRAFANA_HOME=<grafana-installation-directory>
export PATH=$PATH:$GRAFANA_HOME
grafana-server -homepath $GRAFANA_HOME
```

**Cygwin:**

```bash
export GRAFANA_HOME=<grafana-installation-directory>
export PATH=$PATH:$GRAFANA_HOME/bin
grafana-server -homepath $(cygpath -wp "$GRAFANA_HOME")
```

Once Grafana is running, use your web browser to set the user account as follows:

**URL:** [http://localhost:3000](http://localhost:3000)

```shell
User Name: admin
Password: admin
```

The `grafana` app has been preconfigured with the above user name and password. If you have a different account, then you can change them in `bin_sh/setenv.sh`. Note that the included commands require the user with administration privileges
 
## Cygwin: curl

**IMPORTANT:** If you are running this app in the Windows environment then make sure to install **`curl`** from Cygwin. Other implementations may not work properly.

## Importing Dashboards

The dashboards are organized by Grafana folders and they can be found in the following directory:

```bash
cd_app grafana
ls etc/dashboards
```

The following folders of dashboards are bundled with this distribution.

- **padogrid-perf_test** - A set of dashboards for monitoring the entire cluster and map operations executed by the `perf_test` app.

To import the default folder, i.e., `padogrid-perf_test`, first, make sure Grafana is running, and run the `import_folder` command as folllows:

```bash
cd_app grafana; cd bin_sh
./import_folder
```

To import other folders, specify the `-folder` or `-all` option.

```bash
# To import a folder in 'etc/dashboards'
./import_folder -folder padogrid-perf_test

# To imporal all folders in 'etc/dashboards'
./import_folder -all
```

### App: perf_test

The `padogrid-perf_test` folder includes the `perf_test` app dashboards. To view data in these dashboards, you must run the `perf_test`'s `test_ingestion` and `test_tx` scripts.

[Go to perf_test](../perf_test)

## Exporting Dashboards

You can also export your dashboards to use them as backup or templates by executing the `export_folder` command.

```bash
# Export all folders found in Grafana. By default, the dashboards are 
# exported in the export/ directory. You can change it in setenv.sh.
./export_folder -all
```

## Creating Dashboard Templates

You must convert the exported dashboards to templates by executing the `export_to_template` command before you can import them back to Grafana. This is due to the Grafana dependency of non-unique local IDs. The generated templates are portable and can be imported into any instance of Grafana using the `import_folder` command.

```bash
# Convert the exported folders to templates. The templates are placed in
# the templates/ directory. See the usage for details.
./export_to_template
```

## Other Commands

The `bin_sh` directory contains several other useful commands. You can display the usage of each command by specifying the `-?` option as shown below.

```bash
./create_folder -?
Usage:
   ./create_folder [-folder <folder-name>] [-?]

   Creates the specfied Grafana folder.

Default: ./create_folder -folder padogrid-perf_test
```

## Screenshots

![Grafana Screenshot](https://github.com/padogrid/padogrid/blob/develop/images/grafana-screenshot.png?raw=true)
