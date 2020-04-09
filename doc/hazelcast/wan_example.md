# WAN Example: Vagrant Pods

This article describes a complete set of steps for creating a WAN environment. We'll create two (2) Hazelcast clusters named **`ny`** and **`ln`** running on two (2) separate subnets (pods). We'll then configure the `ny` cluster to replicate transactional data to the `ln` cluster, run the `perf_test` app to generate transactional data, and run the desktop to view the data in both clusters.

The steps we'll take are typical steps that you would take when preparing `hazelcast-addon` for your local environment.

1. [Install hazecast-addon](https://github.com/hazelcast/hazelcast-addon#managing-hazelcast-clusters)
2. [Create Network Adapters](#Create-Network-Adapters)
3. [Create Pods](#Create-Pods)
4. [Create Clusters](#Create-Clusters)
5. [Update Configuration Files](#Update-Configuration-Files)
6. [Start Clusters](#Start-Clusters)
7. [Start Management Centers](#Start-Management-Centers)
8. [Run Apps](#Run-Apps)

## Create Network Adapters

Using the VirtualBox Manager, create two (2) host-only network adapters. For our example, let's create two (2) adatpers with the IP addresses `192.168.56.1` and `192.168.57.1`. For instructions, click on your host OS link below.

- [Linux/MacOS](/hazelcast-addon-deployment/src/main/resources/pods#linuxmacos)
- [Windows](/hazelcast-addon-deployment/src/main/resources/pods#windows)

## Create Pods

With the adapters in place, we can now create pods. Let's create two (2) pods named `ny` and `ln` using the host-only adapters created in the previous section. Assign the node names and IP addresses as shown below.

```console
create_pod -pod ny

                       Pod name: ny
              Primary node name: ny-pnode
          Data node name prefix: ny-node
        Host private IP address: 192.168.56.1
      Node IP addres last octet: 10
 Primary node memory size (MiB): 2048
    Data node memory size (MiB): 2048
                Data node count: 2
             Products directory: /Users/dpark/Hazelcast/workspaces/ws-3.12/products
              Vagrant box image: ubuntu/trusty64
```

```console
create_pod -pod ln

                       Pod name: ln
              Primary node name: ln-pnode
          Data node name prefix: ln-node
        Host private IP address: 192.168.57.1
      Node IP addres last octet: 10
 Primary node memory size (MiB): 2048
    Data node memory size (MiB): 2048
                Data node count: 2
             Products directory: /Users/dpark/Hazelcast/workspaces/ws-3.12/products
              Vagrant box image: ubuntu/trusty64
```

### Build Pods

Once the pods are created, your next step is to build them. The build process involves downloading, configuring and running the Vagrant box image. It will take a bit of time to complete. Execute the following to build both pods.

```console
# Build the ny pod
build_pod -pod ny

# Build the ln pod
build_pod -pod ln
```

### Test Networks

Check the networks by issuing the `ping` command as shown below. Note that all node host names end with the suffix `.local`.

```console
ping ny-pnode.local
ping ny-node-01.local
ping ny-node-01.local
ping ln-pnode.local
ping ln-node-01.local
ping ln-node-01.local
```

If the ping command fails then follow the instructions in the following link:

[Multicast DNS (mDNS)](/hazelcast-addon-deployment/src/main/resources/pods#multicast-dns-mdns)

## Create Clusters

You could have created clusters before the pods were built. The order of building pods and creating clusters is not important but to create clusters, you must first create the pods.

Create clusters to run on the pods. Let's name them the same names as the pods.

```console
# Create the ny cluster
create_cluster -cluster ny -pod ny

# Create the ln cluster
create_cluster -cluster ln -pod ln
```

## Update Configuration Files

Let's now configure the `hazelcast.xml` files to include the `perf_test` maps so that we can replicate them over the WAN. 

- `perf_test` performs transaction on `eligibility` and `profile` data and inserts the results in the `summary` map, which we'll replcate over the WAN.

### WAN Publisher - ny

**File:** `$PADOGRID_WORKSPACE/clusters/ny/etc/hazelcast.xml`

Add the following in the above `ny` cluster's configuration file. Note that the `endpoints` is set to the IP address (192.168.57.11) of the first data node of the `ln` cluster.

```xml
    <wan-replication name="ny-to-ln">
        <wan-publisher group-name="dev">
            <class-name>com.hazelcast.enterprise.wan.replication.WanBatchReplication</class-name>
            <properties>
                <property name="endpoints">192.168.57.11:5701</property>
                <property name="ack.type">ACK_ON_OPERATION_COMPLETE</property>
            </properties>
        </wan-publisher>
    </wan-replication>
    
    <map name="transactions">
    	<wan-replication-ref name="ny-to-ln">
    		<merge-policy>com.hazelcast.map.merge.PassThroughMergePolicy</merge-policy>
    	</wan-replication-ref>
    </map>
    <map name="summary">
    	<wan-replication-ref name="ny-to-ln">
    		<merge-policy>com.hazelcast.map.merge.PassThroughMergePolicy</merge-policy>
    	</wan-replication-ref>
    </map>
```

### WAN Consumer - ln

**File:** `$PADOGRID_WORKSPACE/clusters/ln/etc/hazelcast.xml`

Add the following in the above `ln` cluster's configuration file.

```xml
    <wan-replication name="ny-to-ln">
        <wan-consumer>
        </wan-consumer>
    </wan-replication>
	
    <map name="transactions">
        <wan-replication-ref name="ny-to-ln">
        <merge-policy>com.hazelcast.map.merge.PassThroughMergePolicy</merge-policy>
        </wan-replication-ref>
    </map>
    <map name="summary">
        <wan-replication-ref name="ny-to-ln">
            <merge-policy>com.hazelcast.map.merge.PassThroughMergePolicy</merge-policy>
        </wan-replication-ref>
    </map>
```


## Start Clusters

Now that the clusters have been configured, you are ready to start the clusters. Login to each pod and start their respective cluster as follows:

**ny Cluster**

```console
# Login to the 'ny' pod's primary node 
ssh vagrant@ny-pnode.local
password: vagrant

# Start 'ny' cluster
start_cluster -cluster ny

# View the tail end of the first node's log
show_log -cluster ny
```

**ln Cluster**

```console
# Login to the 'ln' pod's primary node 
ssh vagrant@ln-pnode.local
password: vagrant

# Start 'ln' cluster
start_cluster -cluster ln

# View the tail end of the first node's log
show_log -cluster ln
```

## Start Management Centers

Start the management centers for both `ny` and `ln` clusters. Login to the primary node of each pod and run `start_mc` as shown below. 

**Note:** If you wish to start the management centers on nodes other than the primary nodes then you must change the `mc.host` property in the `$PADOGRID_WORKSPACE/clusters/<cluster-name>/etc/cluster.properties` file.

**ny Cluster**

- **MC URL:** [http://ny-pnode.local:8080/hazelcast-mancenter](http://ny-pnode.local:8080/hazelcast-mancenter)

```console
ssh vagrant@ny-pnode.local
password: vagrant

# Start 'ny' management center
start_mc -cluster ny
```

**ln Cluster**

- **MC URL:** [http://ln-pnode.local:8080/hazelcast-mancenter](http://ln-pnode.local:8080/hazelcast-mancenter)

```console
ssh vagrant@ln-pnode.local
password: vagrant

# Start 'ln' management center
start_mc -cluster ln
```

## Run Apps

At last, we are now ready to configure and run the apps and see WAN replication in action. We'll use three (2) apps as follows:

- perf_test
- desktop

You must install them first if you haven't done so as follows.

```console
create_app -app perf_test
create_app -app destkop

# For the desktop app, you will need to build it as follows:

# Build desktop
cd_app desktop; cd bin_sh; ./build_app
```

### Configure Apps

We need to configure the `perf_test` app to connect to the `ny` cluster. These apps will write to the `ny` cluster which in turn will replicate the transactional data to the `ln` cluster.

**hazecastl-client Files:** 
- `$PADOGRID_WORKSPACE/apps/perf_test/etc/hazelcast-client.xml`

In both files listed above, replace the \<network\> tag with the following:

```xml
	<network>
		<cluster-members>
			<address>ny-node-01.local:5701</address>
			<address>ny-node-02.local:5701</address>
		</cluster-members>
	</network>
```

### perf_test

Run `test_ingestion` to ingest `eligibility` and `profile` data, and `test_tx` to create transaction reports in the `summary` map.

```console
cd_app perf_test; cd bin_sh
./test_ingestion -run
./test_tx -run
```

### Desktop

You can view the data in both clusters using the desktop app. Launch two (2) desktop instances as shown below:

```console
cd_app desktop; cd bin_sh
./desktop
./dekstop
```

In the Login window, enter the following:

**ny Cluster**

```console
Locators: ny-node-01.local:5701
App ID: sys
User Name: <your user name>
Password: <blank>
```

**ln Cluster**

```console
Locators: ln-node-01.local:5701
App ID: sys
User Name: <your user name>
Password: <blank>
```

### Prometheus/Grafana

We can monitor the cluster activities by running Prometheus and Grafana. Let's run them on the `ny` cluster.

We need to first edit the Prometheus configuration file to target the `ny` data nodes. Replace the localhost entries in the following file with the data node IP addresses as shown below. Do not enter the data node host names. The IP addresses are required due to a Prometheus address binding issue.

**File**: `$PADOGRID_HOME/apps/grafana/etc/prom-hazelcast.yml`

```yaml
      - targets: [192.168.56.11:8091, 192.168.56.12:8091]
```

You can login to the `ny` primary node to run both Prometheus and Grafana as shown below.

```console
# Login to ny pnode 
ssh vagrant@ny-pnode.local

# Run Prometheus
prometheus --config.file=$PADOGRID_HOME/apps/grafana/etc/prom-hazelcast.yml --log.level=debug

# Run Grafana
grafana-server -homepath $GRAFANA_HOME
```

From the host OS, open the web browser and enter the following URL:

[http://ny-pnode.local:3000](http://ny-pnode.local:3000)

### Dashboards

`hazelcast-addon` includes Grafana dashboards for monitoring CPU and memory usages, GC activities, perf_test cluster operations, and etc. Import the dashboards from the `ny` primary node as shown below.

```console
# Login to ny pnode 
ssh vagrant@ny-pnode.local

cd $PADOGRID_HOME/apps/grafana/bin_sh
./import_folder -all
```

From your web browser, look for the following folders:

- hazelcast-addon-perf_test

Each folder contains a set of dashboards.

[Go To Grafana](/hazelcast-addon-deployment/src/main/resources/apps/grafana)

