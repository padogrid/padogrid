# Pods

We define a *pod* as a collection of VMs configured to run one ore more clusters. In `padogrid` environment, a pod refers to a collection of VirtualBox VMs created and managed by Vagrant on your local machine. To simplify Vagrant configuration, `padogrid` provides commands for building pods and assigning them to individual clusters. With a single command, for example, you can build and run a cluster that spans multiple VMs. As with the local clusters, all of the `padogrid` apps remain intact and are readily available for the pod clusters.

## Pod Type

There are two types of pods: *local* and *non-local*. The local pod runs on your host OS and non-local pods run on guest OS's. By default, `padogrid` creates clusters on the local pod, which gets implicitly created for you when you install `padogrid`. The non-local pods, on the other hand, must explicitly be created by running the `create_pod` command.

## Pod Name

Each pod is identifiable by a unique name. The local pod type has the `local` pod name and for others, you would assign names when you create them by running the `create_pod` command.

## Nodes

A node is essentially a VM that is fully configured to run a cluster member or an app. There are two types of nodes that belong to a pod: *primary node* and *data nodes*. The primary node is typically used to manage clusters and run client applications. The data nodes, on the other hand, run members. The current implementation dedicates one member per data node.

## Pod Properties

There are several pod properties that you can optionally set for creating pods primarily to prevent IP address conflicts. The following table shows a complete list of pod properties:

| Property | Default | Description |
| -------- | ------- | ----------- |
| pod.name | local | Unique pod name. |
| pod.type | local | Pod type. Valid types are `"local"` and `"vagrant"` |
| pod.box.image| ubuntu/trusty64 | Vagran box image name. You can choose another from the Vagrant repo: [https://app.vagrantup.com/boxes/search](https://app.vagrantup.com/boxes/search) |
| node.name.primary | pnode | Primary node name. The primary node is not a data node. It should be used to manage clusters and run client programs.|
| node.name.prefix | node | Data node name prefix. Each data node name begins with this prefix followed by a number assigned by the pod builder.|
| node.ip.lastOctet | 10 | The last octet of the primary node IP address. The pod buillder assigns incremented IP addresses to all nodes starting from this octect. |
| node.memory.primary | 2048 | Primary node memory size in MiB. |
|node.memory.data | 2048 | Data node memory size in MiB. |
| node.count | 2 | Number of data nodes. A node represents a VM. |
| host.productsDir | $PADOGRID_WORKSPACE/products | Host products directory path. |

## Creating VirtualBox Private Network (host-only)

To create a pod, you must first create a host-only network in VirtualBox to setup a private network with your host OS so that the VMs and the host OS can communicate each other.

### Linux/MacOS

- Open the VirtualBox Manager (tested with v6.0).
- Select "Tools" at the upper left corner
- Select "File/Host Network Manager..."
- Select the "Create" button at the upper left.
- We need a static address, so take the default settings of "Configure Adapter Manually". Jot down the name, IPv4 Address and IPv4 Network Mask. For example,
  - Name: vboxnet0
  - IPv4Address: 192.168.56.1
  - IPv4 Network Mask: 255.255.255.0
- Close the window.
- There seems to be a bug in VirtualBox on Mac which forgets to assign the IP address to the host OS. You can check it by running the following:

```console
# Display the host-only network interface.
ifconfig vboxnet0
```

- If you don't see the IP address assigned then run the following:

```console
# Assign the IP address.
sudo ifconfig vboxnet0 inet 192.168.56.1 netmask 255.255.255.0

# Check the settings
ifconfig vboxnet0
```

### Windows

- Open the VirtualBox Manager (tested with v5.2.30).
- Select "File/Host Network Manager..."
- Select the "Create" button at the upper left.
- We need a static address, so take the default settings of "Configure Adapter Manually". Jot down the name, IPv4 Address and IPv4 Network Mask. For example,
  - Name: VirtualBox Host-Only Network
  - IPv4Address: 192.168.56.1
  - IPv4 Network Mask: 255.255.255.0
- Close the window. 
- On DOS prompt, run the following and look for the above IP address. 

```console
REM Display all network adapters (interfaces).
ipconfig
```

## Guest OS Software

Pods share the `products` directory. Before you create a pod, you must first install the OS specific software on your host file system. For example, if your host OS is Mac and the Guest OS is Linux then you would create a directory on your Mac and install all the Linux software in that directory. You would then enter this directory path as the `products` directory path when you execute the `create_pod` command. By default, `create_pod` searches the `products` directory in your workspace. If you want to keep the default directory path then you can create a symbolic link to the Linux installation directory path as shown in the example below.

### Installed Linux Software Example

At a minimum, you need JDK and one of the supported data grid products, i.e., Geode, GemFire, Hazelcast, Jet, etc., installed for your Guest OS. The following is an example list of software that you should consider installing on your host OS.

```console
/Users/dpark/Work/linux/
├── grafana-6.2.5
├── apache-geode-1.11.0
├── pivotal-gemfire-9.9.1
├── hazelcast-jet-enterprise-3.2.2
├── hazelcast-enterprise-3.12.6
├── hazelcast-jet-enterprise-3.2.2
├── jdk1.8.0_212
├── jq-1.6
└── prometheus-2.10.0.linux-amd64
```

### Symbolic Link Example

```console
# Change directory to the workspace directory
cd_workspace

# Create the symbolic link to the installed product directory
ln -s /Users/dpark/Work/linux products
ls -l products
lrwxr-xr-x  1 dpark  staff    23 Dec 25 17:02 products -> /Users/dpark/Work/linux
```

## Pod Example

When you create a pod, the `create_pod` command will prompt for a host private IP address. This address must be a host-only network address. For our example in the previous section, that address would be `192.168.56.1`. Note that you can create multiple host-only networks and configure pods with different networks. Having pods on different networks enables WAN replication tests, for example.

```console
# By default, create_pod adds the primary node and two (2) data nodes.
create_pod -pod mypod

Please answer the prompts that appear blow. You can abort this command at any time
by entering 'Ctrl-C'.

Pod name [mypod]: 
Primary node name [pnode]: 
Data node name prefix [node]: 
This machine has the following IP addresses. Choose one from the list. The IP address
must be a private IP address.
192.168.1.8
192.168.56.1
Host private IP address []: 
Invalid IP addres.
Host private IP address []: 192.168.56.1
First node IP address' octect [10]: 
Primary node memory size in MiB [2048]: 
Data node memory size in MiB [2048]: 
Number of data nodes  [2]: 
Products installation directory path [/Users/dpark/Work/workspaces/ws-3.12/products]:
Vagrant box image [ubuntu/trusty64]: 

You have entered the following:
                       Pod name: mypod
              Primary node name: pnode
          Data node name prefix: node
        Host private IP address: 192.168.56.1
      Node IP addres last octet: 10
 Primary node memory size (MiB): 2048
    Data node memory size (MiB): 2048
                Data node count: 2
             Products directory: /Users/dpark/Work/workspaces/ws-3.12/products
              Vagrant box image: ubuntu/trusty64
Enter 'c' to continue, 'r' to re-enter, 'q' to quit: 

...

# Add one (1) more data node.
add_node -pod mypod

# View the pod properties
show_pod -pod mypod

# Create a cluster to run on the 'mypod' pod. The create_cluster command
# automatically adds members to the cluster to match the number of data nodes
# in the pod. In this example, it adds two (2) members.
create_cluster -pod mypod -cluster finance

# Build the pod. This command builds and launches the Vagrant pod environment with
# one (1) primary node and three (3) data nodes.
build_pod -pod mypod

# Upon completion of the 'build_pod' command, change directory to the pod directory
# and login to the primary node using the password as shown below.
cd_pod mypod
vagrant ssh
password: vagrant
 
# Once logged on to the primary node, switch cluster to finance
switch_cluster finance

# Start the finance cluster
start_cluster

# Check the cluster status.
show_cluster
```

## Pod Files

Each pod runs in their own directory under the parent directory:

```console
ls -l $PADOGRID_WORKSPACE/pods
-rw-r--r--  1 dpark  staff  3138 Jul  8 21:07 README.md
drwxr-xr-x  5 dpark  staff   160 Jul 10 14:20 mypod
```

Each pod has the following directory structure:

```console
mypod/
└── Vagrantfile/
    ├── bin_sh/
    │   ├── build_app
    │   ├── configure_nodes
    │   ├── setenv.sh
    │   └── etc/
    │       ├── pod.properties
    │       └── template-Vagrantfile-*
    └── log/         
```

**Vagrantfile**

The Vagrant configuration file generated by the `build_pod` command. Note that `build_pod` actually invokes `bin_sh/build_app` to generate this file.

**bin_sh/**

| File | Description |
| ---- | ------------ |
| build_app | Builds `Vagrantfile` based on the properties extracted from `etc/pod.properties`. This command is initially invoked by `build_pod`. If you make changes to the `etc/pod.properties` file, then you will need to run this command to regenerate `Vagrantfile`. |
| configure_nodes | Configures all nodes. This command must be run after the VMs are up and running. |
| setenv.sh | This file gets sourced in by the above commands. You can overwrite environment variables or run other tasks in this file. | 

**etc/**

| File | Description |
| ---- | ------------ |
| pod.properties | Pod properties collected by the 'create_pod' command. |
| template-Vagrantfile-\* | Vagrant template files used by the `build_app` command to generate `Vagrantfile`. | 

## Multicast DNS (mDNS)

`padogrid` relies on Avahi to resolve hostnames to IP addresses. Avahi implements the mDNS/DNS-SD protocol to provide a zero-configration service and is fully compatible with MacOS X. The `build_pod` command you executed in the [Pod Example](#Pod-Example) section automatically installs `avahi` on all guest OS nodes. 

For more information on `avahi` and mDNS see the links below:

- [https://www.avahi.org](https://www.avahi.org)
- [https://en.wikipedia.org/wiki/Multicast_DNS](https://en.wikipedia.org/wiki/Multicast_DNS)

### Host OS

**MacOS:** If your host OS is MacOS, then it supports mDNS out of the box as metioned above. You can immediately use the guest OS node names.

**Linux:** If your host OS is Linux then you may need to install `avahi` if it hasn't been installed already. Run the following command to install it.

```console
# Ubuntu, Debian, etc.
apt-get install -y avahi-daemon libnss-mdns

# RedHat, CentOS, etc.
yum install avahi
```

**Windows:** If your host OS is Windows, then unfortunately, mDNS support is currently unavailable. You must manually enter the node names and their IP addresses in the `C:\Windows\System32\drivers\etc\hosts` file as shown in the example below.

**File:** `C:\Windows\System32\drivers\etc\hosts`

```console
# padogrid pods
192.168.56.10	pnode.local
192.168.56.11	node-01.local
192.168.56.12	node-02.local
```

### Guest OS

`padogrid` automatically configures mDNS by installing `avahi` on all Guest OS virtual machines.

## Stopping a Pod

```console
# The following stops (halts) mypod.
stop_pod -pod mypod
```

## Rebuilding a Pod

If you make any changes to the `etc/pod.properties` file, for example, you increased the node memory size, then you will need to rebuild the pod as follows:

```console
# Destroy and rebuild the pod
vagrant destroy -f && build_pod -pod mypod
```

## Removing a Pod

You should stop the pod first before removing it. Otherwise, you would need to stop (halt) nodes (VMs) individually using the VirtualBox Manager. You would also want to create a pod backup by running the 'create_bundle' command as shown below.

```console
# Backup, stop and remove mypod. Note 'remove_pod' removes the
# entire my pod directory. 'create_bundle' creates a backup so
# that you can bring it back if needed.
create_bundle -pod mypod
stop_pod -pod mypod
remove_pod -pod mypod
```
