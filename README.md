![PadoGrid](https://github.com/padogrid/padogrid/raw/develop/images/padogrid-3d-16x16.png) [*PadoGrid*](https://github.com/padogrid) | [*Catalogs*](https://github.com/padogrid/catalog-bundles/blob/master/all-catalog.md) | [*Manual*](https://github.com/padogrid/padogrid/wiki) | [*FAQ*](https://github.com/padogrid/padogrid/wiki/faq) | [*Releases*](https://github.com/padogrid/padogrid/releases) | [*Templates*](https://github.com/padogrid/padogrid/wiki/Using-Bundle-Templates) | [*Pods*](https://github.com/padogrid/padogrid/wiki/Understanding-Padogrid-Pods) | [*Kubernetes*](https://github.com/padogrid/padogrid/wiki/Kubernetes) | [*Docker*](https://github.com/padogrid/padogrid/wiki/Docker) | [*Apps*](https://github.com/padogrid/padogrid/wiki/Apps) | [*Quick Start*](https://github.com/padogrid/padogrid/wiki/Quick-Start) 

---

# PadoGrid

The PadoGrid project aims to deliver a data grid platform with out-of-the-box turnkey solutions to many enterprise architecture use cases. The solutions come in the form of bundles which you simply *install and run*. See the following links for details.

- [**PadoGrid Manual**](https://github.com/padogrid/padogrid/wiki)
- [**FAQ**](https://github.com/padogrid/padogrid/wiki/faq)
- [**Install, run, and learn PadoGrid in less than five (5) minutes**](https://github.com/padogrid/padogrid/wiki/PadoGrid-in-5-Minutes)

---

## Announcements

- *August 29, 2023 - [Neural Network: LSTM RNN updated.](https://github.com/padogrid/bundle-hazelcast-5-app-ml_lstm-cluster-ml_jet) This bundle has been updated to use the latest `HaMqttClient` API included in PadoGrid v0.9.28.*
- *August 28, 2023 - [PadoGrid v0.9.28 released.](https://github.com/padogrid/padogrid/releases/tag/padogrid_0.9.28) This release moves the [`padogrid/padogrid`](https://hub.docker.com/repository/docker/padogrid/padogrid/general) container image from Alpine to Ubuntu and introduces the [`padogrid/padogrid-base`](https://hub.docker.com/repository/docker/padogrid/padogrid-base/general) container image. The `padogrid/padogrid` container image is now fully capable of running Python bundles that require Python graphics display and AI/ML packages.*
- *August 28, 2023 - [padogrid/padogrid-base container image released.](https://hub.docker.com/repository/docker/padogrid/padogrid-base/general) This container image includes only PadoGrid, Mosquitto, and Java for building light-weight images.*

---

## Monthly Tips

- Building MQTT applications that require High Availability? Checkout the [archetypes](https://github.com/padogrid/padogrid/wiki/Cluster-Archetypes) offerred by `HaMqttClient`.
- Want to forecast future events? Checkout the [Neural Network: LSTM RNN](https://github.com/padogrid/bundle-hazelcast-5-app-ml_lstm-cluster-ml_jet) bundle.
- In need of benchmarking IMDG products? Try the [IMDG Product Benchmark Tests](https://github.com/padogrid/bundle-none-imdg-benchmark-tests) bundle.
- New to Hazelcast? Have fun learning Hazelcast data structures in the [PadoGrid Hazelcast Playground](https://github.com/padogrid/bundle-hazelcast-5-playground-python) bundle.
- Checkout the [bundle catalogs](https://github.com/padogrid/catalog-bundles/blob/master/all-catalog.md) for additional use cases.

---

## Installation

You can install PadoGrid and the supported data grid products by running the interactive [**install_padogrid**](https://raw.githubusercontent.com/padogrid/padogrid/develop/padogrid-deployment/src/main/resources/common/bin_sh/install_padogrid) script. Copy and paste the following command into your terminal.

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/padogrid/padogrid/develop/padogrid-deployment/src/main/resources/common/bin_sh/install_padogrid)"
```

The above command may fail on some Linux variants due the large length of the command line arguments. In that case, you need to download and run `install_padogrid` as follows.

```bash
curl -O -fsSL https://raw.githubusercontent.com/padogrid/padogrid/develop/padogrid-deployment/src/main/resources/common/bin_sh/install_padogrid
chmod 755 install_padogrid
./install_padogrid
```

:pencil2: *Note that `install_padogrid` is part of PadoGrid. Once PadoGrid is installed, you can run `install_padogrid` at any time to upgrade or downgrade products.*

[**Quick Start**](https://github.com/padogrid/padogrid/wiki/Quick-Start) provides detailed instructions. 

:pencil2: *The latest PadoGrid snapshot release is available if you cannot wait till the next release. It is automatically built when there were changes made in [RELEASE_NOTES.md](RELEASE_NOTES.md). You can download it from [Releases](https://github.com/padogrid/padogrid/releases) or using `install_padogrid`.*

## PadoGrid Container

To run PadoGrid as a container, please follow the instructions in the links below.

- [Running PadoGrid using Docker and Podman](#running-padogrid-using-docker-and-podman)
- [Running PadoGrid in Kubernetes](#running-padogrid-in-kubernetes)


## Downloads

PadoGrid binary downloads are available from the *Releases* page. If your host does not have access to the Internet and you are unable to run `install_padogrid` then you can download a version from this link and install it manually.

[PadoGrid Releases/Downloads](https://github.com/padogrid/padogrid/releases)

Download links to all the supported data grids are also provided in the following page:

[Supported Data Grid Downloads](https://github.com/padogrid/padogrid/wiki/Supported-Data-Grid-Products-and-Downloads)

Online use case bundles:

[Bundle (Use Case) Catalogs](https://github.com/padogrid/padogrid/wiki/Bundle-Catalogs)

## PadoGrid Brief

PadoGrid is a collection of add-on components and tools specifically designed for [data grid products](#data-Grid-Products) to deliver out-of-the-box, shrink-wrapped solutions. It introduces the concept of *distributed workspaces* for creating DevOps environments in which use cases can be quickly developed, tested, deployed and shared.

A workspace provides a sandbox environment completely isolated from other workspaces and can host a wide range of software components from a simple app to a highly complex ecosystem with many data grid clusters, apps, VMs, and Docker/Kubernetes containers. You can, for example, create a workspace that federates multiple data grid clusters serving inventory and sales data, a workspace that streams database CDC records via Kafka, a workspace that handles streamed data into the federated clusters via one or more Apache Spark or Hazelcast Jet clusters, and yet another workspace that integrates data analytics tools for performing AI/ML operations and creating reports. PadoGrid consolidates your workspaces into a single operations center.

<p align="center" float="left">
  <a>
  <img src="https://raw.githubusercontent.com/wiki/padogrid/padogrid/images/distributed-workspace.png" hspace="14" alt="Distributed Workspace" />
  </a>
</p>

A workspace snapshot can be taken at any time in the form of a bundle that can be quickly deployed and run on another workspace created by another user on another platform. Because of their portability, bundles provide the means to shrink-wrap fully operational use cases. PadoGrid includes bundle catalogs from which you can search your use cases.

- [**Releases/Downloads**](https://github.com/padogrid/padogrid/releases)
- [**Quick Start**](https://github.com/padogrid/padogrid/wiki/Quick-Start)
- [**PadoGrid Manual**](https://github.com/padogrid/padogrid/wiki)
- [Bundle Catalogs](https://github.com/padogrid/padogrid/wiki/Bundle-Catalogs)
- [Bundle Templates](https://github.com/padogrid/padogrid/wiki/Using-Bundle-Templates)
- [Building PadoGrid](#building-padogrid)

## PadoGrid Features

- [Distributed Workspaces](https://github.com/padogrid/padogrid/wiki/Understanding-Workspaces)
- [Multiple Data Grid Products](https://github.com/padogrid/padogrid/wiki/Supported-Data-Grid-Products-and-Downloads)
- [Performance Test App](https://github.com/padogrid/padogrid/wiki/Geode-perf_test-App)
- [Data Grid Desktop App](https://github.com/padogrid/padogrid/wiki/Hazelcast-Desktop-App)
- [Grafana App](https://github.com/padogrid/padogrid/wiki/Geode-Grafana-App)
- [Kubernetes Autoscaler](https://github.com/padogrid/padogrid/wiki/Kubernetes)
- [Docker Clusters](https://github.com/padogrid/padogrid/wiki/Docker)
- [PadoGrid (Vagrant) Pods](https://github.com/padogrid/padogrid/wiki/Understanding-PadoGrid-Pods)
- [Hazelcast Query Language (HQL)](https://github.com/padogrid/padogrid/wiki/HQL-Query)
- [MQTT Virtual Clusters](https://github.com/padogrid/padogrid/wiki/Clustering-MQTT)
- [Integrated Tools (JupyterLab/VS Code)](https://github.com/padogrid/padogrid/wiki/Integrated-Tools)
- [Use Cases via Online Bundles](https://github.com/padogrid/padogrid/wiki/Bundle-Catalogs)

## Building `padogrid`

### Required Software

- Maven 3.x
- JDK 1.8+

### Building `padogrid` Distribution

There are a number of ways to build a PadoGrid distribution. For building a standard distribution release, run the `build_dist.sh` script as follows.

```bash
# Include all products and their man pages (standard distribution)
./build_dist.sh -all
```

You can also build distributions that are tailored to your needs as described in the following link.

[Building PadoGrid](https://github.com/padogrid/padogrid/wiki/Building-padogrid)

## Installing `padogrid` you have built

Released versions of PadoGrid are normally installed by running the `install_padogrid` command. For those that you have built, however, must be installed manually as describe in this section.

Upon successful build, the following distribution files will be generated.

```bash
padogrid-deployment/target/assembly/padogrid_<version>.tar.gz
padogrid-deployment/target/assembly/padogrid_<version>.zip
```

Inflate one of the distribution files in your file system. For example,

```bash
mkdir -p ~/Padogrid/products
tar -C ~/Padogrid/products/ -xzf padogrid_0.9.29-SNAPSHOT.tar.gz
cd ~/Padogrid/products
tree -L 1 padogrid_0.9.29-SNAPSHOT
```

**Output:**

```bash
padogrid_0.9.29-SNAPSHOT
├── LICENSE
├── NOTICE
├── README.md
├── RELEASE_NOTES.md
├── apps
├── bin_sh
├── coherence
├── etc
├── geode
├── hadoop
├── hazelcast
├── kafka
├── lib
├── mosquitto
├── none
├── pods
├── redis
├── snappydata
├── spark
└── workspace
```

## Initializing PadoGrid

:pencil2: If you have run `install_padogrid`, then you have already initialized PadoGrid and you can skip this section.

To use PadoGrid, you must first create an RWE (Root Workspace Environment) by running the interactive command, `create_rwe`, to specify the workspaces directory and the product installation paths.

```bash
~/Padogrid/products/padogrid_0.9.29-SNAPSHOT/bin_sh/create_rwe
```

## Running PadoGrid using Docker and Podman

To save your workspaces created in the container, it is recommended that you mount a data volume along with ports exposed as follows.

```bash
# Docker
docker run --name padogrid -h padogrid -d \
   --mount type=volume,source=padogrid,target=/opt/padogrid \
   -p 8888:8888 -p 5701:5701 -p 5702:5702 -p 5703:5703 -p 8080:8080 \
   -p 9401:9401 -p 9402:9402 -p 9403:9403 -p 3000:3000 -p 9090:9090 \
   -e PADOGRID_HTTPS_ENABLED=true padogrid/padogrid

# Podman
podman run --name padogrid -h padogrid -d \
   --mount type=volume,source=padogrid,target=/opt/padogrid \
   -p 8888:8888 -p 5701:5701 -p 5702:5702 -p 5703:5703 -p 8080:8080 \
   -p 1883:1883 -p 1884:1883 -p 1885:1883 -p 5703:5703 \
   -p 9401:9401 -p 9402:9402 -p 9403:9403 -p 3000:3000 -p 9090:9090 \
   -e PADOGRID_HTTPS_ENABLED=true padogrid/padogrid
```

Once the PadoGrid container is started, you can access the container as follows.

### Login from browser:

- PadoGrid URL: <https://0.0.0.0:8888>
- Password: `padogrid`

### Login from shell:

```bash
# Docker
docker exec -it padogrid /bin/bash

# Podman
poman exec -it padogrid /bin/bash
```

:pencil2: PadoGrid Docker containers follow the same version conventions as the build except for the SNAPSHOT versions which also include a build number starting from 1. For example, the `padogrid/paadogrid:0.9.29-SNAPSHOT-2` image has the build number 2. The SNAPSHOT versions are for testing only and subject to removal without notice.

For additional details, see the [Docker](https://github.com/padogrid/padogrid/wiki/Docker) section of the manual.

## Running PadoGrid in Kubernetes

You can run PadoGrid in Kubernetes as shown below. The PadoGird container stores workspaces in the `/opt/padogrid/workspaces` directory, which you can mount to a persistent volume as needed.

```bash
# kubectl
kubectl run padogrid --image=docker.io/padogrid/padogrid

# oc
oc run padogrid --image=docker.io/padogrid/padogrid
```

To login to the PadoGrid pod, make sure to specify the command, `bash`, as follows.

```bash
# kubectl
kubectl exec -it padogrid -- bash

# oc
oc exec -it padogrid -- bash
```

If you have a Hazelcast cluster running in the same namespace (project) as PadoGrid, then you can run the `perf_test` app as follows.

```bash
export NAMESPACE=<Kubernetes namespace/project>
export HAZELCAST_SERVICE=<Hazelcast Kubernetes service>
# Default cluster name is "dev".
export HAZELCAST_CLUSTER_NAME=<cluster name>
create_app
cd_app perf_test; cd bin_sh
./test_ingestion -run
```

To delete the PadoGrid pod:

```bash
# kubectl
kubectl delete pod paodgrid

# oc
oc delete pod padogrid
```

You may encounter a random userid assigned by Kubernetes and OpenShift instead of the required fixed userid, **padogrid**, especially when you deploy the PadoGrid container. The following link provides further details on running PadoGrid with the fixed userid when deployed in OpenShift.

[Deploying PadoGrid in OpenShift](https://github.com/padogrid/padogrid/wiki/Kubernetes#deploying-padogrid-in-openshift)

## Data Grid Products

PadoGrid natively supports the following data grid, messaging, and analytics products.

---

<p align="center" float="left">
  <a href="https://geode.apache.org/">
  <img src="images/geode.jpg" width="210" height="80" hspace="14" alt="Apache Geode" />
  </a>
  <a href="https://tanzu.vmware.com/gemfire">
  <img src="images/gemfire.jpg" width="210" height="80" hspace="14" alt="VMware GemFire" /> 
  </a>
</p>
<p align="center">
  <a href="https://hazelcast.com/products/imdg/">
  <img src="images/hazelcast.jpg" width="300" height-"80" hspace="14" alt="Hazelcast IMDG" />
  </a>
  <a href="https://hazelcast.com/products/jet/">
  <img src="images/jet.jpg" width="280" height="80" hspace="14" alt="Hazelcast Jet" />
  </a> 
</p>
<p align="center">
  <a href="https://www.tibco.com/products/tibco-computedb">
  <img src="images/computedb.jpg" width="300" height="80" hspace="14" alt="ComputeDB" />
  </a>
  <a href="https://github.com/TIBCOSoftware/snappydata">
  <img src="images/snappydata.jpg" width="280" height="80" hspace="14" alt="SnappyData" />
  </a> 
</p>
<p align="center">
  <a href="https://www.oracle.com/middleware/technologies/coherence.html">
  <img src="images/coherence.jpg" width="200"  height="80" hspace="14" alt="Oracle Coherence" />
  </a>
  <a href="https://spark.apache.org/">
  <img src="images/spark.png" width="200" height="80" hspace="14" alt="Spark" />
  </a> 
</p> 
<p align="center">
  <a href="https://hadoop.apache.org/">
  <img src="images/hadoop.jpg" width="200" height="80" hspace="14" alt="Hadoop" />
  </a>
  <a href="https://kafka.apache.org/">
  <img src="images/kafka.png" width="200" height="80" hspace="14" alt="Kafka" />
  </a> 
</p> 
<p align="center">
  <a href="https://mosquitto.org/">
  <img src="images/mosquitto.png" width="270" height="80" hspace="14" alt="Mosquitto" />
  </a>
  <a href="https://redis.io/">
  <img src="images/redis.png" width="200" height="80" hspace="14" alt="Redis" />
  </a>
</p> 

---

## Where To Go From Here

### PadoGrid Manual

The PadoGrid Manual describes product concepts and provides complete instructions for configuring and operating PadoGrid. It also includes many tutorials and working examples that you can quickly try out on your laptop.

[PadoGrid Manual](https://github.com/padogrid/padogrid/wiki)

### Bundle (Use Case) Catalogs

PadoGrid has been built with use cases in mind. It aims to deliver out-of-the-box turnkey solutions on top of data grid products. The bundle catalogs provide compiled lists of readily available solutions. Just install and run.

[Bundle (Use Case) Catalogs](https://github.com/padogrid/padogrid/wiki/Bundle-Catalogs)

### Creating Your Own Bundles

You can also create online bundles hosted by your repos. The following link provides how-to instructions.

[Understanding Bundles](https://github.com/padogrid/padogrid/wiki/Understanding-Bundles)

### Bundle Templates

Creating your own online bundles is made easy by using the bundle templates. The following link povides template links.

[Using Bundle Templates](https://github.com/padogrid/padogrid/wiki/Using-Bundle-Templates)

### FAQ

The FAQ link aims to provide a comprensive collection of PadoGrid Q&As including a series of topics on how to build distributed systems using PadoGrid.

[FAQ](https://github.com/padogrid/padogrid/wiki/faq)

---

![PadoGrid](https://github.com/padogrid/padogrid/raw/develop/images/padogrid-3d-16x16.png) [*PadoGrid*](https://github.com/padogrid) | [*Catalogs*](https://github.com/padogrid/catalog-bundles/blob/master/all-catalog.md) | [*Manual*](https://github.com/padogrid/padogrid/wiki) | [*FAQ*](https://github.com/padogrid/padogrid/wiki/faq) | [*Releases*](https://github.com/padogrid/padogrid/releases) | [*Templates*](https://github.com/padogrid/padogrid/wiki/Using-Bundle-Templates) | [*Pods*](https://github.com/padogrid/padogrid/wiki/Understanding-Padogrid-Pods) | [*Kubernetes*](https://github.com/padogrid/padogrid/wiki/Kubernetes) | [*Docker*](https://github.com/padogrid/padogrid/wiki/Docker) | [*Apps*](https://github.com/padogrid/padogrid/wiki/Apps) | [*Quick Start*](https://github.com/padogrid/padogrid/wiki/Quick-Start) 
