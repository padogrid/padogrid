# Reactivating Workspaces on AWS EC2

This article describes how to reactivate VM workspaces that have been detached from AWS EC2 VMs. 

## Scenario

You have created a VM workspace with EC2 instances. After you were done running Geode/GemFire cluster(s) on the EC2 instances from the VM workspace, you terminated the EC2 instances without removing the workspace. 

A few days later, you return to your work and are in need of reinstating the same workspace environment with new EC2 instances.

## VM Workspace Activation Steps

VM workspace activation steps involve updating two (2) workspace and cluster configuration files with the public IP addresses of new EC2 instances.

- Workspace: `setenv.sh`
- Cluster: `cluster.properties`

Let's walk through the workspace reactivation steps using the same example workspace described in the following article:

[Workspaces on AWS EC2 Instances](Workspaces-on-AWS-EC2-Instances.md)

### 1. Launch EC2 Instances

First, launch the desired number of EC2 instances from the EC2 Dashboard. For our example, we launched six (6) instances as shown below.


| Name       | IP Address    | Availability Zone |
| ---------- | ------------- | ----------------- |
| locator1   | 3.15.231.153  | us-east-2a        |
| member1    | 3.136.19.139  | us-east-2a        |
| member2    | 3.137.41.211  | us-east-2a        |
| locator2   | 3.16.188.49   | us-east-2b        |
| member3    | 18.225.36.155 | us-east-2b        |
| member4    | 3.17.142.94   | us-east-2b        |


### 2. Update Workspace `setenv.sh`

Gather the public IP addresses of the EC2 instances and list them in the workspace `setenv.sh` file.

```console
switch_workspace ws-aws-gemfire
vi setenv.sh
```

Set the `VM_HOSTS` property with the public IP addresses and make sure `VM_USER` is set to the correct user name.

```bash
VM_HOSTS="3.15.231.153,3.136.19.139,3.137.41.211,3.16.188.49,18.225.36.155,3.17.142.94"
VM_USER="ec2-user"
```

### 3. Update `etc/cluster.properties`

Update the `etc/cluster.properties` file with the new EC2 IP addresses. For our example, the VM cluster name is `mygemfire`.

```console
switch_cluster mygemfire
vi etc/cluster.properties
```

Set the following VM properties. 

```bash
# Set locator and member host lists
vm.locator.hosts=3.15.231.153,3.16.188.49
vm.hosts=3.136.19.139,3.137.41.211,18.225.36.155,3.17.142.94
vm.user=ec2-user

# Set hostnameForClients and redundancyZone for each IP address
# us-east-2a
vm.3.15.231.153.hostnameForClients=3.15.231.153
vm.3.15.231.153.redundancyZone=us-east-2a
vm.3.136.19.139.hostnameForClients=3.136.19.139
vm.3.136.19.139.redundancyZone=us-east-2a
vm.3.137.41.211.hostnameForClients=3.137.41.211
vm.3.137.41.211.redundancyZone=us-east-2a

# us-east-2b
vm.3.16.188.49.hostnameForClients=3.16.188.49
vm.3.16.188.49.redundancyZone=us-east-2b
vm.18.225.36.155.hostnameForClients=18.225.36.155
vm.18.225.36.155.redundancyZone=us-east-2b
vm.3.17.142.94.hostnameForClients=3.17.142.94
vm.3.17.142.94.redundancyZone=us-east-2b
```

It is important to set `hostnameForClients` for each host if you want to connect to the cluster from outside the EC2 environment. This is because the Geode/GemFire members are bound to the EC2 internal IP addresses. Make sure to follow the `vm.<host>.*` pattern for the property names.

:exclamation: If you have reinstated the workspace with an EC2 instance type that is different from the previous instance type then you may also need to change resource properties such as `heap.min` and `heap.max`.

### 4. Sync VMs

Run `vm_sync` to synchronize the workspace.

```console
vm_sync
```

The above command reports the following:

```console
Deploying padogrid_0.9.1 to 3.15.231.153...
Deploying padogrid_0.9.1 to 3.136.19.139...
Deploying padogrid_0.9.1 to 3.137.41.211...
Deploying padogrid_0.9.1 to 3.16.188.49...
Deploying padogrid_0.9.1 to 18.225.36.155...
Deploying padogrid_0.9.1 to 3.17.142.94...

Workspace sync: ws-aws-gemfire
   Synchronizing 3.15.231.153...
   Synchronizing 3.136.19.139...
   Synchronizing 3.137.41.211...
   Synchronizing 3.16.188.49...
   Synchronizing 18.225.36.155...
   Synchronizing 3.17.142.94...
------------------------------------------------------------------------------------------
WARNING:
/home/ec2-user/padogrid/products/jdk1.8.0_212
   JDK not installed on the following VMs. The workspace will not be operational
   until you install JDK on these VMs.
       3.15.231.153 3.136.19.139 3.137.41.211 3.16.188.49 18.225.36.155 3.17.142.94
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
       3.15.231.153 3.136.19.139 3.137.41.211 3.16.188.49 18.225.36.155 3.17.142.94
VM Geode Path:
    /home/ec2-user/padogrid/products/pivotal-gemfire-9.9.1
To install Geode on the above VMs, download the correct version of JDK and execute 'vm_install'.
Example:
   vm_install -geode pivotal-gemfire-9.9.1.tar.gz
------------------------------------------------------------------------------------------
Workspace sync complete.
```

### 5. Install Software

`vm_sync` will display warning messages similar to the output shown above since the new EC2 instances do not have the required software installed. Download the required software and install them by running the `vm_install` command as shown below.

```console
 vm_install -java ~/Downloads/jdk-8u212-linux-x64.tar.gz \
            -geode ~/Downloads/pivotal-gemfire-9.9.1.tgz
```

### 6. Start Cluster

Start the cluster.

```console
start_cluster
```

### 7. Run Apps

For our example, we have the `perf_test` app already created. Edit the `client-cache.xml` file as before.

```console
create_app
cd_app perf_test
vi etc/client-cache.xml
```

Replace the old locator IP addresses with the new locator IP addresses.

```xml
<pool name="serverPool">
   <locator host="3.15.231.153" port="10334" />
   <locator host="3.16.188.49" port="10334" />
</pool>
````

Ingest data into the cluster.

```console
cd bin_sh
./test_ingestion -run
```

## Summary

Reactivating a VM workspace with new EC2 instances requires a simple step of updating the workspace and cluster configuration files with the new instance IP addresses.
