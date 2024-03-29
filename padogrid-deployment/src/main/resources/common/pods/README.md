# Vagrant Pods

We define a *pod* as a collection of VMs configured to run one or more clusters. In PadoGrid environments, a pod refers to a collection of VirtualBox VMs created and managed by Vagrant on your local machine. To simplify Vagrant configuration, PadoGrid provides commands for building pods and assigning them to individual clusters. With a single command, for example, you can build and run a cluster that spans multiple VMs. As with the local clusters, all of the PadoGrid apps remain intact and are readily available for the pod clusters.

## Pod Type

There are two types of pods: *local* and *non-local*. The local pod runs on your host OS and non-local pods run on guest OS's. By default, PadoGrid creates clusters on the local pod, which gets implicitly created for you when you install PadoGrid. The non-local pods, on the other hand, must explicitly be created by running the `create_pod` command.

## Pod Name

Each pod is identifiable by a unique name. The local pod type has the `local` pod name and for others, you would assign names when you create them by running the `create_pod` command.

## Nodes

A node is essentially a VM that is fully configured to run a cluster member or an app. There are two types of nodes that belong to a pod: *primary node* and *data nodes*. The primary node is typically used to manage clusters and run client applications. The data nodes, on the other hand, run members. The current implementation dedicates one member per data node.

## Pod Properties

There are several pod properties that you can optionally set for creating pods primarily to prevent IP address conflicts. The following table shows a complete list of pod properties:

| Property | Default | Description |
| -------- | ------- | ----------- |
| pod.name | local   | Unique pod name. |
| pod.type | local   | Pod type. Valid types are `"local"` and `"vagrant"` |
| pod.box.image| ubuntu/jammy64 | Vagrant box image name. You can choose another from the [Vagrant repo](https://app.vagrantup.com/boxes/search). Not all Vagrant boxes will work in PadoGrid since most of them are customized images. The [Tested Vagrant Boxes](https://github.com/padogrid/padogrid/wiki/Tested-Vagrant-Boxes) section of the PadoGrid manual provides a list of recommended Vagrant boxes. |
| node.name.primary | pnode | Primary node name. The primary node is not a data node. It should be used to manage data grid clusters and run client programs.|
| node.name.prefix | node | Data node name prefix. Each data node name begins with this prefix followed by a number assigned by the pod builder.|
| node.ip.lastOctet | 10 | The last octet of the primary node IP address. The pod buillder assigns incremented IP addresses to all nodes starting from this octect. **Deprecated. Starting PadoGrid 0.9.33, the last octet of the IP address is extracted from the IP address specified by the `create_pod -ip` command.** |
| node.memory.primary | 2048 | Primary node memory size in MiB. |
|node.memory.data | 2048 | Data node memory size in MiB. |
| node.count | 2 | Number of data nodes. A node represents a VM. |
| host.productsDir | $PADOGRID_WORKSPACE/products/linux | Host products directory path where Linux products are installed. |

## Creating VirtualBox Private Network (host-only)

To create a pod, you must first create a host-only network in VirtualBox to setup a private network with your host OS so that the VMs and the host OS can communicate each other.

### Linux/MacOS

#### VirtualBox 7.x

- Open the VirtualBox Manager (tested with v7.0).
- Select "Tools" at the upper left corner.
- Select th "Host-only Netowrks" tab.
- Select the "Create" button at the top.
- Enter "Lower Bound" and "Upper Bound" IP addresses. VirtualBox 7.x has deprecated host-only adapters in favor of host-only networks. When you create a pod, you will need to supply an IP address in this range.

#### VirtualBox 6.x, 5.x

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

#### VitualBox 6.x, 5.x

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
/Users/dpark/Padogrid/products/linux
├── apache-geode-1.15.1
├── grafana-10.2.0
├── hazelcast-enterprise-5.3.6
├── jdk-11.0.21
├── jq-linux-amd64
├── vmware-gemfire-10.0.2
└── prometheus-2.49.1.linux-amd64
```

## Pod Example

When you create a pod, the `create_pod` command will prompt for a host private IP address. This address must be a host-only network address. For our example in the previous section, that address would be `192.168.56.1`. Note that you can create multiple host-only networks and configure pods with different networks. Having pods on different networks enables WAN replication tests.

The following shows how to create a pod named `mypod` and run a cluster named `finance` on it. Note that the cluster we associate with the pod has no product dependencies. It can be any PadoGrid supported product, e.g., Geode, Hazelcast, etc.

```bash
create_pod -avahi -pod mypod
```

The `create_pod` prompts for your inputs. Each prompt includes the default value enclosed in square brackets. Take the default values by hitting the *Enter* key.

```console
Please answer the prompts that appear below. You can abort this command at any time
by entering 'Ctrl-C'.

Pod name [mypod]:
Primary node name [pnode]:
Data node name prefix [node]:
Enter the host-only IP address for the first VM. This IP address must be
in the range defined by a host-only interface. You can create a host-only
interface by selecting the Tools/Host-only Adapters/Networks menu
from the VirtualBox console. IP address typically begins from 192.168.56.2.
Enter the host-only IP address for the first VM [192.168.56.2]:
Primary node memory size in MiB [2048]:
Data node memory size in MiB [2048]:
Number of data nodes  [2]:
Products installation directory path.
[/Users/dpark/Padogrid/products/linux]:

Install Avahi? This allows VMs to enable local network discovery service via
the mDNS/DNS-SD protocol. All VM host names can be looked up with the suffix
'.local', i.e., pnode.local, node-01.local, etc.
Enter 'true' or 'false' [true]:
Vagrant box image [ubuntu/jammy64]:

You have entered the following.
                       Pod name: mypod
              Primary node name: pnode
          Data node name prefix: node
     First host-only IP address: 192.168.56.2
 Primary node memory size (MiB): 2048
    Data node memory size (MiB): 2048
                Data node count: 2
             Products directory: /Users/dpark/Padogrid/products/linux
                  Avahi enabled: true
              Vagrant box image: ubuntu/jammy64
Enter 'c' to continue, 'r' to re-enter, 'q' to quit: c

-------------------------------------------------------------------
               WORKSPACE: /Users/dpark/Padogrid/workspaces/rwe-bundles/bundle-geode-1-app-perf_test_sb-cluster-sb
             Pod Created: mypod
                POD_TYPE: vagrant
           POD_BOX_IMAGE: ubuntu/jammy64
       NODE_NAME_PRIMARY: pnode
        NODE_NAME_PREFIX: node
         HOST_PRIVATE_IP: 192.168.56.2
NODE_PRIMARY_MEMORY_SIZE: 2048
   Data NODE_MEMORY_SIZE: 2048
      NODE_PRIMARY_COUNT: 1
         Data NODE_COUNT: 2
       HOST_PRODUCTS_DIR: /Users/dpark/Padogrid/products/linux
           AVAHI_ENABLED: true
           POD_BOX_IMAGE: ubuntu/jammy64

POD_DIR: /Users/dpark/Padogrid/workspaces/rwe-bundles/bundle-geode-1-app-perf_test_sb-cluster-sb/pods/mypod

The specified pod has successfully been created and configured.
To add more nodes, run 'add_node'.
To remove nodes, run 'remove_node'.
The parameters have been saved in the following file (You can edit this
file before running 'build_pod'):

ETC_DIR: /Users/dpark/Padogrid/workspaces/rwe-bundles/bundle-geode-1-app-perf_test_sb-cluster-sb/pods/mypod/etc/pod.properties

The pod is ready to be built. Execute the following command to build the pod.

   build_pod -pod mypod
-------------------------------------------------------------------
```

For our example, let's add one (1) more data node by executing `add_node`.

```bash
# Add one (1) more data node.
add_node -pod mypod
```

Let's view the `mypod` details by executing `show_pod`.

```bash
# View the pod properties
show_pod -pod mypod -long
```

**Output:**

```console
         WORKSPACE: /home/dpark/Padogrid/workspaces/myrwe/myws
               POD: mypod
          POD_TYPE: vagrant
      POD_CLUSTERS:
         POD State: Down
 NODE_NAME_PRIMARY: pnode
  NODE_NAME_PREFIX: node
NODE_PRIMARY_COUNT: 1
   Data NODE_COUNT: 3
           POD_DIR: /home/dpark/Padogrid/workspaces/myrwe/myws/pods/mypod
```

We have configured the pod with one (1) primary node and a total of three (3) data nodes. Let's create a data grid cluster that will use this pod by executing the `create_cluster` command.

```bash
create_cluster -product geode -pod mypod -cluster finance
```

We are now ready to build the pod by executing the `build_pod` command, which downloads and configures the Vagrant image.

```bash
build_pod -pod mypod
```

Upon completion of the `build_pod` command, change directory to the pod directory and login to the primary node as shown below.

```bash
# Older versions of Vagrant box images may require a login password. If it prompts
# for a password, then enter 'vagrant'.
cd_pod mypod
vagrant ssh

# You can also login to any of the nodes using ssh.
ssh vagrant@192,168.56.2
ssh vagrant@192,168.56.3
ssh vagrant@192,168.56.4
ssh vagrant@192,168.56.5

# If you enabled Avahi, then you can also login using the VM host names.
ssh vagrant@pnode.local
ssh vagrant@node-01.local
ssh vagrant@node-02.local
ssh vagrant@node-03.local
```

Once logged on to the primary node, switch cluster to `finance` and start the cluster.
 
```bash
switch_cluster finance

# Start the finance cluster
start_cluster

# Check the cluster status.
show_cluster
```

Your client apps can connect to the cluster from any of the nodes including the host OS. You can use the data node IP addresses or their host names described in the [Multicast DNS (mDNS)](#multicast-dns-mdns) section to connect to the cluster.

The following sections show Geode and Hazelcast `perf_test`  examples.

### Geode

Create `perf_test` and edit the client configuration file.

```console
create_app
cd_app perf_test
vi etc/client-cache.xml
```

Set the member addresses in the configuration file.

```xml
<client-cache ...>
...
   <pool name="serverPool">
      <locator host="pnode.local" port="10334" />
   </pool>
...
</client-cache>
```

Run `perf_test`.

```console
cd_app perf_test; cd bin_sh
./test_ingestion -run
```


### Hazelcast

Create `perf_test` and edit the client configuration file.

```console
create_app
cd_app perf_test
vi etc/hazelcast-client.xml
```

Set the member addresses in the configuration file.

```xml
<hazelcast-client ...>
...
   <network>
      <cluster-members>
         <address>node-01.local:5701</address>
         <address>node-02.local:5701</address>
         <address>node-03.local:5701</address>
      </cluster-members>
   </network>
...
</hazelcast-client>
```

Run `perf_test`.

```console
cd_app perf_test; cd bin_sh
./test_ingestion -run
```

## Pod Files

Each pod runs in their own directory under the parent directory:

```console
ls -l $PADOGRID_WORKSPACE/pods
```

**Output:**

```console
-rw-r--r--  1 dpark  staff  3138 Jul  8 21:07 README.md
drwxr-xr-x  5 dpark  staff   160 Jul 10 14:20 mypod
```

Each pod has the following directory structure:

```console
mypod/
├── Vagrantfile
├── bin_sh/
│   ├── build_app
│   ├── configure_nodes
│   └── setenv.sh
├── bootstrap.sh
├── etc/
│   ├── pod.properties
│   └── template-Vagrantfile-*
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

PadoGrid relies on Avahi to resolve hostnames to IP addresses. Avahi implements the mDNS/DNS-SD protocol to provide a zero-configration service and is fully compatible with MacOS X. The `build_pod` command you executed in the [Pod Example](#pod-example) section automatically installs `avahi` on all guest OS nodes. 

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
# PadoGrid pods
192.168.56.2	pnode.local
192.168.56.3	node-01.local
192.168.56.4	node-02.local
192.168.56.5	node-03.local
```

### Guest OS

PadoGrid automatically configures mDNS by installing `avahi` on all Guest OS virtual machines.

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

You should stop the pod first before removing it. Otherwise, you would need to stop (halt) nodes (VMs) individually using the VirtualBox Manager. Optionally, you might want to create a pod backup by running the `create_bundle` command before removing the pod as shown below. You can reinstall the backup later to reproduce the same pod environment using the `install_bundle` command.

```console
# Backup, stop and remove mypod. Note 'remove_pod' removes the
# entire my pod directory. 'create_bundle' creates a backup so
# that you can bring it back if needed.
create_bundle -pod mypod
stop_pod -pod mypod
remove_pod -pod mypod
```
