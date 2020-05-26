# Hazelcast on Minishift/CDK

This directory contains Kubernetes configuration files for deploying Hazelcast on **Minishift**.

## WSL Users

To run `minishift` and `oc` run on WSL, please follow the instructions in the [Tips](#Tips) section at the bottom of this article.

## Creating Workspace

For this tutorial, let's create a new workspace named `ws-minishift` with **Hazelcast 4.x**. The template files included are configured to download the latest release of Hazelcast. You can change the version number as needed in the template files (See `etc/hazelcast/`.)

```bash
create_workspace -name ws-minishift
```

Upon completion of creating the workspace, switch into the workspace.

```bash
switch_workspace ws-minishift
```

We will be using the `$PADOGRID_WORKSPACE` environment variable set by `switch_workspace` in the subsequent sections. You can check its value as follows:

```bash
echo $PADOGRID_WORKSPACE
/Users/dpark/Padogrid/workspaces/myrwe/ws-minishift
```

## Required Software

Before we begin, we must first install the following software. See the [References](#References) section for URLs.

- VirtualBox (for Windows, VirtualBox or Hyper-V)
- minishift (or CDK)
- docker (for Windows, Docker Toolbox if VirutalBox, Docker Desktop if Hyper-V)
- oc

## Creating Kubernetes Environment

In your workspace, create a Kubernetes environment in which we will setup Hazelcast deployment files.

```bash
create_k8s -k8s minishift -cluster minishift_test

# Upon creation, source in the 'setenv.sh' file as follows.
. $PADOGRID_WORKSPACE/k8s/minishift_test/bin_sh/setenv.sh
```

We will be using the `$HAZELCAST_OPENSHIFT_DIR` environment variable set by `setenv.sh` in the subsequent sections.

## Quick Start

### 1. Start Minishift VM

If you are using Windows Hyper-V, then replace `--vm-driver=virtualbox` with `--vm-driver=hyperv`.

```bash
# Start minishift with 5Gi and 4 CPUs.
# If you are using Hyper-V (You can find the external virtual switch from
# the Hyper-V Manager console):
minishift start --memory=5120 --cpus=4 --hyperv-virtual-switch "your-external-virtual-switch"

# If you are using VirtualBox:
minishift start --memory=5120 --cpus=4 --vm-driver virtualbox
```

### 2. Create shared directory

Login to the Minishift host and create a directory where we will upload addon jar files. We'll be mounting `/data/custom` as a persistent volume later.

```bash
# Login to minishft host and create directory to be mounted as a persistent volume.
minishift ssh
sudo mkdir -p /var/lib/minishift/pv/data/custom/etc
sudo mkdir -p /var/lib/minishift/pv/data/custom/plugins/v1
sudo chmod -R 777 /var/lib/minishift/pv/data

# Minishift runs on a RHEL base with SELinux which restricts
# a container from writing directly to the host file system. The following
# allows the Hazelcast container to write to the directory created
# for the persistent volume.
sudo chcon -R -t svirt_sandbox_file_t /var/lib/minishift/pv/*
sudo restorecon -R /var/lib/minishift/pv/

# Change password to docker
sudo passwd docker
exit
```

### 3. Set `minishift` host name

Let's add the Minishift IP address to the `/etc/hosts` file for convenience. The Kubernetes configuration files included in PadoGrid use the host name `minishift`.

**Linux/macOS:**

```bash
sudo echo "$(minishift ip)	minishift" >> /etc/hosts
```

**Windows:**

If you are using WSL, you should also add the `minishift` host name in the Windows `hosts` file.

```dos
minishift.exe ip
```

**Output:**
```console
192.168.1.38
```

Edit the `hosts` file:
```dos
REM Edit the hosts file and add the minishift host name
notepad C:\Windows\System32\drivers\etc\hosts
```

Add the following in the `hosts` file:

```console
192.168.1.38	minishift
```

### 4. Upload PadoGrid addon jar files to the shared directory

With the `minishift` host name in place, you can now use it to login to the Minishift VM. Let's upload the required binary files to the host OS file system which we'll be mounting as a persistent volume shortly.

```bash
cd_k8s minishift_test

# Upload the hazelcast.xml file to the minishift host.
scp etc/hazelcast.xml docker@minishift:/var/lib/minishift/pv/data/custom/etc/

# Upload addon jar files to the minishift host.
# IMPORTANT: Upload v4/* for Hazelcast 4.x. This tutorial is for 4.x.

# Hazelcast 4.x:
scp -r $PADOGRID_HOME/lib/*  \
$PADOGRID_HOME/hazelcast/lib/v4/* \
$PADOGRID_HOME/hazelcast/plugins/v4/* \
docker@minishift:/var/lib/minishift/pv/data/custom/plugins/v1/
```

### 5. Create Docker registry secret

To download the Hazelcast images from the RedHat Registry (registry.connect.redhat.com), you must create a Docker registry secret using your RedHat account, otherwise you will get an "unauthorized" error during the image download time.

```bash
# Create rhcc docker registry secret required for downloading
# the Hazelcast images from the RedHat registry.
oc create secret docker-registry rhcc \
   --docker-server=registry.connect.redhat.com \
   --docker-username=<red_hat_username> \
   --docker-password=<red_hat_password> \
   --docker-email=<red_hat_email>

# Link the rhcc secret to default.
oc secrets link default rhcc --for=pull
```

### 6. Deploy and start containers

You are now ready to deploy and start containers.

```console
# You can remain logged in as developer or login as administrator.
oc login -u system:admin

# Change directory to etc/ where the Kubernetes configuration files are located.
cd $HAZELCAST_OPENSHIFT_DIR/etc/hazelcast
```

Start Hazelcast cluster by executing one of the three (3) commands listed below. For Hazelcat Enterprise, the `IMDG_LICENSE_KEY` envrionment variable should have already been set with the license key you entered when you were creating the workspace.

#### Hazelcast Enterprise Image from RedHat Registry (registry.connect.redhat.com)

```bash
oc new-app -f hazelcast-enterprise-rhel.yaml \
  -p NAMESPACE=$(oc project -q) \
  -p ENTERPRISE_LICENSE_KEY=$IMDG_LICENSE_KEY
```

#### Hazelcast Enterprise Image from Docker Registry (docker.io)

```bash
oc new-app -f hazelcast-enterprise.yaml \
  -p NAMESPACE=$(oc project -q) \
  -p ENTERPRISE_LICENSE_KEY=$IMDG_LICENSE_KEY
```

#### Hazelcast Open Source Image from Docker Registry (docker.io)

Note that the `hazelcast-oss.yaml` template does not include the Mangement Center.

```bash
oc new-app -f hazelcast-oss.yaml \
  -p NAMESPACE=$(oc project -q)
```

## Directory Overview

The `etc/hazelcast` directory contains the OpenShift template file, `hazelcast.yaml` for launching a Hazelcast cluster.

```console
minishift_test
├── README-Minishift.md
├── bin_sh
│   └── setenv.sh
└── etc
    ├── hazelcast
    │   └── hazelcast.yaml
    └── hazelcast.xml
```

## Monitoring Kubernetes

### Dashboard

Start the Kubernetes dashboard by running the following command. It will automatically launch the browser.

```console
minishift console
```

OpenShift Container Platform login:

```console
user name: admin
password: admin
```

### Management Center

Expose the Hazecast and Management Center services. Management Center is for Hazelcast Enterprise only.

```bash
# Expose the Hazelcast and Management Center services.
oc expose svc/hazelcast-service
oc expose svc/management-center-service

# Get the Hazelcast and Management Center host names.
oc get route
```

**Output:**

Your output should look similar to the following.

```console
NAME                        HOST/PORT                                                 PATH      SERVICES                    PORT      TERMINATION   WILDCARD
hazelcast-service           hazelcast-service-myproject.192.168.1.38.nip.io                     hazelcast-service           5701                    None
management-center-service   management-center-service-myproject.192.168.1.38.nip.io             management-center-service   8080                    None
```

Include `hazelcast-mancenter` in the URL as follows:

**URL:** http://management-center-service-default.192.168.1.38.nip.io/hazelcast-mancenter

## Running Client Applications

### `perf_test`

Create the default perf_test app.

```bash
create_app
cd_app perf_test
```

To connect to the Hazelcast cluster from an external client, you must disable SmartRouting and directly connect to a single member. This is not ideal as it puts all of the client load on the connected member. Minishift does not support external IPs which are required in order to enable 
Routing.

Edit the `hazelcast-client.xml` configuration file.

```bash
vi etc/hazelcast-client.xml
```

Replace the `network` element with the following (service port `30000` is exposed):

```xml
<!-- $PADOGRID_WORKSPACE/apps/perf_test/etc/hazelcast-client.xml -->
   <network>
      <smart-routing>false</smart-routing>
      <cluster-members>
         <address>hazelcast-service-myproject.192.168.1.38.nip.io:30000</address>
      </cluster-members>
   </network>
```

Execute `test_ingestion` to ingest data.

```bash
cd_app perf_test; cd bin_sh
./test_ingestion -run
```

From the Management Center, you should see two (2) maps, `eligibility` and `profile` being populated.

## Tearing Down

Execute the following:

```console
# To view all resource objects:
oc get all --selector app=hazelcast -o name

# To delete all resource objects:
oc delete all,configmap,pvc,serviceaccount,rolebinding --selector app=hazelcast -l template=hazelcast-openshift-template
oc delete route,svc --all
oc delete pv,pvc -l app=hazelcast

# Stop/delete the minishift VM.
minishift stop
minishift delete
```

## Tips

### 1. How do I run Minishift on WSL?

To run this tutorial on WSL, add the following in the `~/.bashrc` file.

```bash
# In .bashrc or .bash_profile
alias minishift='path_to_minishift.exe'
alias oc="$(wslpath -a $(minishift oc-env | grep -oP "(?<=PATH=)[^;]+"))/oc.exe"
# Audo-completion
. ~/.oc_completion.bash
. ~/.minishift_completion.bash
```

Note that you can also download a Linux version of `oc` from the following URL.

**URL:** [https://www.okd.io/download.html](https://www.okd.io/download.html)

Create auto-completion files as follows.

```bash
minishift completion bash > ~/.minishift_completion.bash
oc completion bash > ~/.oc_completion.bash
```

### 2. I can't start the minishift dashboard. I'm getting the following error message:
```console
X Unable to enable dashboard: decode C:\Users\<user>\.minishift\config\config.json: EOF
```
**Solution:** Delete the minishift, config.json file, and restart minishift

```dos
minishift delete
erase C:\Users\<user>\.minishift\config\config.json
minishift start --extra-config=kubelet.authentication-token-webhook=true --memory=5120 --cpus=4 --vm-driver=virtualbox
```

## References

1. Download VirtualBox, [https://www.virtualbox.org/wiki/Downloads](https://www.virtualbox.org/wiki/Downloads).
2. Download CDK (Container Development Kit, aka, Minishift)[https://developers.redhat.com/products/cdk/download](https://developers.redhat.com/products/cdk/download)
3. Download `oc` Client Tools, [https://www.okd.io/download.html](https://www.okd.io/download.html).
4. Install Docker, [https://docs.docker.com/install/](https://docs.docker.com/install/).
