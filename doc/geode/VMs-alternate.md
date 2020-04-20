# Running PadoGrid on VMs and PMs

PadoGrid provides a simple way to manage Geode/GemFire clusters on laptop, Vagrant pods, and VMs. This article describes how to create and run a Geode/GemFire cluster on multiple VMs and/or PMs (Physical Machines). The instructions provided apply to any VMs and PMs including AWS, Azure, GCP, physical machines, VirtualBox, vSphere, Vagrant pods (VMs), etc. 

## Password-less SSH Login

The first step is to check to make sure you are able to login to all VMs without the password. For AWS EC2, for example, this is already done for you so you can skip this section.

If you need to manually setup password-less SSH login, then follow the [instructions here](/doc/password-less-ssh-login.md).

## Install PadoGrid

Untar the distribution on anywhere in the VM file system. For our example, we'll untar it in the `~/padogrid/products` directory and also install Geode Enterprise and JDK in the same directory.

```console
mkdir -p ~/padogrid/products
tar -C ~/padogrid/products -xzf padogrid_0.9.1.tar.gz
tar -C ~/padogrid/products -xzf apache-geode-1.11.0.tgz
tar -C ~/padogrid/products -xzf jdk-8u212-linux-x64.tar.gz
```

If you have your private key file (`.pem`) for ssh login, then place it in the `~/padogrid` directory.

```console
# Example: place your AWS SSH private key file in ~/padogrid
tree -L 2 ~/padogrid
~/padogrid
├── foo-pk.pem
└── products
    ├── apache-geode-1.11.0
    ├── padogrid_0.9.1
    └── jdk1.8.0_212
```

## Initialize PadoGrid

Execute the following interactive command and provide values for all prompts. Let's create the default workspace `ws-vm` and the default cluster 'vm-cluster` in the `~/padogrid/workspaces/myrwe` directory for our demo.

:exclamation: Note the `-vm` option which enables the default cluster to run on VMs.

```console
~/padogrid/products/padogrid_0.9.1/bin_sh/init_geode_addon -path ~/padogrid/workspaces/myrwe -workspace ws-vm -cluster vm-cluster -vm
```

## Source in `initenv.sh`

Source in the workspaces `initenv.sh` file which sets your workspaces-wide environment variables.

```console
. ~/padogrid/workspaces/myrwe/initenv.sh

# Optionally add the above line in .bashrc so that padogrid is
# automatically initialized when you login next time.
echo ". ~/padogrid/workspaces/myrwe/initenv.sh" >> ~/.bashrc
```

### Environment Variable: `GEODE_ADDON_WORKSPACE`

The environment variable `GEODE_ADDON_WORKSPACE` is set by sourcing in `initenv.sh`. We will use this variable in the subsequent sections.

### `vm-cluster`

The above `create_root` command executed in the [Initialize PadoGrid](#initialize-padogrid) section creates the `$GEODE_ADDON_WORKSPACE/clusters/vm-cluster` directory which contains sub-directories and files that are specific to the `vm-cluster` cluster. 

```console
vm-cluster
├── etc
│   ├── cluster.properties
│   ├── cache.xml
│   ├── gemfire.properites
│   ├── log4j2.properties
│   └── prometheus.yml
├── lib
├── log
├── plugins
├── run
│   └── vm-cluster-ubuntu1-01
└── setenv.sh
```

### `vm-cluster/etc/cluster.properties`

In the `vm-cluster` directory, you will find `etc/cluster.properties` which defines cluster-level properties. Let's edit this file to configure password-less `ssh` login and include **additional VMs** that can be part of the `vm-cluster` cluster.

```console
# Browse the cluster properties and change them as needed. 
# Pay attention to the following property:
#   vm.privateKeyFile=
switch_cluster vm-cluster
vi etc/cluster.properties

# Set the private key file path. This property must be commented out
# if a private key file is not required.
vm.privateKeyFile=~/padogrid/foo-pk.pem

# Include locator VM host names. Host names (or IP addresses) must be
# comma separated with no spaces. Spaces are not supported.
vm.locator.hosts=ubuntu4,ubuntu5

# Include additional member VM host names. Host names (or IP addresses) must be
# comma separated with no spaces. Spaces are not supported.
vm.hosts=ubuntu1,ubuntu2,ubuntu3

# If VMs are multi-homed then you may need to set bind-address and hostname-for-clients
# for each VM. The property name format is 'vm.<host>.bindAddress' and vm.<host>.hostnameForClients.
vm.ubuntu1.bindAddress=ubuntu1
vm.ubuntu1.hostnameForClients=ubuntu1
vm.ubuntu2.bindAddress=ubuntu2
vm.ubuntu2.hostnameForClients=ubuntu2
vm.ubuntu3.bindAddress=ubuntu3
vm.ubuntu3.hostnameForClients=ubuntu3
vm.ubuntu4.bindAddress=ubuntu4
vm.ubuntu4.hostnameForClients=ubuntu4
vm.ubuntu5.bindAddress=ubuntu5
vm.ubuntu5.hostnameForClients=ubuntu5
```

### `vm-cluster/etc/cache.xml`

You can also update the `cache.xml` configuration file at this time as needed.

```console
# Update cache.xml as needed.
vi $GEODE_ADDON_WORKSPACE/clusters/vm-cluster/etc/cache.xml
```

## Deploy PadoGrid

You can repeat the above steps for all other VMs. That could be a lot of work. No worries. PadoGrid is equipped with several utility commands that can help you with such repetitive tasks. For our demo, we'll create a tarball of the entire `~/padogrid` directory and deploy it to all the VMs with a couple of commands.

```console
# Change directory to the your home directory.
cd

# Create a tarball of the entire Geode directory
tar czf Geode.tar.gz Geode/

# Copy the tarball to all VMs listed in the 'cluster.properties' file
vm_copy Geode.tar.gz

# Untar the tarball in all other VMs
vm_exec tar -C ~/ -xzf ~/padogrid.tar.gz

# Update .bashrc in all other VMs (Optional)
vm_exec "echo \". ~/padogrid/workspaces/myrwe/initenv.sh\" >> ~/.bashrc"
```

You now have JDK, Geode, PadoGrid, and the `ws-vm` workspace installed on all the hosts set by the `vm.locastor.hosts` and `vm.hosts` properties in the `etc/cluster.properties` file.

## Add Locators and Members

You now need to add locators by executing `add_locator -all`, which adds one locator to each locator VM. Similarly, the `add_member -all` adds one member to each member VM. 

:exclamation: For VM clusters, `add_member` adds a member to a VM only if the VM does not have a member already added. Remember, PadoGrid supports only one member per VM per cluster. On the other hand, for non-VM clusters, `add_member` indiscriminately adds a new member regardless of the member count.

```console
# Add a locator to each locator VM
add_locator -all

# Add a member to each member VM
add_member -all
```

## Deploy Changes

Any changes you made can be easily deployed to all the VMs specified in the `cluster.properties` file by  running the `vm_copy` command. The `vm_copy` command copies the specified file or directory to the same path in all the locator and member VMs. For example, if you made any changes in the `vm-cluster` directory, you can deploy the entire directory by executing the following:

```console
# Copy by absolute path
vm_copy $GEODE_ADDON_WORKSPACE/clusters/vm-cluster

# Or change directory to vm-cluster and copy from there
cd_cluster
vm_copy .
```

## About `vm_copy`

The`vm_copy` command is a powerful command for applying your changes to all of the locator and member VMs. It is likely that you will often be reconfiguring Geode clusters or updating application library files. As with all other `geode-adddon` commands, by default, `vm_copy` is bound to the cluster settings in the `etc/cluster.properties` file. If you need to apply changes to the VMs other than the ones specified in the `cluster.properties` file, then you can use the `-vm` option to list the VMs to which the changes are to be applied. For example, if you need to include additional VMs to the cluster, you can install the tarball you created earlier using the `-vm` option as follows:

```console
# Copy the tarbarll to 2 additional VMs.
vm_copy -vm ubuntu4,ubuntu5 Geode.tar.gz

# Untar it on the 2 VMs.
vm_exec -vm ubuntu4,ubuntu5 tar -C ~/ -xzf ~/padogrid.tar.gz
```

Continuing with the example, to include the new VMs in the cluster, you would update the `etc/cluster.properties` file and once again use the `vm_copy` command to apply the changes to all the VMs including the ones you just added.

```console
vi $GEODE_ADDON_WORKSPACE/clusters/vm-cluster/etc/cluster.properties

# Include additional locator host names. Host names (or IP addresses) must be
# comma separated with no spaces. Spaces are not supported.
vm.locator.hosts=ubunt1,ubuntu2

# Include additional VM host names. Host names (or IP addresses) must be
# comma separated with no spaces. Spaces are not supported.
vm.hosts=ubuntu1,ubuntu2,ubuntu3,ubuntu4,ubuntu5

# Apply changes to all the VMs except for this VM.
vm_copy $GEODE_ADDON_WORKSPACE/clusters/vm-cluster/etc/cluster.properties
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

## Shutdown/Stop/Kill Cluster

```console
# Gracefuly shutdown cluster including locators. This may take a while.
shutdown_cluster -all

# Stop cluster including locators. Stops each locator/member one at a time.
stop_cluster -all

# Kill cluster including locators. kill -9 on all locators and members.
kill_cluster -all
```

## Tear Down

If you want to remove the cluster from all the VMs, then you must first stop the cluster and execute the `remove_cluster` command. The workspace can be removed using the `vm_exec -all` command. 

```console
# Stop cluster
stop_cluster -all

# Remove cluster. Unlike other commands, this command requires the '-cluster' option.
remove_cluster -cluster vm-cluster

# Remove workspace from all VMs. The '-all' option includes the VM where you 
# are executing this command. Note that we specify the host names 
# since we have removed the vm-cluster. 
vm_exec -all -vm ubuntu1,ubuntu2,ubuntu3,ubuntu4,ubuntu5 rm -rf $GEODE_ADDON_WORKSPACE

# Of course, we can also remove the entire workspaces directory.
vm_exec -all -vm ubuntu1,ubuntu2,ubuntu3,ubuntu4,ubuntu5 rm -rf ~/padogrid/workspaces/myrwe
```
