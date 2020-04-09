# WAN Example

This article describes a complete set of steps for creating a local WAN environment. We'll create two (2) Geode clusters named **`ny`** and **`ln`** running on `localhost`. We'll then configure the `ny` cluster to replicate transactional data to the `ln` cluster and run the `perf_test` apps to generate transactional data.

A complementary bundle that has WAN clusters preconfigured and ready to be run is available in the [geode-addon-bundles](https://github.com/javapark1/geode-addon-bundles/tree/master/help/bundle-geode-1.11.0-cluster-app-ln-ny-perf_test_wan) repo. If you want to skip this article and just run the bundle, then you can install the bundle as follows.

```console
# To see instructions
show_bundle bundle-geode-1.11.0-cluster-app-ln-ny-perf_test_wan.tar.gz

# To install bundle
install_bundle -download bundle-geode-1.11.0-cluster-app-ln-ny-perf_test_wan.tar.gz
```

The steps we'll take are typical steps that you would take when preparing `geode-addon` for your local environment.

0. [Install `geode-addon`](https://github.com/javapark1/geode-addon#building-geode-addon)
1. [Create Workspace](#1-Create-Workspace)
2. [Create Clusters](#2-Create-Clusters)
3. [Update Configuration Files](#3-Update-Configuration-Files)
4. [Start Clusters](#4-Start-Clusters)
5. [Monitor Clusters](#5-Monitor-Clusters)
6. [Ingest Data](#6-Ingest-Data)
7. [Tear Down](#7-Tear-Down)
8. [Summary](#8-Summary)

## 1. Create Workspace

You can skip this section if you already have a workspace in which you want to walk through this tutorial.

Let's create a new workspace where we can create and test WAN clusters. Run the `create_workspace` command which will prompt for Java, Geode, workspace name, default cluster, and VM prompts. **Make sure to take the default `false` value for the `Enable VM?` prompt.** We'll be running clusters locally on your laptop.

```console
create_workspace -name ws-wan
```

Make sure to switch workspace into the new workspace you just created.

```console
switch_workspace ws-wan
```

## 2. Create Clusters

Create the `ny` and `ln` clusters with different locator start port numbers as shown below.

```
# Create the ny cluster
create_cluster -cluster ny -port 10334

# Create the ln cluster
create_cluster -cluster ln -port 10344
```

## 3. Update Configuration Files

Let's now configure the `cache.xml` files to configure the `summary` region to replicate over the WAN. 

- `perf_test` performs transactions on `eligibility` and `profile` data and inserts the results in the `summary` map, which we'll replicate over the WAN.


### ny

To enable WAN gateways, we must first assign a unique number defined by the GemFire property, `distributed-system-id` to each cluster. This number must in the the range of 1 to 255, inclusive. The GemFire properties are set in the `etc/gemfire.properties` file.

```console
switch_cluster ny
vi etc/gemfire.properties
```

In addition to `distributed-system-id`, we also need to set the `remote-locators` property to the remote locators that this cluster will connect to. For our example, we set it to the `ln` locator.

```properties
# ny ID
distributed-system-id=1
# ln locator (for multiple locators, separate them with comma)
remote-locators=localhost[10344]
```

We also need to configure the WAN gateway. This is done in the `etc/cache.xml` file. Let's configure the `ny` cluster as the sender as by adding the `<gateway-sender>` element just below the `<cache>` element and adding the `<region-attributes>` element as shown below. It is important that the order of XML elements defined in the `cache.xml` must be kept in the order they are defined in the schema file.

```xml
<cache ...>

	<!-- Gateway sender -->
	<gateway-sender id="ny-to-ln" remote-distributed-system-id="2" parallel="true"
		dispatcher-threads="2" maximum-queue-memory="150" order-policy="partition">
	</gateway-sender>
    
    ...
    
    <region name="summary" refid="PARTITION">
		<region-attributes gateway-sender-ids="ln-to-ny"></region-attributes>
	</region>
    
    ...
    
</cache>
```

### ln

As with the `ny` cluster, the `ln` cluster also requires the unique number set to the `distributed-system-id` property.	

```console
switch_cluster ny
vi etc/gemfire.properties
```

Set `distributed-system-id` to 2. Unlike `ny`, we don't need to set the `remote-locators` for `ln` since it will not be sending but receiving data only.

```properties
# ny ID
distributed-system-id=2
```

Let's now configure the WAN gateway for `ln`.

```console
switch_cluster ln
vi etc/cache.xml
```

We only need to define it as the receiver as shown below. The `start-port` and `end-port` attributes are optional attributes that define the range of TCP ports that the receiver will listen on.

```xml
<cache ...>

	<!-- Gateway receiver -->
	<gateway-receiver start-port="5010" end-port="5020">
	</gateway-receiver>
    
    ...
    
</cache>
```

## 4. Start Clusters

Let's start both clusters.

```console
start_cluster -cluster ny
start_cluster -cluster ln
```

## 5. Monitor Clusters

The clusters can be monitored in a number of ways, i.e., by Pulse, gfsh, VSD, JMX, Grafana, geode-addon, etc. Let's use Pulse for our example. You can view the gateway sender activities from Pulse. To get the Pulse URLs for both clusters run the following:

```console
show_cluster -long -cluster -ny
show_cluster -long -cluster ln
```

You should see the following URLs from the command outputs.

- ny: [http://localhost:7070/pulse](http://localhost:7070/pulse)
- ln: [http://localhost:7080/pulse](http://localhost:7080/pulse)

## 6. Ingest Data

Let's create and change directory to the `perf_test` app's `bin_sh` directory.

```console
create_app
cd_app perf_test; cd bin_sh
```

The `perf_test` app has already been properly configured to ingest data into the `ny` cluster. By default, it connects to the locator at `localhost[10334]`, which is the `ny` locator in our example.

We first need to ingest data into the `eligibility` and `profile` regions before we can perform transactions. Run the following to populate these regions in the `ny` cluster.

```console
./test_ingestion -run
```

We are now ready to generate transactional data into the `summary` region which we have configured to replicate over the WAN. The following command aggregates the `eligibiliy` and `profile` regions by the `groupNumber` field and inserts the `GroupSummary` objects that contain the aggregation information.

```console
./test_tx -run
```

From Pulse, you should now see the `summary` region populated in both clusters. Note that the `eligibility` and `profile` regions are only populated in the `ny` cluster. This is because we have not configured them with the WAN gateway.

## 7. Tear Down

```console
stop_cluster -all -cluster ny
stop_cluster -all -cluster ln
```

## 8. Summary

Replicating data over the WAN between Geode/GemFire clusters require the following configurations.

- Locators (`gemfire.properties`): The `distributed-system-id` and `remote-locators=localhost` properties.
- Members (`cache.xml`): The `gateway-sender` and `gateway-receiver` elements. The `gateway-sender-ids` region attribute for each region.

`geode-addon` greatly simplifies the configuration steps and running clusters.

