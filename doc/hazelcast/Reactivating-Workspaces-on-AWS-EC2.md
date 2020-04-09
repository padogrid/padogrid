# Reactivating Workspaces on AWS EC2

This article describes how to reactivate VM workspaces that have been detached from AWS EC2 VMs. 

## Scenario

You have created a VM workspace with EC2 instances. After you were done running Hazelcast cluster(s) on the EC2 instances from the VM workspace, you terminated the EC2 instances without removing the workspace. 

A few days later, you return to your work and are in need of reinstating the same workspace environment with new EC2 instances.

## VM Workspace Activation Steps

VM workspace activation steps involve updating two (2) workspace and cluster configuration files with the public IP addresses of new EC2 instances.

- Workspace: `setenv.sh`
- Cluster: `cluster.properties`

Let's walk through the workspace reactivation steps using the same example workspace described in the following article:

[Workspaces on AWS EC2 Instances](Workspaces-on-AWS-EC2-Instances.md)

### 1. Launch EC2 Instances

First, launch the desired number of EC2 instances from the EC2 Dashboard. For our example, we launched five (5) instances as shown below.

| Name       | IP Address     | Availability Zone | Tag                  |
| ---------- | -------------- | ----------------- | -------------------- |
| mancenter  | 3.18.113.154   | us-east-2c        | padogrid/aws-cluster |   
| member1    | 3.21.44.203    | us-east-2a        | padogrid/aws-cluster |
| member2    | 18.218.42.247  | us-east-2a        | padogrid/aws-cluster |
| member3    | 18.222.225.117 | us-east-2b        | padogrid/aws-cluster |
| member4    | 18.188.216.237 | us-east-2b        | padogrid/aws-cluster |


### 2. Update Workspace `setenv.sh`

Gather the public IP addresses of the EC2 instances and list them in the workspace `setenv.sh` file.

```console
switch_workspace ws-aws-hazelcast
vi setenv.sh
```

Set the `VM_HOSTS` property with the public IP addresses and make sure `VM_USER` is set to the correct user name.

```bash
VM_HOSTS="3.18.113.154,3.21.44.203,18.218.42.247,18.222.225.117,18.188.216.237"
VM_USER="ec2-user"
```

### 3. Update `etc/cluster.properties`

Update the `etc/cluster.properties` file with the new EC2 IP addresses. For our example, the VM cluster name is `myhz`.

```console
switch_cluster myhz
vi etc/cluster.properties
```

Set the following VM properties. 

```bash
# Set Management Center host
mc.host=3.18.113.154

# Set member hosts
vm.hosts=18.218.42.247,18.222.225.117,18.188.216.237,3.18.113.154
```

:exclamation: If you have reinstated the workspace with an EC2 instance type that is different from the previous instance type then you may also need to change resource properties such as `heap.min` and `heap.max`.

### 4. Sync VMs

Run `vm_sync` to synchronize the workspace.

```console
vm_sync
```

The above command reports the following:

```console
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 3.18.113.154...
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 3.21.44.203...
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 18.218.42.247...
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 18.222.225.117...
Deploying hazelcast-addon_0.9.0-SNAPSHOT to 18.188.216.237...

Workspace sync: ws-aws-hazelcast
   Synchronizing 3.18.113.154...
   Synchronizing 3.21.44.203...
   Synchronizing 18.218.42.247...
   Synchronizing 18.222.225.117...
   Synchronizing 18.188.216.237...
------------------------------------------------------------------------------------------
WARNING:
/home/ec2-user/padogrid/products/jdk1.8.0_212
   JDK not installed on the following VMs. The workspace will not be operational
   until you install JDK on these VMs.
       3.18.113.154 3.21.44.203 18.218.42.247 18.222.225.117 18.188.216.237
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
       3.18.113.154 3.21.44.203 18.218.42.247 18.222.225.117 18.188.216.237
VM Hazelcast Path:
    /home/dpark/Work/linux/hazelcast-enterprise-3.12.6
To install Hazelcast on the above VMs, download the correct version of JDK and execute 'vm_install'.  
Example:
   vm_install -hazelcast hazelcast-enterprise-3.12.6.tar.gz
------------------------------------------------------------------------------------------
Workspace sync complete.
```

### 5. Install Software

`vm_sync` will display warning messages similar to the output shown above since the new EC2 instances do not have the required software installed. Download the required software and install them by running the `vm_install` command as shown below.

```console
vm_install -java ~/Downloads/jdk-8u212-linux-x64.tar.gz \
           -hazelcast ~/Downloads/hazelcast-enterprise-3.12.6.tar.gz
```

### 6. Start Cluster

Start the cluster.

```console
start_cluster
```

### 7. Run Apps

For our example, we have already created the `perf_test` app. Because we have configured the EC2 discovery service, the client app requires no changes. Let's run the app.

```console
cd_app perf_test; cd bin_sh
./test_ingestion -run
```

## Summary

Reactivating a VM workspace with new EC2 instances is just a matter of updating the workspace and cluster configuration files with the new instance IP addresses.
