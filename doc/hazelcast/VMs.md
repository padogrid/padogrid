# Running hazelcast-addon on VMs and PMs

PadoGrid provides a simple way to manage Hazelcast clusters on laptop, Vagrant pods, Kubernetes, and VMs. This article describes how to create and run a Hazelcast cluster on multiple VMs and/or PMs (Physical Machines). Instructions provided apply to any VMs and PMs including AWS, Azure, GCP, physical machines, VirtualBox, vSphere, etc.

With PadoGrid, you can have a complete control over Hazelcast running on VMs from your local machine. `hazelast-addon` preserves the state of VM configurations and deployments on your laptop so that you can conveniently activate or deactivate the VM environment at any time. After you are done running VMs, you can tear them down and reinstate the very same environment later with a single command. This saves you time and reduces VM costs when you are working in a cloud environment, for example. 

## Installation Steps

There are four (4) main steps to installing PadoGrid on VMs. Once these steps are performed, you can then synchronize your local machine with the remote VMs by simply executing the `vm_sync` command, which enables the local control of VMs.

1. [Setup password-less SSH login](#Password-less-SSH-Login)
2. [Download required software](#Download-Software)
3. [Create VM workspace](#Create-VM-Workspace)
4. [Synchronize VM Workspace](#Synchronize-VM-Workspace)

## Password-less SSH Login

The first step is to check to make sure you are able to login to the VMs that you want to cluster without the password. For AWS EC2, for example, this is already done for you so you can skip this section.

If you need to manually setup password-less SSH login, then follow the [instructions here](/doc/password-less-ssh-login.md).

## Download Software

PadoGrid requires the following software. If they are not installed on VMs, then download their tarball or zip distributions.

- JDK
- Hazelcast

For our demo, we'll assume they are downloaded in the following directory. Note that they must be tarball or zip distributions.

```console
/home/dpark/Downloads
├── jdk1.8.0_212.tar.gz
└── hazelcast-enterprise-3.12.6.tar.gz
```

## Create VM Workspace

Create a VM workspace on your local machine, e.g., your laptop.

```console
create_workspace -vm -name ws-vm
```

The above command interactively prompts for software installation paths information. The `-vm` option enables VMs and `-name` option names the workspace. The following shows my laptop environment. The environment variables that begin with `VM_` are specific to the remote VMs. All other environment variables without the prefix `VM_` pertain to your laptop environment. For example, `VM_HAZELCAST_ADDON_HOME` is the PadoGrid installation path in the VM hosts you specified.

```console
Please answer the prompts that appear below. If you are not able to complete
the prompts at this time then use the '-quiet' option to bypass the prompts.
You can complete the requested values later in the generated 'setenv.sh' file
You can abort this command at any time by entering 'Ctrl-C'.

Enter Java home path.
[/home/dpark/padogrid/products/jdk1.8.0_212]:

Enter Hazelcast (IMDG or Jet) home directory path. Choose one
from the defaults listed below or enter another.
   /home/dpark/padogrid/products/hazelcast-enterprise-3.12.6
   /home/dpark/padogrid/products/hazelcast-jet-enterprise-3.2.2
[/home/dpark/padogrid/products/hazelcast-enterprise-3.12.6]:
/home/dpark/padogrid/products/hazelcast-enterprise-4.0
Enter workspace name.
[ws-vm]:

Enter default cluster name.
[myhz]:

Enable VM? Enter 'true' or 'false' [true]:
Enter VM JDK home path.
[/home/dpark/padogrid/products/jdk1.8.0_212]:
/home/dpark/padogrid/products/jdk1.8.0_212
Enter VM Hazelcast home path.
[/home/dpark/padogrid/products/hazelcast-enterprise-4.0]:
/home/dpark/padogrid/products/hazelcast-enterprise-4.0
Enter VM hazelcast-addon home path.
[/home/dpark/Work/products/hazelcast-addon_0.9.0-SNAPSHOT]:
/home/dpark/padogrid/products/hazelcast-addon_0.9.0-SNAPSHOT
Enter VM workspaces path.
[/home/dpark/Work/padogrid/workspaces/rwe-wsl]:
/home/dpark/padogrid/workspaces/rwe
Enter VM host names or IP addresses separated by comma.
[]:
ubuntu1,ubuntu2,ubuntu3
Enter VM user name.
[]: dpark
Enter VM SSH private key path. If you don't have the key file (.pem) then
leave it blank for now. You can place the file in the workspace directory or
set the path in the workspace 'setenv.sh' file later.
[]:

You have entered the following.
                            JAVA_HOME: /home/dpark/padogrid/products/jdk1.8.0_212
                       HAZELCAST_HOME: /home/dpark/padogrid/products/hazelcast-enterprise-4.0
            HAZELCAST_ADDON_WORKSPACE: /home/dpark/Work/padogrid/workspaces/rwe-wsl/ws-vm
                      Default Cluster: myhz
                           VM_ENABLED: true
                         VM_JAVA_HOME: /home/dpark/padogrid/products/jdk1.8.0_212
                    VM_HAZELCAST_HOME: /home/dpark/padogrid/products/hazelcast-enterprise-4.0
              VM_HAZELCAST_ADDON_HOME: /home/dpark/padogrid/products/hazelcast-addon_0.9.0-SNAPSHOT
   VM_HAZELCAST_ADDON_WORKSPACES_HOME: /home/dpark/padogrid/workspaces/rwe
         VM_HAZELCAST_ADDON_WORKSPACE: /home/dpark/padogrid/products/workspace/ws-vm
                             VM_HOSTS: ubuntu1,ubuntu2,ubuntu3
                              VM_USER: dpark
                  VM_PRIVATE_KEY_FILE: 
Enter 'c' to continue, 'r' to re-enter, 'q' to quit: c
```

The above example shows that the installation paths in the VMs are different from the local installation paths. We choose to install all the software components in the `/home/dpark/padogrid/products` for the VMs.

If you specify the private key file, i.e., `ecs.pem` in our example, then it is automatically copied to to the newly created workspace directory. It is later deployed to the VMs when you execute the `vm_sync` command.

You can also run the `create_workspace` in the non-interactive mode by specifying the `-quiet` option. The following command produces the same workspace configurations as the above interactive example.

```console
create_workspace -quiet \
-name ws-vm \
-cluster myhz \
-java /home/dpark/padogrid/products/jdk1.8.0_212 \
-hazelcast /home/dpark/padogrid/products/hazelcast-enterprise-3.12.6 \
-vm ubuntu1,ubuntu2,ubuntu3 \
-vm-java /home/dpark/padogrid/products/jdk1.8.0_212 \
-vm-hazelcast /home/dpark/padogrid/products/hazelcast-enterprise-3.12.6 \
-vm-addon /home/dpark/padogrid/products/hazelcast-addon_0.9.0-SNAPSHOT \
-vm-workspaces /home/dpark/padogrid/workspaces/rwe \
-vm-user dpark
```

## SSH Private Key

If a private key file is required and you have not specified in the previous example, then you can place it in the workspace directory. PadoGrid automatically picks up the first `.pem` file found in the workspace directory if the `VM_PRIVATE_KEY_FILE` environment variable is not set in the `setenv.sh` file. The following shows the contents of the `ws-vm` workspace directory we created.

```console
# Example: place your AWS SSH private key file (foo.pem) in the ws-vm workspace.
switch_workspace ws-vm
tree -L 1 .
.
├── apps
├── bundles
├── clusters
├── ecs.pem
├── initenv.sh
├── lib
├── plugins
├── pods
└── setenv.sh
```

## Test VM Workspace

Before you sync the local workspace with VMs, which may initially take some time to complete if you need to install software, we should first test the environment to make sure SSH works properly. This is done by executing `vm_test`.

```console
vm_test
```

`vm_test` outputs the following:

```console
------------------------------------------------------------------------------------------
Workspace: ws-vm

Environment:
                  VM_ENABLED=true
                    VM_HOSTS=ubuntu1,ubuntu2,ubuntu3
                     VM_USER=dpark
         VM_PRIVATE_KEY_FILE=
                VM_JAVA_HOME=/home/dpark/padogrid/products/jdk1.8.0_212
           VM_HAZELCAST_HOME=/home/dpark/padogrid/products/hazelcast-enterprise-3.12.6
     VM_HAZELCAST_ADDON_HOME=/home/dpark/padogrid/products/hazelcast-addon_0.9.0-SNAPSHOT
VM_HAZELCAST_ADDON_WORKSPACE=/home/dpark/padogrid/workspaces/rwe/ws-vm
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
Network Test:
   ubuntu1 - OK
   ubuntu2 - OK
   ubuntu3 - OK
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
WARNING:
/home/dpark/padogrid/products/jdk1.8.0_212
   Java is not installed on the following VMs. The workspace will not be operational
   until you install Java on these VMs.
       ubuntu1

   To install Java on the above VMs, download the correct version of Java and execute 'vm_install'.    
   Example:
      vm_install -java jdk1.8.0_212.tar.gz
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
WARNING:
/home/dpark/padogrid/products/hazelcast-enterprise-3.12.6
   Hazelcast is not installed on the following VMs. The workspace will not be operational
   until you install Hazelcast on these VMs.
       ubuntu1 ubuntu2 ubuntu3

   To install Hazelcast on the above VMs, download the correct version of Hazelcast and
   execute 'vm_install'.

   Example:
      vm_install -hazelcast hazelcast-enterprise-3.12.6.tar.gz
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
Cluster: myhz

Cluster Properties File:
   /c/Users/dpark/Work/padogrid/workspaces/rwe-wsl/ws-vm/clusters/myhz/etc/cluster.properties

Environment:
          vm.enabled=true
            vm.hosts=ubuntu1,ubuntu2,ubuntu3
             vm.user=dpark
   vm.privateKeyFile=

The cluster VM environment is identical to the workspace VM environment.
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
Summary:
   One or more VM issues found. Please correct them before executing 'vm_sync'.

Workspace Issues:
   None.
Network Issues:
   None.
Software Issues:
   Java missing from the following VMs. Install Java with 'vm_install -java'.
      ubuntu1
   Hazelcast missing from the following VMs. Install Hazelcast with 'vm_install -hazelcast'.
      ubuntu1 ubuntu2 ubuntu3
Cluster Issues:
   None.
------------------------------------------------------------------------------------------
```

`vm_test` scans all the VMs and reports any issues it encounters. For example, the above`vm_test` report shows that SSH sessions are working fine but Java is not installed on `ubuntu1` and Hazelcast is not installed on all of the VMs. To install Java and Hazelcast on the VMs, we execute the `vm_install` command as follows.

```console
vm_install -java /home/dpark/Downloads/jdk-8u212-linux-x64.tar.gz \
-hazelcast /home/dpark/Downloads/hazelcast-enterprise-3.12.6.tar.gz
```

`vm_install` outputs the following:

```console
jdk-8u212-linux-x64.tar.gz
   Installing ubuntu1...
   Java installation complete.

hazelcast-enterprise-3.12.6.tar.gz
   Installing ubuntu1...
   Installing ubuntu2...
   Installing ubuntu3...
   Hazelcast installation complete.

Run 'vm_test' to check installation and configuration status.
```

If you run `vm_test` after installing Java and Hazelcast, you should see no issues in the VM workspace. You can now proceed to sync the VM workspace.

## Synchronize VM Workspace

In the previous section, we have locally created and tested a VM workspace. Before we can use it on the remote VMs, we need to synchronize it with the VMs. To do so, we execute the `vm_sync` command, which synchronizes the workspace you just created and automatically installs PadoGrid on all the VMs if it is not already installed.

```console
vm_sync
```

`vm_sync` outputs the following:

```console
Workspace sync: ws-vm
   Synchronizing ubuntu1...
   Synchronizing ubuntu2...
   Synchronizing ubuntu3...
Workspace sync complete.
```

## Configure Hazelcast Cluster

In our example, we have created the default cluster named, `myhz`. Let's switch into the `myhz` cluster.

```console
switch_cluster myhz
```

The cluster directory has the following files.

```console
myhz
├── bin_sh
│   └── setenv.sh
├── etc
│   ├── cluster.properties
│   ├── hazelcast.xml
│   ├── hibernate.cfg-mysql.xml
│   ├── hibernate.cfg-postgresql.xml
│   ├── log4j2.properties
│   └── prometheus.yml
├── lib
├── log
├── plugins
└── run
```

### `etc/cluster.properties`

In the `myhz` directory, you will find `etc/cluster.properties` which defines cluster-level properties. Let's edit this file to set the Management Center host (`mc.host`) to `ubuntu3` and the cluster VMs (`vm.hosts`) to `ubuntu1,ubuntu2`.

```console
# Browse the cluster properties and change them as needed. 
# Pay attention to the following two (2) properties:
#   mc.host=
#   vm.privateKeyFile=
vi etc/cluster.properties

# Set Management Center host
mc.host=ubuntu3

# Set the private key file path. If this property is set then it overwrites the
# VM_PRIVATE_KEY_FILE  environment variable set in the workspace's setenv.sh file. 
#vm.privateKeyFile=~/padogrid/products/Workspaces/ecs.pem

# Include additional VM host names. Host names (or IP addresses) must be
# comma separated with no spaces. Spaces are not supported.
vm.hosts=ubuntu1,ubuntu2
```

### `etc/hazelcast.xml`

You can also update the `hazelcast.xml` file at this time as needed.

```console
vi etc/hazelcast.xml
```

## Sync Changes

Any changes you made can be easily deployed to all the VMs specified by the `VM_HOSTS` environment variable by running the `vm_sync` command, which copies the entire workspace directory to the VMs. This can take some time to complete if you have many VMs and large binary files in the workspace.

After the VMs have been synchronized, if you make changes to only a few files, then instead of executing `vm_sync` again, you can execute `vm_copy` which copies only the specified file or directory to the VMs.

```console
switch_cluster myhz

# To copy a single file
vm_copy etc/cluster.proerties

# To copy the entire directory
vm_copy .
```

### `vm_sync` vs `vm_copy`

The main difference between `vm_sync` and `vm_copy` is that `vm_sync` copies the entire workspace and `vm_copy` copies only the specified file or directory. Both commands allow you to apply the changes you made to all of the VMs. As you might have guessed, `vm_sync` will take longer to complete since it copies the entire workspace, but it is more convenient to use than `vm_copy` since you don't have to remember the files you modified. These commands will quickly become your favorite commands as it is likely that you will often be reconfiguring Hazelcast clusters or updating application library files.

:exclamation: Note that `vm_copy` by default only copies the files that are in the workspace directory hierarchy. If you try to copy a file that is outside of the workspace directory then it will fail and output an error message. If you need to copy non-workspace files, specify the `-mirror` option, which copies the absolute file path to the same absolute file path in the remote VMs.

## Start Cluster

You are now ready to start the `myhyz` cluster.

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
```

## Start Management Center

To start the Management Center on `ubuntu3` as we configured

```console
start_mc
show_log -log mc
```

**URL:** [http://ubuntu3:8080/hazelcast-mancenter](ubuntu3:8080/hazelcast-mancenter)

## Stop/Kill Cluster

```console
stop_cluster
kill_cluster
```

## Stop Management Center

```console
stop_mc
```

## Running from VMs

You can also run VM workspaces from any of the VMs. As with the local environment, you must first setup the PadoGrid environment on the VM in which you want to run workspaces by sourcing in the workspaces `initenv.sh` file.

```console
# SSH into one of the VMs
ssh ubuntu1

# Source in intienv.sh
. ~/padogrid/workspaces/rwe/initenv.sh

# Optionally add the above line in .bashrc so that hazelcast-addon is
# automatically initialized when you login next time.
echo ". ~/padogrid/workspaces/rwe/initenv.sh" >> ~/.bashrc

# Once you have initenv.sh souced in you can then execute any of the PadoGrid
# commands as before. The following starts the `myhz` cluster.
switch_workspace ws-vm
swtich_cluster myhz
start_cluster
```

## Tear Down

If you want to remove the cluster from all the VMs, then you must first stop the cluster and execute the `remove_cluster` command. The workspace can be removed using the `vm_exec -all` command. 

```console
# Stop cluster and management center
stop_cluster
stop_mc

# Remove cluster. Unlike other commands, this command requires the '-cluster' option.
remove_cluster -cluster myhz

# Simulate removing workspace from all VMs. Displays removal steps but does not
# actually remove the workspace.
remove_workspace -workspace ws-vm -simulate

# Remove workspace from all VMs. Runs in interactive mode.
remove_workspace -workspace ws-vm
```

## References

1. [Workspaces on AWS EC2 Instances](Workspaces-on-AWS-EC2-Instances.md)
2. [Reactivating Workspaces on AWS EC2](Reactivating-Workspaces-on-AWS-EC2.md)
