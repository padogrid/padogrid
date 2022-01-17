# SnappyData Docker Compose

This article describes how to create a Docker environment and launch a SnappyData cluster using `docker-compose`.

:exclamation: You must first install Docker and Docker Compose. See [References](#References) for download links.

## Create PadoGrid Docker Cluster

```console
create_docker -cluster mydocker
```

By default, the create_docker command adds one (1) locator, two (2) SnappyData servers (members), and one (1) leader in the cluster. You can change the number of servers using the `-count` option. For example, the following command adds four (4) servers.

```console
# Create SnappyData cluster with 4 members
create_docker -cluster mydocker -count 4
```

If you are running Docker containers other than SnappyData containers and they need to connect to the SnappyData cluster, then specify host IP address that is accessible from the containers using the `-host` option. For example, the following example specifies the host IP, `host.docker.internal`, which is defined by Docker Desktop. Please run `create_docker -?` or `man create_docker` for the usage.

```console
create_docker -cluster mydocker -host host.docker.internal
```

## Configure the Cluster Environment

First, change directory to `mydocker`.

```console
cd_docker mydocker
```

The `mydocker` directory structure is shown below.

```console
mydocker
├── etc
│   ├── gemfirexd.properties
│   ├── log4j2.properties
│   └── prometheus.yml
├── lib
│   ├── jmx_prometheus_javaagent-0.11.0.jar
│   ├── log4j-api-2.11.2.jar
│   ├── log4j-core-2.11.2.jar
│   └── snappydata-addon-core-0.9.2-SNAPSHOT.jar
├── log
└── stats
```

Edit the `.env` file as needed. For example, you can change the host IP by setting the `HOSTNAME_FOR_CLIENTS` environment variable in this file.

```console
vi .env
```

Configure SnappyData servers by editing `padogrid/etc/gemfirexd.properties`.

```console
vi padogrid/etc/gemfirexd.properties
vi padogrid/etc/cache.xml
```

## Start Cluster

```console
docker-compose up
```

## Run `snappy`

Run `snappy` to connect to the cluster.

```console
snappy
snappy> connect client 'localhost:1527'
snappy> show schemas
```

**Output:**

```console
databaseName
---------------------------------------------
app
default
sys

3 rows selected
```
 
## Run SnappyData Monitoring Console (Pulse)

**Pulse URL:** [http://localhost:5050/dashboard/](http://localhost:5050/dashboard/)

## Connect via JDBC Client

Using SQL client applications such as [SQuirreL SQL](http://squirrel-sql.sourceforge.net/), you can connect to a locator or server (member) and execute queries. 

**JDBC URL:** jdbc:snappydata://localhost:1527/

## Teardown

Ctrl-C from the `docker-compose up` command and prune the containers.

```console
docker-compose down
docker container prune
```

## References

1. Install Docker, [https://docs.docker.com/install/](https://docs.docker.com/install/).
2. Install Docker Compose, [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/).

