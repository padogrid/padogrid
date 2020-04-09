# PadoGrid on AWS

This document describes how to install and run PadoGrid using [Hazelcast Discovery Plugin for AWS](https://github.com/hazelcast/hazelcast-aws). The following steps are described in detail in the subsequent sections.

1. [Launch EC2 Instances](#1-launch-ec2-instances)
2. [Install Software](#2-install-software)
3. [Create Workspace Environment](#3-create-workspaces-environment)
4. [Configure Cluster](#4-configure-cluster)
5. [Deploy Installed Software](#5-deploy-installed-software)
6. [Start Cluster](#6-start-cluster)
7. [Start Management Center](#7-start-management-center)
8. [Configure App](#8-configure-app)
9. [Start App](#9-start-app)
10. [Tear Down](#10-tear-down)

## 1. Launch EC2 Instances

For our demo, we'll launch `t2.micro` instances.

### Hazelcast Cluster VMs

Launch EC2 instances in two (2) different zones with the tag, `padogrid`. For example,

| Instance    | Type     | Zone       | Tag      |
| ----------- | -------- | ---------- | -------- |
| cluster-vm1 | t2.micro | us-east-2a | padogrid |
| cluster-vm2 | t2.micro | us-east-2a | padogrid |
| cluster-vm3 | t2.micro | us-east-2b | padogrid |
| cluster-vm4 | t2.micro | us-east-2b | padogrid |

### Management Center VM

Launch an EC2 instance in any zone for running the Management Center. Let's call this host `mc-vm`.

| Instance    | Type     |  Zone      | Tag      |  
| ----------- | -------- | ---------- | -------- |
| mc-vm       | t2.micro | us-east-2c | padogrid |

### Client App VM

Launch an EC2 instance in any zone for running client apps.

| Instance    | Zone     | Zone       |
| ----------- | -------- | ---------- |
| client-vm   | t2.micro | us-east-2c |

## 2. Install Software

Install the following software distributions in the Management Center VM, i.e., `mc-vm`.

- hazelcast-addon
- Hazelcast Enterprise
- JDK

For our demo, we assume the `~/padogrid/products` directory contains all installations. We will create `workspaces` in the next section.

```console
~/padogrid/products
├── hazelcast-addon_0.9.1-SNAPSHOT
├── hazelcast-enterprise-3.12.6
├── jdk1.8.0_212
└── workspaces
```

## 3. Create Workspaces Environment

```console
~/padogrid/products/padogrid_0.9.1-SNAPSHOT/bin_sh/create_root

Please answer the prompts that appear below. If you are not able to complete
the prompts at this time then use the '-quiet' option to bypass the prompts.
You can complete the requested values later in the generated 'setenv.sh' file
You can abort this command at any time by entering 'Ctrl-C'.

Enter workspaces home path. This is where all of your workspaces will be stored.
[]:
/home/ec2-user/padogrid/workspaces/myrwe 
Enter Java home path.
[]:
/home/ec2-user/padogrid/products/jdk1.8.0_212
Enter Hazelcast IMDG Enterprise home directory path.
[]:
/home/ec2-user/padogrid/products/hazelcast-enterprise-3.12.6
Enter Hazelcast Jet Enterprise home directory path.
[]:

Directory does not exist or not a directory. Do you want to skip?
Enter 'c' to skip this question and continue, 'r' to re-enter, 'q' to quit: c
Enter default workspace name [myws]: 
Enter default cluster name [myhz]: 
Enable VM? Enter 'true' or 'false' [false]: true

You have entered the following.
  HAZELCAST_ADDON_WORKSPACES: /home/ec2-user/padogrid/workspaces/myrwe
                   JAVA_HOME: /home/ec2-user/padogrid/products/jdk1.8.0_212
              HAZELCAST_HOME: /home/ec2-user/padogrid/products/hazelcast-enterprise-3.12.6
                    JET_HOME: 
           Default Workspace: myws
             Default Cluster: myhz
                  VM Enabled: true
Enter 'c' to continue, 'r' to re-enter, 'q' to quit: c
```

### Enter License Key

```console
vi /home/ec2-user/padogrid/workspaces/myrwe/setenv.sh
IMDG_LICENSE=***
```

### Initialize Workspaces

```console
. /home/ec2-user/padogrid/workspaces/myrwe/initenv.sh
```

### Update .bashrc

Let's add the workspaces initialization step in the `.bashrc` file so that we don't need to run in manually.

```console
echo ". /home/ec2-user/padogrid/workspaces/myrwe/myws/initenv.sh -quiet" >> ~/.bashrc
```

## 4. Configure Cluster

### Add VMs

Edit `etc/cluster.properties`

```console
switch_cluster myhz
vi etc/cluster.properties
```

Make the following changes in the `etc/cluster.properties` file:

```properties
# Change the heap size to 256m
heap.min=256m
heap.max=256m

# Set the management center host name. This must the public host name.
mc.host=mc-vm

# IMPORTANT: vm.hosts must not contain spaces
vm.hosts=cluster-vm1,cluster-vm2,cluster-vm3,cluster-vm4

# Set your private key file path
vm.privateKeyFile=/home/ec2-user/padogrid/your-private-key.pem
```

### Set Discovery Service

Edit `etc/hazelcast.xml`
```console
cd_cluster myhz
vi etc/hazelcast.xml
```

Replace the `<join>` element and add `ZONE_AWARE` as follows:

```xml
   <join>
      <multicast enabled="false"></multicast>
      <aws enabled="true">
         <access-key>your-access-key</access-key>
         <secret-key>your-secret-key</secret-key>
         <region>us-east-2</region>
         <security-group-name>launch-wizard-1</security-group-name>
         <tag-key>padogrid</tag-key>
         <tag-value>aws-cluster</tag-value>
         <hz-port>5701</hz-port>
         <connection-retries>3</connection-retries>
      </aws>
   </join>
   <partition-group enabled="true" group-type="ZONE_AWARE"></partition-group>
```

## 5. Deploy Installed Software

We are now ready to deploy software including the changes we made to all the cluster VMs. This is done by simply creating a tarball of the entire `~/padogrid/products` directory and deploy the tarball to the cluster VMs using the `vm_copy` and `vm_exec` commands included in PadoGrid.

### Create Tarball

```console
cd
tar czf Hazelcast.tar.gz Hazelcast
```

### Deploy `.bashrc` and Tarball

Let's deploy `.bashrc` and the tarball to all the cluster VMs.

```console
vm_copy .bashrc
vm_copy Hazelcast.tar.gz
vm_exec tar xzf Hazelcast.tar.gz
```

Let's also deploy it to the client VM, i.e., `client-vm` so that we can run the `perf_test` app included in PadoGrid. Note that you need to specify the client VM host name using the `-vm` option. This is because the client VM host is not part of the cluster.

```console
vm_copy -vm client-vm .bashrc
vm_copy -vm client-vm Hazelcast.tar.gz
vm_exec tar xzf Hazelcast.tar.gz
```

## 6. Start Cluster

```console
start_cluster

# Monitor log files to see members join to form a cluster
show_log
```

## 7. Start Management Center

```console
start_mc
```

`start_mc` outputs the management center URL.

## 8. Configure App

Login to `client-vm` and install the `perf_test` app.

```console
create_app
```

### Set Discovery Service

Edit `etc/hazelcast-client.xml`

```console
create_app
cd_app perf_test
vi etc/hazecast-client.xml
```

Replace the `<network>` element with the following. The \<aws\> element is exactly same as the one we have configured for the members earlier.

```xml
   <network>
      <aws enabled="true">
         <access-key>your-access-key</access-key>
         <secret-key>your-secret-key</secret-key>
         <region>us-east-2</region>
         <security-group-name>launch-wizard-1</security-group-name>
         <tag-key>padogrid</tag-key>
         <tag-value>aws-cluster</tag-value>
         <hz-port>5701</hz-port>
         <connection-retries>3</connection-retries>
         <!-- If you are running clients outside of AWS, i.e., on your PC
         then include the following: -->
         <!--
         <use-public-ip>true</use-public-ip>
         -->
      </aws>
   </network>
```

## 9. Start App

```console
cd_app perf_test; cd bin_sh
./test_injestion -run
```

## 10. Tear Down

Login to `mc-vm` and execute the following:

```console
stop_cluster
stop_mc
```

Terminate EC2 instances.
