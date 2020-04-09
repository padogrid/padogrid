# Workspaces on AWS EC2 Instances

This article provides step-by-step instructions for creating and running a PadoGrid workspace on AWS EC2 instances.

## Launch EC2 Instances

From the EC2 Dashboard launch five (5) EC2 instances of type `t2.micro` on three (3) different availability zones and collect their public IP addresses. For our example, we launched two (2) instances on each of the `us-east-2a` and `us-east-2b` availability zones, one (1) instance on `us-east-2c` for the management center, named the instances, and collected their public IP addresses as follows:

| Name       | IP Address     | Availability Zone | Tag                  |
| ---------- | -------------- | ----------------- | -------------------- |
| mancenter  | 3.19.14.241    | us-east-2c        | padogrid/aws-cluster |   
| member1    | 3.135.241.115  | us-east-2a        | padogrid/aws-cluster |
| member2    | 52.14.100.181  | us-east-2a        | padogrid/aws-cluster |
| member3    | 3.135.226.150  | us-east-2b        | padogrid/aws-cluster |
| member4    | 18.216.40.10   | us-east-2b        | padogrid/aws-cluster |

We also created the key-value tag of `padogrid` and `aws-cluster` which we will use when we configure the EC2 discovery service later.

### Security Group

For our example, the security group for the EC2 instances should have the following rules defined for both inbound and outbound traffics.

Type             | Protocol | Port Range | Source           | Description  |
| -------------- | -------- | ---------- | ---------------- | ------------ |
| HTTP           | TCP      | 80         | Custom 0.0.0.0/0 | HTTP required by AWS discovery service |
| HTTP           | TCP      | 80         | Custom ::/0      | HTTP required by AWS discovery service |
| Custom TCP Rule| TCP      | 8080       | Custom 0.0.0.0/0 | Management Center |
| Custom TCP Rule| TCP      | 8080       | Custom ::/0      | Management Center |
| SSH            | TCP      | 22         | Custom 0.0.0.0/0 | SSH |
| SSH            | TCP      | 22         | Custom ::/0      | SSH |
| HTTPS          | TCP      | 443        | Custom 0.0.0.0/0 | HTTPS required by AWS discovery service |
| HTTPS          | TCP      | 443        | Custom ::/0      | HTTPS required by AWS discovery service |

## Create VM Workspace

To create a workspace, run `create_workspace`, which by default runs in the interactive mode. For our example, let's run it in the non-interactive mode by specifying the `-quiet` option as shown below. (Change the option values to your settings.)

```console
create_workspace -quiet \
-name ws-aws-hazelcast \
-cluster myhz \
-java /home/dpark/padogrid/products/jdk1.8.0_212 \
-hazelcast /home/dpark/padogrid/products/hazelcast-enterprise-3.12.6 \
-vm 3.19.14.241,3.135.241.115,52.14.100.181,3.135.226.150,18.216.40.10 \
-vm-java /home/ec2-user/padogrid/products/jdk1.8.0_212 \
-vm-hazelcast /home/ec2-user/padogrid/products/hazelcast-enterprise-3.12.6 \
-vm-addon /home/ec2-user/padogrid/products/hazelcast-addon_0.9.0-SNAPSHOT \
-vm-workspaces /home/ec2-user/padogrid/workspaces/rwe \
-vm-user ec2-user \
-vm-key /home/dpark/Work/aws/dpark.pem
```

The above creates the workspace named `ws-aws-hazelcast` and places all the installations in the `/home/ec2-user/padogrid/products` directory. When you are done with installation later, each EC2 instance will have the following folder contents:

```console
/home/ec2-user/padogrid/products
├── hazelcast-enterprise-3.12.6
├── hazelcast-addon_0.9.0-SNAPSHOT
├── jdk1.8.0_212
└── workspaces
    ├── initenv.sh
    ├── setenv.sh
    └── ws-aws-hazelcast
        └── clusters
            └── myhz
```

The non-vm options, `-java` and `-hazelcast` must be set to Java and Hazelcast home paths in your local file system.

The following table shows the breakdown of the options.

| Option         | Value                                                   | Description                     |
|--------------- | ------------------------------------------------------- | ------------------------------- |
| -name          | ws-aws-hazelcast                                        | Workspace name                  |
| -cluster       | myhz                                                    | Cluster name                    |
| -java          | /home/dpark/padogrid/products/jdk1.8.0_212              | JAVA_HOME, local file system    |
| -hazelcast     | /home/dpark/padogrid/products/hazelcast-enterprise-3.12.6 | HAZELCAST_HOME, local file system   |
| -vm            | 3.18.113.154,3.21.44.203,18.218.42.247,18.222.225.117,18.188.216.237 | EC2 instance public IP addresses. Must be separated by command with no spaces |
| -vm-java       | /home/ec2-user/padogrid/products/jdk1.8.0_212                   | JAVA_HOME, EC2 instances        |
| -vm-hazelcast  | /home/ec2-user/padogrid/products/hazelcast-enterprise-3.12.     | HAZELCAST_HOME, EC2 instances       |
| -vm-addon      | /home/ec2-user/padogrid/products/hazelcast-addon_0.9.0-SNAPSHOT | HAZELCAST_ADDON_HOME, EC2 instances |
| -vm-workspaces | /home/ecs2-user/padogrid/products/workspaces                    | HAZELCAST_ADDON_WORKSPACES_HOME, EC2 instances |
| -vm-user       | ec2-user                                                | User name, EC2 instances        |
| -vm-key        | /home/dpark/Work/aws/dpark.pem                          | Private key file, local file system |


## Configure Cluster

### cluster.properties

We launched `t2.micro` instances which only have 1 GB of memory. We need to lower the Hazelcast member heap size to below this value. Edit the `cluster.properties` file as follows:

```console
switch_workspace ws-aws-hazelcast
switch_cluster myhz
vi etc/cluster.properties
```

Change the heap and host properties in that file as follows:

```bash
# Lower the heap size from 1g to 512m
heap.min=512m
heap.max=512m
```

Let's set `mc.host` to the Management Center IP address. Note that the `create_workspace` command created the `myhz` cluster by executing the `create_cluster` command, which by default sets the first VM host you specified using the `-vm` option as the Management Center host.

```bash
# Set the first VM as the management center
mc.host=3.19.14.241 
```

Now, set the `vm.hosts` property to the remaining hosts. Note also that the `create_cluster` command automatically sets this property to the value of the `-vm` option. WE just need to remove the first VM host in the list which we will use only for the Management Center host.

```bash
# Rest of the 4 VMs for members
vm.hosts=3.135.241.115,52.14.100.181,3.135.226.150,18.216.40.10
```

### hazelcast.xml

Hazecast provides an EC2 discovery service which discovers EC2 instances along with their metadata such as availability zone information for configuring zone-aware HA. Configure the discovery service as shown below with your access key and secret access IDs. Also, configure the partition group to `ZONE_AWARE`.

```xml
<hazelcast ...>
    ...
	<network>
        ...
		<join>
			<multicast enabled="false"></multicast>
			<aws enabled="true">
				<access-key>your-access-key</access-key>
				<secret-key>your-secret-key2</secret-key>
				<region>us-east-2</region>
				<security-group-name>hazelcast-cluster</security-group-name>
				<tag-key>padogrid</tag-key>
				<tag-value>aws-cluster</tag-value>
				<hz-port>5701</hz-port>
				<connection-retries>3</connection-retries>
			</aws>
		</join>
        ...
    </network>
    <partition-group enabled="true" group-type="ZONE_AWARE"></partition-group>
    ...
</hazelcast>
```

## Sync VMs

Run `vm_sync` to synchronize the workspace.

```console
vm_sync
```

The above command reports the following:

```console
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 3.19.14.241...
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 3.135.241.115...
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 52.14.100.181...
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 3.135.226.150...
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 18.216.40.10...

Workspace sync: ws-aws-hazelcast
   Synchronizing 3.19.14.241...
   Synchronizing 3.135.241.115...
   Synchronizing 52.14.100.181...
   Synchronizing 3.135.226.150...
   Synchronizing 18.216.40.10...
------------------------------------------------------------------------------------------
WARNING:
/home/ec2-user/padogrid/products/jdk1.8.0_212
   JDK not installed on the following VMs. The workspace will not be operational
   until you install JDK on these VMs.
       3.19.14.241 3.135.241.115 52.14.100.181 3.135.226.150 18.216.40.10
VM Java Home Path:
      /home/ec2-user/padogrid/products/jdk1.8.0_212
To install Java on the above VMs, download the correct version of JDK and execute 'vm_install'.
Example:
   vm_install -java jdk1.8.0_212.tar.gz
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
WARNING:
   Hazelcast is not installed on the following VMs. The workspace will not be operational
   until you install Hazelcast on these VMs.
       3.19.14.241 3.135.241.115 52.14.100.181 3.135.226.150 18.216.40.10
VM Hazelcast Path:
    /home/ec2-user/padogrid/products/hazelcast-enterprise-3.12.6
To install Hazelcast on the above VMs, download the correct version of JDK and execute 'vm_install'.        Example:
   vm_install -hazelcast hazelcast-enterprise-3.12.6.tar.gz
------------------------------------------------------------------------------------------
Workspace sync complete.
```

## Install Software

`vm_sync` will display warning messages similar to the output shown above since the new EC2 instances do not have the required software installed. Download the required software and install them by running the `vm_install` command as shown below.

```console
vm_install -java ~/Downloads/jdk-8u212-linux-x64.tar.gz \
           -hazelcast ~/Downloads/hazelcast-enterprise-3.12.6.tar.gz
```

## Start Cluster

Start the cluster.

```console
start_cluster
```

## Monitor Cluster

To monitor the cluster:

```console
show_cluster
```

## View Log

To view logs:

```console
# member1
show_log

# member2
show_log -num 2
```

## Management Center

We've configured PadoGrid to run the Management Center on one of the VMs.

Start the Management Center.

```console
start_mc
```

The `start_mc` command will display the Management Center URL with the internal host name. To get the URL with the public IP address, run the `show_mc` command.

```console
show_mc
```

 For our example, it displays the following:
 
 ```console
 ----------------------------------------------------------------
        WORKSPACE: /home/ec2-user/padogrid/products/workspaces/ws-aws-hazelcast
             Name: myhz-mc-8080
            STATE: Running
              PID: 4777
          Cluster: myhz
       Deployment: VM
              URL: http://3.19.14.241:8080/hazelcast-mancenter
----------------------------------------------------------------
```

## Test Cluster

You can run the `perf_test` app to ingest data into the cluster and monitor the region sizes increase from the Management Center.

First, create the `perf_test` app and edit its configuration file.

```console
create_app
cd_app perf_test
vi etc/hazelcast-client.xml
```

Add the same EC2 discovery settings in the `client-cache.xml` file as we did for the cluster. If you will be running the client app from outside the EC2 environment then you must also add the `<use-public-ip>` element as shown below.

```xml
<hazelcast-client ...>
...
   <network>
      <aws enabled="true">
         <access-key>your-access-key</access-key>
         <secret-key>your-secret-key2</secret-key>
         <region>us-east-2</region>
         <security-group-name>hazelcast-cluster</security-group-name>
         <tag-key>padogrid</tag-key>
         <tag-value>aws-cluster</tag-value>
         <hz-port>5701</hz-port>
         <connection-retries>3</connection-retries>
         <!-- For running apps outside EC2 -->
         <use-public-ip>true</use-public-ip>
      </aws>
   </network>
...
<hazelcast-client>
```

Ingest data into the cluster.

```console
cd bin_sh
./test_ingestion -run
```

You should see the cluster filling up with data from the Management Center.

### Including Additional EC2 Instances

You can include additional EC2 instances to the workspace by entering the new instance IP addresses in the workspace `setenv.sh` file. Let's launch a new EC2 instance for running client apps. We ran the `perf_test` app locally from your laptop in the previous section. We'll use this new EC2 instance to run the same app from the new EC2 instance. For our example, the new EC2 instance has the following IP address.

| Name       | IP Address     | 
| ---------- | -------------- |
| client     | 3.18.102.53    |

Let's add the IP address to the workspace `setenv.sh` file.

```console
cd_workspace
vi setenv.sh
```

Append the new IP address (`3.18.102.53`) to the VM_HOSTS environment variable as follows:

```bash
VM_HOSTS="3.18.113.154 3.21.44.203 18.218.42.247 18.222.225.117 18.188.216.237,3.18.102.53"
```

After saving the `setenv.sh` file, run `vm_sync` to synchronize the VMs so that the new instance will have PadoGrid installed.

```console
vm_sync
```

The `vm_sync` command output should be similar to what we have seen before.

```console
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 3.18.102.53...

Workspace sync: ws-aws-hazelcast
   Synchronizing 3.19.14.241...
   Synchronizing 3.135.241.115...
   Synchronizing 52.14.100.181...
   Synchronizing 3.135.226.150...
   Synchronizing 18.216.40.10...
   Synchronizing 3.18.102.53...
------------------------------------------------------------------------------------------
WARNING:
/home/ec2-user/padogrid/products/jdk1.8.0_212
   JDK not installed on the following VMs. The workspace will not be operational
   until you install JDK on these VMs.
       3.18.102.53
VM Java Home Path:
      /home/ec2-user/padogrid/products/jdk1.8.0_212
To install Java on the above VMs, download the correct version of JDK and execute 'vm_install'.
Example:
   vm_install -java jdk1.8.0_212.tar.gz
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
WARNING:
   Hazelcast is not installed on the following VMs. The workspace will not be operational
   until you install Hazelcast on these VMs.
       3.18.102.53
VM Hazelcast Path:
    /home/ec2-user/padogrid/products/hazelcast-enterprise-3.12.6
To install Hazelcast on the above VMs, download the correct version of JDK and execute 'vm_install'.        Example:
   vm_install -hazelcast hazelcast-enterprise-3.12.6.tar.gz
------------------------------------------------------------------------------------------
Workspace sync complete.
```

Let's install Java and Hazelcast as before.

```console
vm_install -java ~/Downloads/jdk-8u212-linux-x64.tar.gz \
           -hazelcast ~/Downloads/hazelcast-enterprise-3.12.6.tar.gz
```

### Running Apps from EC2 Instances

Let's login to the new EC2 instance and run the ` perf_test` app from there.

```console
# First change directory to the workspace where the key file (dpark.pem) is located.
cd_workspace

# ssh into the client VM
ssh -i dpark.pem ec2-user@3.18.102.53
```

We need to initialize PadoGrid before we can run apps. Let's do this in `.bashrc` so that it is done automatically when we login next time.

```console
echo ". /home/ec2-user/padogrid/workspaces/rwe/initenv.sh -quiet" >> ~/.bashrc
. ~/.bashrc
```

When we ran `vm_sync` earlier, it also deployed the `perf_test` app to all the VMs. Note that we set the `<use-public-ip>` element was set to `true`. If you have the security group set differently than our example, you may need to change this value to `false` so that the app uses the internal IP instead. 

Edit `etc/client.xml`

```console
cd_app perf_test
vi etc/hazelcast-client.xml
```

Set `<use-public-ip>` to false.

```xml
<hazelcast-client ...>
...
   <network>
      <aws enabled="true">
         ...
         <!-- For running apps inside EC2 -->
         <use-public-ip>false</use-public-ip>
      </aws>
   </network>
...
<hazelcast-client>
```

Run `test_ingestion`.
```
cd bin_sh
./test_ingestion -run
```

## Preserving Workspace

If you terminate the EC2 instances without removing the workspace, then your workspace will be preserved on your local machine. This means you can later reactivate the workspace by simply launching new EC2 instances and configuring the workspace with the new public IP addresses. The following link provides step-by-step instructions describing how to reactivate VM workspaces.

[Reactivating Workspaces on AWS EC2](Reactivating-Workspaces-on-AWS-EC2.md)

## Tear Down

1. Stop the cluster

If you want to remove the cluster from all the VMs, then you must first stop the cluster and execute the `remove_cluster` command.

```console
# Stop cluster and management center
stop_cluster 
stop_mc
```

2. Remove the workspace

If you want to preserve the workspace so that you can later reactivate it then skip this step and jump to the next step; otherwise, run the `remove_workspace` command which will also remove the cluster.

```console
# Simulate removing workspace from all VMs. Displays removal steps but does not
# actually remove the workspace.
remove_workspace -workspace ws-aws-hazelcast -simulate

# Remove workspace from all VMs. Runs in interactive mode.
remove_workspace -workspace ws-aws-hazelcast
```

3. Terminate the EC2 instances

From the EC2 Dashboard terminate the EC2 instances.
