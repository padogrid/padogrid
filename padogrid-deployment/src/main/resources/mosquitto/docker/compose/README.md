# Mosquitto Docker Compose

This article describes how to create a Docker environment and launch a Mosquitto cluster using `docker compose`.

:exclamation: You must first install Docker and Docker Compose. See [References](#references) for download links.

## Switch to Mosquitto Cluster

Make sure you switch into the correct Mosquitto cluster before creating a Docker cluster. The `create_docker` command applies the current cluster settings to the Docker Compose files.

```bash
# Create and switch into a Mosquitto cluster
make_cluster -product mosquitto -cluster mymosquitto 
switch_cluster mymosquitto
```

## Create PadoGrid Docker Cluster

Once you are switched into the cluster, run the `create_docker` command which creates a Docker cluster that is specific to that product.

```bash
# Create Mosquitto cluster with 3 members (default) with the default host name prefix, 'broker'.
# You can change the prefix with the -prefix option.
create_docker -cluster edge
```

By default, the `create_docker` command adds two (3) Mosquitto brokers (members) in the cluster. You can change the number of brokers using the `-count` option. For example, the following command adds five (5) brokers.

```bash
# Create Mosquitto cluster with five (5) brokers with the default host name prefix, 'broker'. 
create_docker -cluster edge -count 5
```

## Configure the Cluster Environment

```bash
cd_docker edge
```

Edit the `.env` file as needed.

```bash
vi .env
```

There are four configuration files as follows. Edit them as needed.

| Configuration File                 | For            |
| ---------------------------------- | -------------- |
| `padogrid/etc/mqttv5-client.yaml`  | `HaMqttClient` |
| `padogrid/etc/simulator.yaml`      | MQTT data feed |


The `padogrid-mqtt` containers are configured with the `padogrid/etc/simulator.yaml` file for publishing simulated data. Before starting the containers, you can replace or modify this file as needed to generate the desired data.

```bash
vi padogrid/etc/simulator.yaml
```

## Start Cluster

```bash
cd_docker edge
docker compose up
```

## Run `vc_subscribe`

If you launched the default cluster, i.e., three (3) brokers, then you can create and run `vc_subscribe` without the endpoints specified. By default, `vc_subscribe` creates a virtual cluster comprised of the endpoints `tcp://localhost:1883-1885`. 

✏️  The `create_docker` command generates the `docker-compose.yaml` file with the topic name prefix set with the cluster name followed by `/broker<number>`. For this example, the first broker's topic name prefix is set as follows.

```yaml
services:
  broker1:
  ...
    environment:
      - PADOGRID_TOPIC_PREFIX=edge/broker1
  ...
```

The following command listens on data published by the first broker, `broker1`, or the endpoint, `tcp://localhost:1883`. 

```bash
# Subscribe to broker1 - tcp://localhost:1883
vc_subscribe -t edge/broker1/#
```

To listen on messages from all the brokers, subscribe to `edge/#`.

```bash
# Subscribe to all of the default brokers
vc_subscribe -t edge/#
```

If you have created cluster with additional brokers, then you can specify the `-endpoints` option to create a virutal cluster that includes them. For example, the following includes five (5) endpoints.

```bash
# Subscribed to five (5) brokers
vc_subscribe -endpoints tcp://localhost:1883-1887 -t edge/#
```

## Run `chart_mqtt`

The simulator comes in a bundle, which also includes the `chart_mqtt` command that graphically displays the simulated MQTT data. You can install the bundle as follows.

```bash
install_bundle -download -quiet bundle-none-app-simulator
```

Once installed, change directory to `simulator` and run the `chart_mqtt` command as follows.

```bash
cd_app simulator/bin_sh
./build_app
./chart_mqtt -t edge/broker1/sine
```

The simulator in each `padogrid-mqtt` container publishes to numerous topics. You can find the topic names from the `vc_subscribe` output or the `simulator.yaml` file.

```bash
cd_docker edge
cat padogrid/etc/simulator.yaml
```

Some interesting charts are as follows.

```bash
cd_app simulator/bin_sh
./chart_mqtt -t edge/broker1/dampedSineWave
./chart_mqtt -t edge/broker1/circle
./chart_mqtt -t edge/broker1/tanh
./chart_mqtt -t edge/broker1/heartbeat
```

Like `vc_subscribe`, `chart_mqtt` also supports the `-endpoints` option. The following `chart_mqtt` command creates a virtual cluster that includes five (5) brokers and displays the `circle` data published by `broker5` that has the endpoint `tcp://localhost:1887`.

```bash
# Both of the following display the same data received from broker5
./chart_mqtt -endpoints tcp://localhost:1883-1887 -t edge/broker5/circle
./chart_mqtt -endpoints tcp://localhost:1887 -t edge/broker5/circle
```

## Run `perf_test`

If you have launched the default cluster, i.e., three (3) brokers, then you can create and run `perf_test` as is. If you have changed the cluster endpoints, then you need to set the endpoints for `perf_test` in the `etc/mqttv5-client.yaml` file as follows.

```bash
create_app
cd_app perf_test; cd bin_sh
vi etc/mqttv5-client.yaml
```

Change the value of `serverURIs` accordiningly.

```yaml
          serverURIs: [tcp://localhost:1883-1885]
```

Run `perf_test`.

```bash
cd bin_sh
./test_group -run
```

## Teardown

Ctrl-C from the `docker compose up` command and prune the containers.

```bash
docker compose down
docker container prune
```

## Tips

1. If you get the **"No space left on device"** error when you start `docker compose`, run the following to free up the disk space.

```bash
docker system prune --volumes
````

## References
1. Install Docker, <ttps://docs.docker.com/install/>.
2. Install Docker Compose, Docker Compose is now part of the `docker` command. Before installing `docker-compose`, check to see if this option exists, <https://docs.docker.com/compose/install/>. 
3. Data Feed Simulator, bundle-none-app-simulator, PadoGrid Bundles, <https://github.com/padogrid/bundle-none-app-simulator>.
