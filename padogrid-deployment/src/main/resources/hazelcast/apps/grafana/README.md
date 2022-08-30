# Hazelcast Grafana App

The `grafana` app provides a simple and quick way to integrate Hazelcast with Grafana by including several commands for accessing Grafana along with pre-configured Hazelcast dashboards. For example, you can import the included dashboards into Grafana with the `import_grafana` command and monitor the entire cluster in a single view.

## Installing Grafana App

The Grafana app is part of the padogrid distribution. Run the create_app to install it in your workspace.

```bash
create_app -app grafana
```

## Enabling/Disabling Grafana

Support for Grafana is enabled by default for all Hazelcast clusters created by the `create_cluster` command. You can enable or disable it by setting the `grafana.enabled` property in each cluster's `etc/cluster.properties` file as follows:

```bash
# etc/cluster.properties
# By default, Grafana is enabled.
grafana.enabled=true
```

## Enabling Prometheus for Management Center

By default, the clusters created by `create_cluster` also enable **Clustered JMX** for Management Center. To enable Prometheus for Management Center, however, you must explicitly configure the Management Center as shown below.

```bash
# Change directory to the cluster's directory
cd_cluster
vi bash_sh/setenv.sh
```

Add the following in the `bin_sh/setenv.sh` file. Note that you also need to configure Prometheus to include the Management Center as one of its targets as described in the [Prometheus](#Prometheus) section.

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

## JMX Exporter Agent

Grafana is supported via the JMX exporter provided by Prometheus. It is already included in the distribution and fully integrated with Hazelcast out of the box. You can learn more about the exporter from the following site:

**URL:** [https://github.com/prometheus/jmx_exporter](https://github.com/prometheus/jmx_exporter)

## Required Software

There are three (3) required software components that you must install before you can use the `grafana` app.

1. JQ - JSON Processor
2. Prometheus
2. Grafana

### JQ - JSON Processor

The `grafana` app relies on JQ to process JSON objects.

**URL:** [https://stedolan.github.io/jq](https://stedolan.github.io/jq)

Include it in your PATH:

```
# Assuming jq is placed in your home bin directory:
export PATH=~/bin:$PATH
```

### Prometheus

Download and install Prometheus:

**URL:** [https://prometheus.io/download](https://prometheus.io/download/)

If you have enabled Prometheus for Management Center, then you must also include the Management Center as one of the targets in the Prometheus configuration files.

```bash
cd_app grafana
vi etc/prom-hazelcast.yaml
```

Using our example in the [Enabling Prometheus for Management Center](#Enabling-Prometheus-for-Management-Center) section, add `localhost:2222` in the `targets` parameter.

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
prometheus --config.file=etc/prom-hazelcast.yml

# Using absolute path
prometheus --config.file=$PADOGRID_WORKSPACE/apps/grafana/etc/prom-hazelcast.yml
```

**Cygwin:**

```bash
export PATH=$PATH:<path to Prometheus installation directory>

# Using relative path:
cd_app grafana
prometheus.exe --config.file=$(cygpath -wp etc/prom-hazelcast.yml)

# Using absolute path
prometheus --config.file=$(cygpath -wp "$PADOGRID_WORKSPACE/apps/grafana/etc/prom-hazelcast.yml")
```

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

### Grafana

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

Once Grafana is running, use your web browser to set the user account as follows:

**URL:** [http://localhost:3000](http://localhost:3000)

```console
User Name: admin
Password: admin
```

The `grafana` app has been preconfigured with the above user name and password. If you have a different account, then you can change them in `setenv.sh`. Note that the included commands require the user with administration privileges
 
## Cygwin: curl

❗️ If you are running this app in the Windows environment then make sure to install **`curl`** from Cygwin. Other implementations may not work properly.

## Importing Dashboards

The dashboards are organized by Grafana folders and they can be found in the following directory:

```bash
cd_app grafana
ls etc/dashboards
```

The following folders of dashboards are bundled with this distribution.

- **hazelcast-addon-perf_test** - A set of dashboards for monitoring the entire cluster and map operations executed by the `perf_test` app.

To import the default folder, i.e., `hazelcast-addon-perf_test`, first, make sure Grafana is running, and run the `import_folder` command as follows:

```bash
cd bin_sh
./import_folder
```

To import other folders, specify the `-folder` or `-all` option.

```bash
# To import a folder in 'etc/dashboards'
./import_folder -folder hazelcast-addon-perf_test

# To imporal all folders in 'etc/dashboards'
./import_folder -all
```

### App: perf_test

The `hazelcast-addon-perf_test` folder includes the `perf_test` app dashboards. To view data in these dashboards, you must run the `perf_test` ingestion and transaction scripts. The following command creates the default app, perf_test, in your workspace.

```bash
create_app
```

For perf_test details, see [perf_test README.md](../perf_test/README.md).

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

   Creates the specified Grafana folder.

Default: ./create_folder -folder hazelcast-addon-perf_test
```

## Screenshots

![Grafana Screenshot](https://github.com/padogrid/padogrid/blob/develop/images/grafana-screenshot.png?raw=true)
