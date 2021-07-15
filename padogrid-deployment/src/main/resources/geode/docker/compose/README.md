# Geode Docker Compose

This article describes how to create a Docker environment and launch a Geode cluster using `docker-compose`.

:exclamation: You must first install Docker and Docker Compose. See [References](#References) for download links.

## Create PadoGrid Docker Cluster

```console
create_docker -cluster mydocker
```

By default, the create_docker command adds one (1) locator and two (2) Geode servers (members) in the cluster. You can change the number of servers using the `-count` option. For example, the following command adds four (4) servers.

```console
# Create Geode cluster with 4 members
create_docker -cluster mydocker -count 4
```

If you are running Docker containers other than Geode containers and they need to connect to the Geode cluster, then specify host IP address that is accessble from the containers using the `-host` option. For example, the following example specifies the host IP, `host.docker.internal`, which is defined by Docker Desktop. Please run `create_docker -?` or `man create_docker` for the usage.

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
├── .env
├── README.md
├── docker-compose.yaml
└── padogrid
    ├── etc
    │   ├── cache.xml
    │   ├── gemfire.properties
    │   ├── hibernate.cfg-mysql.xml
    │   ├── hibernate.cfg-postgresql.xml
    │   ├── log4j2.properties
    │   └── prometheus.yml
    ├── lib
    │   ├── padogrid-core-0.9.0-SNAPSHOT.jar
    │   ├── jmx_prometheus_javaagent-0.11.0.jar
    │   ├── log4j-api-2.11.2.jar
    │   └── log4j-core-2.11.2.jar
    ├── log
    ├── plugins
    │   └── padogrid-core-0.9.0-SNAPSHOT-tests.jar
    └── stats
```

Edit the `.env` file as needed. For example, you can change the host IP by setting the `HOSTNAME_FOR_CLIENTS` environment variable in this file.

```console
vi .env
```

Configure Geode servers by editing `padogrid/etc/gemfire.properties` and `padogrid/etc/cache.xml`.

```console
vi padogrid/etc/gemfire.properties
vi padogrid/etc/cache.xml
```

Place your application jar files in the `padogrid/plugins` directory, which already contains PadoGrid test jar for running `perf_test`. 

```console
ls padogrid/plugins/
```

## Start Cluster

```console
docker-compose up
```

## Run `gfsh`

`gfsh` must be run in the locator container.

```console
docker container exec -it mydocker_locator_1 bash
gfsh
gfsh>connect --locator=locator[10334]
gfsh>list members
```

**Output:**

```console
Member Count : 3

 Name   | Id
------- | -------------------------------------------------------------
locator | 192.168.128.2(locator:35:locator)<ec><v0>:41000 [Coordinator]
server1 | 192.168.128.3(server1:33)<v1>:41000
server2 | 192.168.128.4(server2:33)<v1>:41000
```

## Run Pulse

URL: http://localhost:7070/pulse

## Run Swagger UI

**URL:** [http://localhost:7080/geode/swagger-ui.html](http://localhost:7080/geode/swagger-ui.html)

## Run `perf_test`

You can run `perf_test` as is without modifications.

```console
create_app
cd_app perf_test; cd bin_sh
./test_ingestion -run
```

## Tear Down

Ctrl-C from the `docker-compose up` command and prune the containers.

```console
docker-compose down
docker container prune
```

## References
1. Install Docker, [https://docs.docker.com/install/](https://docs.docker.com/install/).
2. Install Docker Compose, [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/). 
