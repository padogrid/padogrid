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
# Create Mosquitto cluster with 3 members (default)
create_docker -cluster mydocker
```

By default, the create_docker command adds two (3) Hazelcast brokers (members) in the cluster. You can change the number of brokers using the `-count` option. For example, the following command adds four (4) servers.

```bash
# Create Mosquitto cluster with 4 brokers
create_docker -cluster mydocker -count 4
```

## Configure the Cluster Environment

```bash
cd_docker mydocker
```

Edit the `.env` file as needed.

```bash
vi .env
```

There are four configuration files as follows. Edit them as needed.

| Configuration File                 | For            |
| ---------------------------------- | -------------- |
| `padogrid/etc/mosquitto.yaml`      | Mosquitto      |
| `padogrid/etc/log4j2.properties`   | `HaMqttClient` |
| `padogrid/etc/mqttv5-client.yaml`   | `HaMqttClient` |
| `padogrid/etc/simulator-edge.yaml` | MQTT data feed |

Place your application jar files in the `padogrid/plugins` directory, which already contains PadoGrid test jar for running `perf_test`. 

```bash
ls padogrid/plugins
```

## Start Cluster

```bash
docker compose up
```

## Log Files

The log files are generated in the `padogrid/log` directory. The following example tails the first member's log file.

```bash
cd_docker mydocker
tail -f padogrid/log/mydocker-broker1.log
```

## Run `perf_test`

If you have not changed the Hazelcast cluster name, you can run `perf_test` as is without modifications.

```bash
create_app
cd_app perf_test; cd bin_sh
./test_ingestion -run
```
 
If you have changed the Hazelcast cluster name, then add the `<cluster-name>` element in the `etc/hazelcast-client.xml` file as follows:

```xml
<hazelcast...>
...
   <cluster-name>mydocker</cluster-name>
...
</hazelcast>
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
1. Install Docker, [https://docs.docker.com/install/](https://docs.docker.com/install/).
2. Install Docker Compose, Docker Compose is now part of the `docker` command. Before installing `docker-compose`, check to see if this option exists, [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/). 
