# Workspaces on AWS EC2 Instances

This article provides step-by-step instructions for creating and running a VM workspace on AWS EC2 instances.

## Launch EC2 Instances

From the EC2 Dashboard launch six (6) EC2 instances of type `t2.micro` on two (2) different availability zones and collect their public IP addresses. For our example, we launched three (3) instances on each of the `us-east-2a` and `us-east-2b` availability zones, named the instances, and collected their public IP addresses as follows:

| Name       | IP Address     | Availability Zone |
| ---------- | -------------- | ----------------- |
| locator1   | 3.135.221.186  | us-east-2a        |
| member1    | 18.191.168.36  | us-east-2a        |
| member2    | 3.135.186.150  | us-east-2a        |
| locator2   | 3.135.232.83   | us-east-2b        |
| member3    | 18.218.40.90   | us-east-2b        |
| member4    | 18.217.250.90  | us-east-2b        |

## Create VM Workspace

To create a workspace, run `create_workspace`, which by default runs in the interactive mode. For our example, let's run it in the non-interactive mode by specifying the `-quiet` option as follows: 

```console
create_workspace -quiet \
-name ws-aws-gemfire \
-cluster mygemfire \
-java /home/dpark/padogrid/products/jdk1.8.0_212 \
-geode /home/dpark/padogrid/products/pivotal-gemfire-9.9.1 \
-vm 3.135.221.186,18.191.168.36,3.135.186.150,3.135.232.83,18.218.40.90,18.217.250.90 \
-vm-java /home/ec2-user/padogrid/products/jdk1.8.0_212 \
-vm-geode /home/ec2-user/padogrid/products/pivotal-gemfire-9.9.1 \
-vm-addon /home/ec2-user/padogrid/products/geode-addon_0.9.0-SNAPSHOT \
-vm-workspaces /home/ec2-user/padogrid/workspaces/myrwe \
-vm-user ec2-user \
-vm-key /home/dpark/padogrid/dpark.pem
```

The above creates the workspace named `ws-aws-gemfire` and places all the installations in the `/home/ec2-user/padogrid/products` directory. When you are done with installation later, each EC2 instance will have the following folder contents:

```console
/home/ec2-user/padogrid/products
├── pivotal-gemfire-9.9.1
├── geode-addon_0.9.0-SNAPSHOT
├── jdk1.8.0_212
└── workspaces
    ├── initenv.sh
    ├── setenv.sh
    └── ws-aws-gemfire
        └── clusters
            └── mygemfire
```

The non-vm options, `-java` and `-geode` must be set to Java and Geode home paths in your local file system.

The following table shows the breakdown of the options.

| Option         | Value                                                    | Description                     |
|--------------- | -------------------------------------------------------- | ------------------------------- |
| -name          | ws-aws-gemfire                                           | Workspace name                  |
| -cluster       | mygeode                                                  | Cluster name                    |
| -java          | /home/dpark/padogrid/products/jdk1.8.0_212               | JAVA_HOME, local file system    |
| -geode         | /home/dpark/padogrid/products/pivotal-gemfire-9.9.1      | GEODE_HOME, local file system   |
| -vm            | 3.135.221.186,18.191.168.36,3.135.186.150,3.135.232.83,18.218.40.90,18.217.250.90 | EC2 instance public IP addresses. Must be separated by comma with no spaces |
| -vm-java       | /home/ec2-user/padogrid/products/jdk1.8.0_212            | JAVA_HOME, EC2 instances        |
| -vm-geode      | /home/ec2-user/padogrid/products/pivotal-gemfire-9.9.1   | GEODE_HOME, EC2 instances       |
| -vm-addon      | /home/ec2-user/padogrid/products/geode-addon_0.9.0-SNAPSHOT | GEODE_ADDON_HOME, EC2 instances |
| -vm-workspaces | /home/ecs2-user/padogrid/workspaces/myrwe                | GEODE_ADDON_WORKSPACES_HOME, EC2 instances |
| -vm-user       | ec2-user                                                 | User name, EC2 instances        |
| -vm-key        | /home/dpark/padogrid/dpark.pem                           | Private key file, local file system |


## Configure Cluster

We launched `t2.micro` instances which only have 1 GB of memory. We need to lower the GemFire member heap size to below this value. Edit the `cluster.properties` file as follows:

```console
switch_workspace ws-aws-gemfire
switch_cluster mygemfire
vi etc/cluster.properties
```

Change the heap and host properties in that file as follows:

```bash
# Lower the heap size from 1g to 512m
heap.min=512m
heap.max=512m
```

We have launched two (2) EC2 instances for locators. Let's set `vm.locator.hosts` to their IP addresses.

```bash
# Set the first VM as the locator
vm.locator.hosts=3.135.221.186,3.135.232.83
```

Make sure to remove the locator IP addresses from the member host list set by the `vm.hosts` property.

```bash
# Rest of the 4 VMs for members
vm.hosts=18.191.168.36,3.135.186.150,18.218.40.90,18.217.250.90
```

Lastly, set the redundancy zone for each of the VM. We can use any name as long as they are unique per zone. For our example, let's use the same availability zone names.

```bash
vm.3.135.221.186.redundancyZone=us-east-2a
vm.18.191.168.36.redundancyZone=us-east-2a
vm.3.135.186.150.redundancyZone=us-east-2a
vm.3.135.232.83.redundancyZone=us-east-2b
vm.18.218.40.90.redundancyZone=us-east-2b
vm.18.217.250.90.redundancyZone=us-east-2b
```

Note that we didn't need to set the redundancy zone for locators since they are not data nodes.

## Sync VMs

Run `vm_sync` to synchronize the workspace.

```console
vm_sync
```

The above command reports the following:

```console
Deploying geode-addon_0.9.0-SNAPSHOT to 3.135.221.186...
Deploying geode-addon_0.9.0-SNAPSHOT to 18.191.168.36...
Deploying geode-addon_0.9.0-SNAPSHOT to 3.135.186.150...
Deploying geode-addon_0.9.0-SNAPSHOT to 3.135.232.83...
Deploying geode-addon_0.9.0-SNAPSHOT to 18.218.40.90...
Deploying geode-addon_0.9.0-SNAPSHOT to 18.217.250.90...

Workspace sync: ws-aws-gemfire
   Synchronizing 3.135.221.186...
   Synchronizing 18.191.168.36...
   Synchronizing 3.135.186.150...
   Synchronizing 3.135.232.83...
   Synchronizing 18.218.40.90...
   Synchronizing 18.217.250.90...
------------------------------------------------------------------------------------------
WARNING:
/home/ec2-user/padogrid/products/jdk1.8.0_212
   JDK not installed on the following VMs. The workspace will not be operational
   until you install JDK on these VMs.
       3.135.221.186 18.191.168.36 3.135.186.150 3.135.232.83 18.218.40.90 18.217.250.90
VM Java Home Path:
      /home/ec2-user/padogrid/products/jdk1.8.0_212
To install Java on the above VMs, download the correct version of JDK and execute 'vm_install'.
Example:
   vm_install -java jdk1.8.0_212.tar.gz
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
WARNING:
   Geode is not installed on the following VMs. The workspace will not be operational
   until you install Geode on these VMs.
       3.135.221.186 18.191.168.36 3.135.186.150 3.135.232.83 18.218.40.90 18.217.250.90
VM Geode Path:
    /home/ec2-user/padogrid/products/pivotal-gemfire-9.9.1
To install Geode on the above VMs, download the correct version of JDK and execute 'vm_install'.
Example:
   vm_install -geode pivotal-gemfire-9.9.1.tar.gz
------------------------------------------------------------------------------------------
```

## Install Software

`vm_sync` will display warning messages similar to the output shown above since the new EC2 instances do not have the required software installed. Download the required software and install them by running the `vm_install` command as shown below.

```console
vm_install -java ~/Downloads/jdk-8u212-linux-x64.tar.gz \
           -geode ~/Downloads/pivotal-gemfire-9.9.1.tgz
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

To view logs

```console
# member1
show_log

# member2
show_log -num 2

# locator1
show_log -log locator

# locator2
show_log -log locator -num 2
```

## Pulse

You can get the Pulse URL by running the `show_cluster -long` command. For our example, both locators serve Pulse.

- http://3.135.221.186:7070/pulse
- http://3.135.232.83:7070/pulse

You can view the members running in each redundancy zone from Pulse.

![Pulse Screenshot](/images/pulse-redundancy-zone.png)

## Test Cluster

You can run the `perf_test` app to ingest data into the cluster and monitor the region sizes increase from Pulse.

First, create the `perf_test` app and edit its configuration file to point to the locator running on EC2.

```console
create_app
cd_app perf_test
vi etc/client-cache.xml
```

Set the locator hosts in the `etc/client-cache.xml` with the locator IP addresses.

```xml
<pool name="serverPool">
   <locator host="3.135.221.186" port="10334" />
   <locator host="3.135.232.83" port="10334" />
</pool>
```

Ingest data into the cluster.

```console
cd bin_sh
./test_ingestion -run
```

### Including Additional EC2 Instances

You can include additional EC2 instances to the workspace by entering the new instance IP addresses in the workspace `setenv.sh` file. Let's launch a new EC2 instance for running client apps. We ran the `perf_test` app locally from your laptop in the previous section. We'll use this new EC2 instance to run the same app from the new EC2 instance. For our example, the new EC2 instance has the following IP address.

| Name       | IP Address     | 
| ---------- | -------------- |
| client     | 18.219.75.145  |

Let's add the IP address to the workspace `setenv.sh` file.

```console
cd_workspace
vi setenv.sh
```

Append the new IP address to the VM_HOSTS environment variable as follows:

```bash
VM_HOSTS="3.135.221.186,18.191.168.36,3.135.186.150,3.135.232.83,18.218.40.90,18.217.250.90,18.219.75.145"
```

After saving the `setenv.sh` file, run `vm_sync` to synchronize the VMs so that the new instance will have the `geode-addon` installed.

```console
vm_sync
```

The `vm_sync` command output should be similar to what we have seen before.

```console
Deploying geode-addon_0.9.0-SNAPSHOT to 18.219.75.145...

Workspace sync: ws-aws-gemfire
   Synchronizing 3.135.221.186...
   Synchronizing 18.191.168.36...
   Synchronizing 3.135.186.150...
   Synchronizing 3.135.232.83...
   Synchronizing 18.218.40.90...
   Synchronizing 18.217.250.90...
   Synchronizing 18.219.75.145...
------------------------------------------------------------------------------------------
WARNING:
/home/ec2-user/padogrid/products/jdk1.8.0_212
   JDK not installed on the following VMs. The workspace will not be operational
   until you install JDK on these VMs.
       18.219.75.145
VM Java Home Path:
      /home/ec2-user/padogrid/products/jdk1.8.0_212
To install Java on the above VMs, download the correct version of JDK and execute 'vm_install'.
Example:
   vm_install -java jdk1.8.0_212.tar.gz
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
WARNING:
   Geode is not installed on the following VMs. The workspace will not be operational
   until you install Geode on these VMs.
       18.219.75.145
VM Geode Path:
    /home/ec2-user/padogrid/products/pivotal-gemfire-9.9.1
To install Geode on the above VMs, download the correct version of JDK and execute 'vm_install'.
Example:
   vm_install -geode pivotal-gemfire-9.9.1.tar.gz
------------------------------------------------------------------------------------------
Workspace sync complete.
```

Let's install Java and GemFire as before.

```console
 vm_install -java ~/Downloads/jdk-8u212-linux-x64.tar.gz \
            -geode ~/Downloads/pivotal-gemfire-9.9.1.tgz
```

### Running Apps from EC2 Instances

In the previous section, we added a new EC2 instance for running client apps. 

Login to the client instance.

```console
# First change directory to the workspace where the key file (dpark.pem) is located.
cd_workspace

# ssh into the client VM
ssh -i dpark.pem ec2-user@18.219.75.145
```

We need to initialize `geode-addon` before we can run apps. Let's do this in `.bashrc` so that it is done automatically when we login next time.

```console
echo ". /home/ec2-user/padogrid/workspaces/myrwe/initenv.sh -quiet" >> ~/.bashrc
. ~/.bashrc
```

When we ran `vm_sync` earlier, it also deployed the `perf_test` app to all the VMs. We can simply change directory to `perf_test` and run `test_ingestion` as before.

```console
cd_app perf_test; cd bin_sh
./test_ingestion -run
```

## Preserving Workspace

If you terminate the EC2 instances without removing the workspace, then your workspace will be preserved on your local machine. This means you can later reactivate the workspace by simply launching new EC2 instances and configuring the workspace with the new public IP addresses. The following link provides step-by-step instructions describing how to reactivate VM workspaces.

[Reactivating Workspaces on AWS EC2](Reactivating-Workspaces-on-AWS-EC2.md)

## Tear Down

1. Stop the cluster

If you want to remove the cluster from all the VMs, then you must first stop the cluster and execute the `remove_cluster` command.

```console
# Stop cluster including members and locator
stop_cluster -all
```

2. Remove the workspace

If you want to preserve the workspace so that you can later reactivate it then skip this step and jump to the next step; otherwise, run the `remove_workspace` command which will also remove the cluster.

```console
# Simulate removing workspace from all VMs. Displays removal steps but does not
# actually remove the workspace.
remove_workspace -workspace ws-aws-gemfire -simulate

# Remove workspace from all VMs. Runs in interactive mode.
remove_workspace -workspace ws-aws-gemfire
```

3. Terminate the EC2 instances

From the EC2 Dashboard remove the EC2 instances.
