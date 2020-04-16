# PadoGrid

The PadoGrid project aims to deliver a data grid platform with out-of-the-box turnkey solutions to many enterprise architecture use cases. The solutions come in the form of bundles which you simply *install and run*.

## PadoGrid Brief

PadoGrid is a collection of add-on components and tools specifically designed for data grid products to deliver out-of-the-box shrink-wrapped solutions. It introduces the concept of *distributed workspaces* for creating DevOps environments in which use cases can be quickly developed, tested, deployed and shared.

A workspace provides a sandbox environment completely isolated from other workspaces and can host a wide range of software components from a simple app to a highly complex ecosystem with many data grid clusters, apps, VMs, and Docker/Kubernetes containers. You can, for example, create a workspace that federates multiple data grid clusters serving inventory and sales data, a workspace that streams database CDC records via Kafka, a workspace that handles streamed data into the federated clusters via one or more Apache Spark or Hazelcast Jet clusters, and yet another workspace that integrates data analytics tools for performing AI/ML operations and creating reports. PadoGrid consolidates your workspaces into a single operations center.

A workspace snapshot can be taken at any time in the form of a bundle that can be quickly deployed and run on another workspace created by another user on another platform. Because of their portability, bundles provide the means to shrink-wrap fully operational use cases. PadoGrid includes bundle catalogs from which you can search your use cases.

- [**PadoGrid Manual**](https://github.com/padogrid/padogrid/wiki)
- [Bundle Catalogs](https://github.com/padogrid/padogrid/wiki/Bundle-Catalogs)
- [Quick Start](https://github.com/padogrid/padogrid/wiki/Quick-Start)
- [Building PadoGrid](#Building-padogrid)
- [Installing PadoGrid](#Insalling-padogrid)

## PadoGrid Features

- [Distributed Workspaces](https://github.com/padogrid/padogrid/wiki/Understanding-Workspaces)
- [Multiple Data Grid Products](https://github.com/padogrid/padogrid/wiki/Supported-Data-Grid-Products)
- [Performance Test App](https://github.com/padogrid/padogrid/wiki/Geode-perf_test-App)
- [Data Grid Desktop App](https://github.com/padogrid/padogrid/wiki/Hazelcast-Desktop-App)
- [Grafana App](https://github.com/padogrid/padogrid/wiki/Geode-Grafana-App)
- [Kubernetes Autoscaler](https://github.com/padogrid/padogrid/wiki/Kubernetes)
- [Docker Clusters](https://github.com/padogrid/padogrid/wiki/Docker)
- [Vagrant Pods](https://github.com/padogrid/padogrid/wiki/Understanding-Vagrant-Pods)
- [Hazelcast Query Language (HQL)](https://github.com/padogrid/padogrid/wiki/HQL-Query)
- [Use Cases in Online Bundles](https://github.com/padogrid/padogrid/wiki/Bundle-Catalogs)

## Building `padogrid`

You can build `padogrid` using any of the following options. For distribution, always include man pages.

```console
# Include man pages (recommended)
./build_dist.sh

# Without man pages (fast build)
./build_dist.sh -skipMan

# Maven (without man pages, fastest build)
mvn install

# Build all: build_dist.sh + external apps (slowest and largest build)
./build_all.sh
```

## Installing `padogrid`

Upon successful build, the following distribution files will be generated.

```console
# The following distributions contain all the padogrid components.
padogrid-deployment/target/assembly/padogrid_<version>.tar.gz
padogrid-deployment/target/assembly/padogrid_<version>.zip

# The following distributions contain all the padogrid components plus
# external applications.
padogrid-deployment/target/assembly/padogrid-all_<version>.tar.gz
padogrid-deployment/target/assembly/padogrid-all_<version>.zip
```

Inflate one of the distribution files in your file system. For example,

```console
mkdir ~/Padogrid/products
tar -C ~/Padogrid/products/ -xzf padogrid_0.9.1-SNAPSHOT.tar.gz
cd ~/Padogrid/products
tree -L 1 padogrid_0.9.1-SNAPSHOT
```

**Output:**

```console
padogrid_0.9.1-SNAPSHOT
├── LICENSE
├── NOTICE
├── README.md
├── RELEASE_NOTES.txt
├── bin_sh
├── etc
├── geode
├── hazelcast
├── lib
└── pods
```

## Data Grid Products

PadoGrid currently supports the following data grid products.

---

<p align="center" float="left">
  <a href="https://geode.apache.org/">
  <img src="images/geode.jpg" width="200" hspace="10" alt="Apache Geode" />
  </a>
  <a href="https://tanzu.vmware.com/gemfire">
  <img src="images/gemfire.jpg" width="200"  hspace="10" alt="VMware GemFire" /> 
  </a>
</p>
<p align="center">
  <a href="https://hazelcast.com/products/imdg/">
  <img src="images/hazelcast.jpg" width="290"  hspace="10" alt="Hazelcast IMDG" />
  </a>
  <a href="https://hazelcast.com/products/jet/">
  <img src="images/jet.jpg" width="270" hspace="10" alt="Hazelcast Jet" />
  </a> 
</p>

---

## Where To Go From Here

### PadoGrid Manual

The PadoGrid Manual describes product concepts and provides complete instructions for configuring and operating PadoGrid. It also includes many tutorials and working examples that you can quickly try out on your laptop.

[PadoGrid Manual](https://github.com/padogrid/padogrid/wiki)

### Bundle Catalogs

PadoGrid has been built with use cases in mind. It aims to deliver out-of-the-box turnkey solutions on top of data grid products. The bundle catalogs provide compiled lists of readily available solutions. Just install and run.

[Bundle Catalogs](https://github.com/padogrid/padogrid/wiki/Bundle-Catalogs)
