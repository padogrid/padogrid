# Running PadoGrid on VMs and PMs

PadoGrid provides a simple way to manage Hazelcast clusters on laptop, Vagrant pods, Kubernetes, and VMs. This article describes how to create and run a Hazelcast cluster on multiple VMs and/or PMs (Physical Machines). Instructions provided apply to any VMs and PMs including AWS, Azure, GCP, physical machines, VirtualBox, vSphere, etc. 

## Password-less SSH Login

The first step is to check to make sure you are able to login to all VMs without the password. For AWS EC2, for example, this is already done for you so you can skip this section.

If you need to manually setup password-less SSH login, then follow the [instructions here](/doc/password-less-ssh-login.md).


## Install PadoGrid

Untar the distribution on anywhere in the VM file system. For our example, we'll untar it in the `~/Hazelcast` directory and also install Hazelcast Enterprise and JDK in the same directory.

```console
mkdir -p ~/padogrid/products
tar -C ~/padogrid/products -xzf padogrid_0.9.1.tar.gz
tar -C ~/padogrid/products -xzf hazelcast-enterprise-3.12.6.tar.gz
tar -C ~/padogrid/products -xzf jdk-8u212-linux-x64.tar.gz
```

If you have your private key file (`.pem`) for ssh login, then place it in the `~/padogrid/products` directory.

```console
# Example: place your AWS SSH private key file in ~/padogrid
tree -L 2 ~/padogrid
~/padogrid
├── foo-pk.pem
└── products
    ├── hazelcast-enterprise-3.12.6
    ├── jdk1.8.0_212
    └── padogrid_0.9.1
```

## Initialize PadoGrid

Execute the following interactive command and provide values for all prompts. Let's create the default workspace `ws-vm` and the default cluster 'vm-cluster` in the `~/padogrid/workspace/myrwe` directory for our demo.

:exclamation: Note the `-vm` option which enables the default cluster to run on VMs.

```console
~/padogrid/products/padogrid_0.9.1/bin_sh/init_hzaddon -path ~/padogrid/workspaces/rwe -workspace ws-vm -cluster vm-cluster -vm
```

## Set Enterprise License Key

Set the enterprise license key in the workspaces `setenv.sh` file.

```console
# Set the enterprise license key
vi ~/padogrid/workspaces/rwe/setenv.sh
IMDG_LICENSE_KEY=xxxxx
```

## Source in `initenv.sh`

Source in the workspaces `initenv.sh` file which sets your workspaces-wide environment variables.

```console
. ~/padogrid/workspace/myrwe/initenv.sh

# Optionally add the above line in .bashrc so that hazelcast-addon is
# automatically initialized when you login next time.
echo ". ~/padogrid/workspace/myrwe/initenv.sh" >> ~/.bashrc
```

### Environment Variable: `HAZELCAST_ADDON_WORKSPACE`

The environment variable `HAZELCAST_ADDON_WORKSPACE` is set by sourcing in `initenv.sh`. We will use this variable in the subsequent sections.

### `vm-cluster`

The above command creates the `$HAZELCAST_ADDON_WORKSPACE/clusters/vm-cluster` directory which contains sub-directories and files that are specific to the `vm-cluster` cluster.

```console
vm-cluster
├── etc
│   ├── cluster.properties
│   ├── hazelcast.xml
│   ├── log4j2.properties
│   └── prometheus.yml
├── lib
├── log
├── plugins
├── run
│   └── vm-cluster-ubuntu1-01
└── setenv.sh
```

Let's switch to this cluster so that we can operate the cluster within its context.

```console
switch_cluster vm-cluster
```

### `etc/cluster.properties`

In the `vm-cluster` directory, you will find `etc/cluster.properties` which defines cluster-level properties. Let's edit this file to configure password-less `ssh` login and include **additional VMs** that can be part of the `vm-cluster` cluster.

```console
# Browse the cluster properties and change them as needed. 
# Pay attention to the following two (2) properties:
#   mc.host=
#   vm.privateKeyFile=
vi etc/cluster.properties

# Set Management Center host
mc.host=ubuntu1

# Set the private key file path. This property must be commented out
# if a private key file is not required.
vm.privateKeyFile=~/Hazelcast/foo-pk.pem

# Include additional VM host names. Host names (or IP addresses) must be
# comma separated with no spaces. Spaces are not supported.
vm.hosts=ubuntu1,ubuntu2,ubuntu3
```

### `etc/hazelcast.xml`

You can also update the `hazelcast.xml` file at this time as needed.

```console
vi etc/hazelcast.xml
```

## Deploy PadoGrid

You can repeat the above steps for all other VMs or create a tarball of the `~/padogrid` directory and untar it on the other VMs. That could be a lot of work. No worries. PadoGrid is equipped with several utility commands that can help you with such repetitive tasks. For our demo, we'll create a tarball of the entire `~/Hazelcast` directory and deploy it to all the VMs with a couple of commands.

```console
# Change directory to the your home directory.
cd

# Create a tarball of the entire Hazelcast directory
tar czf padogrid.tar.gz padogrid/

# Copy the tarball to all the VMs listed in the 'cluster.properties' file
vm_copy padogrid.tar.gz

# Untar the tarball in all other VMs
vm_exec tar -C ~/ -xzf ~/padogrid.tar.gz

# Update .bashrc in all other VMs (Optional)
vm_exec "echo \". ~/padogrid/workspace/myrwe/initenv.sh\" >> ~/.bashrc"
```

You now have JDK, Hazelcast Enterprise, PadoGrid, and the `ws-vm` workspace installed on all the VMs.

## Add Members

You now need to add a member to each VM by executing `add_member -all`, which adds one member to each VM. 

:exclamation: For VM clusters, `add_member` adds a member to a VM only if the VM does not have a member already added. Remember, PadoGrid supports only one member per VM per cluster. On the other hand, for non-VM clusters, `add_member` indiscriminately adds a new member regardless of the member count.

```console
# Run the following to add a member to each VM
add_member -all
```

## Deploy Changes

Any changes you made can be easily deployed to all the VMs specified in the `cluster.properties` file by  running the `vm_copy` command. The `vm_copy` command copies the specified file or directory to the same path in all the VMs. For example, if you made any changes in the `vm-cluster` directory, you can deploy the entire directory by executing the following:

```console
# Copy by absolute path
vm_copy $HAZELCAST_ADDON_WORKSPACE/clusters/vm-cluster

# Or change directory to vm-cluster and copy from there
cd_cluster
vm_copy .
```

## About `vm_copy`

The`vm_copy` command is a very powerful command for applying your changes to all of the VMs. It is likely that you will often be reconfiguring Hazelcast clusters or updating application library files. As with all other commands, by default, `vm_copy` is bound to the cluster settings in the `etc/cluster.properties` file. If you need to apply changes to the VMs other than the ones specified in the `cluster.properties` file, then you can use the `-vm` option to list the VMs to which the changes are to be applied. For example, if you need to include additional VMs to the cluster, you can install the tarball you created earlier using the `-vm` option as follows:

```console
# Copy the tarbarll to 2 additional VMs.
vm_copy -vm ubuntu4,ubuntu5 padogrid.tar.gz

# Untar it on the 2 VMs.
vm_exec -vm ubuntu4,ubuntu5 tar -C ~/ -xzf ~/padogrid.tar.gz
```

Continuing with the example, to include the new VMs in the cluster, you update the `etc/cluster.properties` file and once again use the `vm_copy` command to apply the changes to all of the VMs including the ones you just added.

```console
cd_cluster
vi etc/cluster.properties

# Include additional VM host names. Host names (or IP addresses) must be
# comma separated with no spaces. Spaces are not supported.
vm.hosts=ubuntu1,ubuntu2,ubuntu3,ubuntu4,ubuntu5

# Apply changes to all the VMs except for this VM.
vm_copy etc/cluster.properties
```

## Start Cluster

You are now ready to start the `vm-cluster` cluster.

```console
start_cluster
```

## Monitor Cluster

To monitor the cluster:

```console
show_cluster
```

## View Log

To view log

```console
# ubuntu1
show_log

# ubuntu2
show_log -num 2

# ubuntu3
show_log -num 3
```

## Start Management Center

Start the Management Center on `ubuntu1`.

```console
# From ubuntu1, run
start_mc
```

**URL:** [http://ubuntu1:8080/hazelcast-mancenter](ubuntu1:8080/hazelcast-mancenter)

## Stop/Kill Cluster

```console
stop_cluster
kill_cluster
```

## Stop Management Center

```console
stop_mc
```

## Tear Down

If you want to remove the cluster from all the VMs, then you must first stop the cluster and execute the `remove_cluster` command. The workspace can be removed using the `vm_exec -all` command. 

```console
# Stop cluster
stop_cluster

# Remove cluster. Unlike other commands, this command requires the '-cluster' option.
remove_cluster -cluster vm-cluster

# Remove workspace from all VMs. The '-all' option includes the VM in which you 
# are executing this command. Note that we specify the host names 
# since we have removed the vm-cluster 
vm_exec -all -vm ubuntu1,ubuntu2,ubuntu3,ubuntu4,ubuntu5 rm -rf $HAZELCAST_ADDON_WORKSPACE

# Of course, we can also remove the entire workspaces directory.
vm_exec -all -vm ubuntu1,ubuntu2,ubuntu3,ubuntu4,ubuntu5 rm -rf ~/padogrid/workspace/myrwe
```
