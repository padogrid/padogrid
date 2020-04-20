# Hazelcast on GKE using `kustomize`

This directory contains Kubernetes configuration files for deploying Hazelcast, Prometheus, custom metrics API, and Horizontal Pod Autoscaler (HPA) on **GKE**. The configuration files are to be used with the `kustomize` or `kubectl apply -k` command.

## Initializing Workspace

To follow instructions in this article, you must first install `PADOGRID` and create a workspace. For example, the following creates the `ws-gke` workspace in the `~/padogrid/workspaces/myrwe` directory. Make sure to source in the `initenv.sh` file, which sets the required environment variables that are specific to the workspace you created.

```console
mkdir -p ~/padogrid/workspaces/myrwe
tar -C ~/padogrid/products/ -xzf padogrid_0.9.1
~/padogrid/products/padogrid_0.9.1/bin_sh/create_workspace -workspace ~/padogrid/workspaces/myrwe/ws-gke
. ~/padogrid/workspaces/myrwe/ws-gke/initenv.sh
```

We will be using the `$PADOGRID_WORKSPACE` environment variable set by `initenv.sh` throughout this article.

```console
echo $PADOGRID_WORKSPACE 
/Users/dpark/padogrid/workspaces/myrwe/ws-gke
```

:exclamation: If you have built PadoGrid from Windows and the commands fail due to the Windows line break issue, then you must convert the next line characters using the `dos2linux` command. Make sure to convert all files including the hidden files as follows:

```
dos2unix ~/Hazelcast/padogrid_0.9.1/hazelcast/bin_sh/*
dos2unix ~/Hazelcast/padogrid_0.9.1/hazelcast/bin_sh/.*sh
dos2unix ~/Hazelcast/padogrid_0.9.1/apps/k8s/kustom/bin_sh/*
dos2unix ~/Hazelcast/padogrid_0.9.1/apps/k8s/kustom/bin_sh/.*sh
```

## Required Software List

Before you begin, you must first install the following software. See the [References](#References) section for URIs.

- Docker
- openssl (most operating systems have this already installed)
- gcloud
- kubectl
- kustomize (optional - `kubectl apply -k` equivalent to `kustomize` )
- jq (optional)
- watch (optional)

## Creating Kubernetes Environment

In your workspace, create a Kubernetes environment in which we will setup Hazelcast deployment files as follows:

```console
create_k8s -k8s gke -cluster kustomize-test
```

The above command creates the following directory with Hazelcast kustomization files. We will be working in this directory throughout this article.

```console
kustomize-test
├── README-GKE.md
├── bin_sh
│   ├── create_certs
│   └── setenv.sh
└── etc
    ├── custom-metrics
    │   ├── base
    │   ├── custom-metrics-api
    │   ├── metrics-server
    │   ├── overlay-base
    │   └── prometheus
    └── hazelcast
        ├── base
        ├── init
        ├── overlay-base
        ├── overlay-nfs
        └── storage
```

## Configuring GCP Environment

The configuration and deployment steps described in this article use the following environment variables. If you have them set to your values, then you can execute the commands from each step by simple copy/paste.

| Parameter       | Value                |
| --------------- | -------------------- |
| HAZELCAST_KUSTOM_DIR | $PADOGRID_WORKSPACE/k8s/kustomize-test |
| GCR_HOSTNAME    | gcr.io               |
| PROJECT_ID      | hazelcast-33         |
| CLUSTER_NAME    | kustomize-test       |
| REGION          | us-east1             |
| ZONE            | us-east1-b           |
| INSTANCE_NAME   | gke-kustomize-test-default-pool-d2bd95b0-3jb5 |

Replace the values below with your values in the `$PADOGRID_WORKSPACE/k8s/kustomize-test/bin_sh/setenv.sh` file.

```console
# Edit $PADOGRID_WORKSPACE/k8s/kustomize-test/bin_sh/setenv.sh
vi $PADOGRID_WORKSPACE/k8s/kustomize-test/bin_sh/setenv.sh

# Set the following environment variables with your values.
export HAZELCAST_KUSTOM_DIR=$PADOGRID_WORKSPACE/k8s/kustomize-test
export CLUSTER_NAME=kustomize-test
export GCR_HOSTNAME=gcr.io
export PROJECT_ID=hazelcast-33
export REGION=us-east1
export ZONE=us-east1-b
export INSTANCE_NAME=gke-kustomize-test-default-pool-d2bd95b0-3jb5

# Source in setenv.sh
. $PADOGRID_WORKSPACE/k8s/kustomize-test/bin_sh/setenv.sh
```

We'll use the `$HAZELCAST_KUSTOM_DIR` environment variable set in the `setenv.sh` file in the subsequent sections.

### Configure Default GCP Environment

First, set the default GCP environment as follows:

```console
gcloud config set project $PROJECT_ID
gcloud config set compute/region $REGION
gcloud config set compute/zone $ZONE

# Check the setttings
gcloud info
```

### Upload Hazelcast Images to GCR Registry

The GCR Registry follows the following naming conventions.

```console
[GCR_HOSTNAME]/[PROJECT_ID]/[IMAGE]:[TAG]
```

#### Hazelcast Enterprise

| Parameter    | Value                |
| ------------ | -------------------- |
| IMAGE        | hazelcast-enterprise |
| TAG          | 3.12.1               |

#### Management Center

| Parameter    | Value                |
| ------------ | -------------------- |
| IMAGE        | management-center    |
| TAG          | latest               |

Hazelcast and Management Center must be registered in GCR before we can deploy Hazelcast on GKE.

For GCR authentication methods, see the following link:

[https://cloud.google.com/container-registry/docs/advanced-authentication#gcloud_as_a_docker_credential_helper](https://cloud.google.com/container-registry/docs/advanced-authentication#gcloud_as_a_docker_credential_helper)

```console
# Authenticate to GCR. If this step is not ideal then follow the steps below.
gcloud auth configure-docker

# If the above step is not ideal then execute the following two commands instead:
gcloud components install docker-credential-gcr
docker-credential-gcr configure-docker

# Once you have docker credentials configured, you can 
# push Docker images to GCR.

# Push Hazelcast Enterprise image to GCR
docker pull hazelcast/hazelcast-enterprise:3.12.1
docker tag hazelcast/hazelcast-enterprise:3.12.1 $GCR_HOSTNAME/$PROJECT_ID/hazelcast/hazelcast-enterprise:3.12.1
docker push $GCR_HOSTNAME/$PROJECT_ID/hazelcast/hazelcast-enterprise:3.12.1

# To pull (not required, just an example)
# docker pull $GCR_HOSTNAME/$PROJECT_ID/hazelcast/hazelcast-enterprise:3.12.1

# Man Center to GCR
docker pull hazelcast/management-center:latest
docker tag hazelcast/management-center:latest $GCR_HOSTNAME/$PROJECT_ID/hazelcast/management-center:latest
docker push $GCR_HOSTNAME/$PROJECT_ID/hazelcast/management-center:latest

# To pull (not required, just an example)
# docker pull $GCR_HOSTNAME/$PROJECT_ID/hazelcast/management-center:latest

# To delete Hazelcast Enterprise image
gcloud container images delete $GCR_HOSTNAME/$PROJECT_ID/hazelcast/hazelcast-enterprise:3.12.1 --force-delete-tags

# To delete Man Center image
gcloud container images delete $GCR_HOSTNAME/$PROJECT_ID/hazelcast/management-center:latest --force-delete-tags
```


You can browse the GCR Project Registry. The URI has the format:

```
https://console.cloud.google.com/gcr/images/$PROJECT_ID
```

Using our example,

[https://console.cloud.google.com/gcr/images/hazelcast-33](https://console.cloud.google.com/gcr/images/hazelcast-33)

### Create Kubernetes Cluster

For our demo, we will use the following parameters.

| Parameter       | Value          |
| --------------- | -------------- |
| DEVICE_ID       | sdb            |
| MNT_DIR         | hazelcast      |
| SIZE            | 1 GB           |
| DISK_NAME       | gce-nfs-disk   |

Create a cluster and a disk which we will later mount using Persistent Volume. We'll be uploading application specific library files to the shared disk.

```console
# Create cluster
gcloud container clusters create --zone=$ZONE --disk-type=pd-standard --machine-type=n1-standard-2 $CLUSTER_NAME

# Get authentication credentials
gcloud container clusters get-credentials $CLUSTER_NAME

# Create disk to store application specific library files
# gcloud compute disks create --size=[SIZE] --zone=[ZONE] [DISK_NAME]
gcloud compute disks create --size=1GB --zone=$ZONE gce-nfs-disk

# Verify the disk
gcloud compute disks list | grep gce-nfs-disk
gcloud compute disks describe gce-nfs-disk --zone=$ZONE

# List the VM instances for the cluster you just created (kustomize-test for this demo)
# and set the INSTANCE_NAME environment variable in setenv.sh.
# Make sure to source in setenv.sh afterwards.
gcloud compute instances list | grep $CLUSTER_NAME
vi $HAZELCAST_KUSTOM_DIR/bin_sh/setenv.sh
export INSTANCE_NAME=<a-node-name>
. $HAZELCAST_KUSTOM_DIR/bin_sh/setenv.sh

# Select one of the instances and attach the disk to it
# gcloud compute instances attach-disk --disk [DISK_NAME] [INSTANCE_NAME]
gcloud compute instances attach-disk --disk gce-nfs-disk $INSTANCE_NAME
```

### Format Disk

We need to format the disk we created and create a directory in which we'll upload application specific library files that Hazelcast containers can access via Java `CLASSPATH`.

We do this by attaching the disk to one of the nodes in the cluster and initialize it from there.

```console
# Login to the VM instance (node) that you attached the disk
# gcloud compute ssh --project [PROJECT_ID] --zone [ZONE] [INSTANCE_NAME]
gcloud compute ssh $INSTANCE_NAME

# Display disk information from the $INSTANCE_NAME (node) shell
sudo df -h
sudo lsblk

# Format the disk to ext4 
# sudo mkfs.ext4 -m 0 -F -E lazy_itable_init=0,lazy_journal_init=0,discard /dev/[DEVICE_ID]
sudo mkfs.ext4 -m 0 -F -E lazy_itable_init=0,lazy_journal_init=0,discard /dev/sdb

# Create a directory that serves as the mount point
# sudo mkdir -p /mnt/disks/[MNT_DIR]
sudo mkdir -p /mnt/disks/hazelcast

# Mount the disk
# sudo mount -o discard,defaults /dev/[DEVICE_ID] /mnt/disks/[MNT_DIR]
sudo mount -o discard,defaults /dev/sdb /mnt/disks/hazelcast

# Configure read/write permissions
sudo chmod a+w /mnt/disks/hazelcast

# Create the plugins directory where the application specific library (jar) 
# files will be stored.
mkdir -p /mnt/disks/hazelcast/data/custom/plugins/v1

# Unmount disk
sudo umount /mnt/disks/hazelcast

# Exit from the node shell
exit
```

Upon exiting from the node shell, detach the disk from the node.

```
# Detach disk
gcloud compute instances detach-disk --disk gce-nfs-disk $INSTANCE_NAME
```

### Deploy NFS

Let's now deploy an NFS server that will provide access to the disk we created.

```console
cd $HAZELCAST_KUSTOM_DIR/etc

# Start NFS Server
kubectl apply -k hazelcast/storage/gke/init-nfs

# NFS server address is set to "nfs-server.default.svc.cluster.local".
# This demo uses the "default" namespace and therefore no changes are required.
# Change this value in nfs-pv.yaml only if your namespace is not "default".
vi hazelcast/overlay-nfs/nfs-pv.yaml

# Create the persistent volume and claim for the NFS disk
kubectl apply -k hazelcast/overlay-nfs
```

### Check NFS Status

Run the following to see the NFS server (pod), persistent volume and persistent claim are properly installed.

```console
kubectl get pv
kubectl get pvc
kubectl get pod
```

### Copy Application Library Files to NFS Disk

We can now upload the application library files to the NFS disk which can be accessed by all Hazelcast containers via the persistence volume claim. To do this, we create a pod that uses the persistence volume claim to access the shared NFS disk. 

```console
# First, create a pod to which you will mount the disk
kubectl apply -k hazelcast/storage/gke/nfs-pod

# Copy the PADOGRID jar files 
# (Note: copy to /var/nfs/plugins/v1, NOT /data/custom/plugins/v1)
kubectl cp $PADOGRID_HOME/lib/PADOGRID-core-0.2.0-SNAPSHOT.jar nfs-pod:/var/nfs/plugins/v1/
kubectl cp $PADOGRID_HOME/plugins/PADOGRID-core-0.2.0-SNAPSHOT-tests.jar nfs-pod:/var/nfs/plugins/v1/

# Delete the pod
kubectl delete -k hazelcast/storage/gke/nfs-pod
```

### Create Certificates

To use custom metrics, we need to setup TLS certificates. This is done by running the `bin_sh/create_certs` script which creates and inserts them into the `overlay-base/cm-adapter-serving-certs.yaml` file. Please see this script for details.

```console
# IMPORTANT: First, create TLS certificates for the Prometheus custom metrics API adapter
cd $HAZELCAST_KUSTOM_DIR/bin_sh
./create_certs
```

### About Overlay Files

The following files are in the overlay directories so that you can modify them without altering the original files. You can copy other original files into the overlay directories if you need to modify them. If you do copy additional files, make sure to include them in the `kustomization.yaml` file, also.

| Original Dir  | Overlay Dir            | File                      |
| ------------- | ---------------------- | ------------------------- |
|hazelcast/base | hazelcast/overlay-base | configmap.yaml            |
|hazelcast/base | hazelcast/overlay-base | statefulset.yaml          |
|hazelcast/base | hazelcast/overlay-base | mc-statefulset.yaml       |
|hazelcast/base | hazelcast/overlay-base | hazelcast-hpa-custom.yaml |
|hazelcast/base | hazelcast/overlay-base | hazelcast-hpa-custom.yaml |
|hazelcast/storage/gke/nfs | hazelcast/overlay-nfs | nfs-pv.yaml     |

Some of these overlay files will be modified in the subsequent sections. Their `kustomization.yaml` files are shown below for your reference.

#### overlay-base/kustomization.yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
- ../base

patchesStrategicMerge:
- configmap.yaml
- statefulset.yaml
- mc-statefulset.yaml
- hazelcast-hpa-custom.yaml
```

#### overlay-nfs/kustomization.yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
- ../storage/gke/nfs

patchesStrategicMerge:
- nfs-pv.yaml
```

### Set License Key and Image Names

We could use the default settings of Hazelcast images, but for our demo, we will use the images we uploaded to GCR previously. As mentioned earlier, the GCR image names follow the conventions shown below.

```console
[GCR_HOSTNAME]/[PROJECT_ID]/[IMAGE]:[TAG]
```

For our demo, change the image names of Hazelcast Enterprize and Management Center as follows.

| Parameter      | Value                |
| -------------- | -------------------- |
| GCR_HOSTNAME   | gcr.io               |
| PROJECT_ID     | hazelcast-33         |

#### Hazelcast Enterprise

| Parameter  | Value                |
| ---------- | -------------------- |
| IMAGE      | hazelcast-enterprise |
| TAG        | 3.12.1               |

Make sure to replace `gcr.io` with your `GCR_HOSTNAME` and `hazelcast-33` with your `PROJECT_ID`.

```console
cd $HAZELCAST_KUSTOM_DIR/etc

# Set license key and change Hazelcast Enterprise image name
vi hazelcast/overlay-base/statefulset.yaml

# In statefulset.yaml make the following changes
- name: HZ_LICENSE_KEY
  value: <License key goes here -- done by overlay-base>
image: gcr.io/hazelcast-33/hazelcast/hazelcast-enterprise:3.12.1
```

#### Management Center

| Parameter  | Value                |
| ---------- | -------------------- |
| IMAGE      | management-center    |
| TAG        | latest               |

Make sure to replace `gcr.io` with your `GCR_HOSTNAME` and `hazelcast-33` with your `PROJECT_ID`.

```console
# Change Management Center image name
vi hazelcast/overlay-base/mc-statefulset.yaml

# In mc-statefulset.yaml make the following changes
- name: MC_LICENSE_KEY
  value: <License key goes here -- done by overlay-base>
image: gcr.io/hazelcast-33/hazelcast/management-center:latest
```

### Hazelcast Configuration File

The Hazelcast configuration file, `configmap.yaml` is found in the `hazelcast/base`. This distribution includes a *kustomized* version of that file in the the `hazelcast/overlay-base` directory. It has been preconfigured with `PADOGRID` domain classes and eviction policies to demonstrate the autoscaler. We have already uploaded the jar files that contain the domain classes in the [Copy Application Library Files to NFS Disk](#Copy-Application-Library-Files-to-NFS-Disk) section. You can modify this file as needed to incorporate your own applications.

### Deploy Hazelcast and Custom Metrics

We are now ready to deploy Hazelcast and custom metrics to the GKE cluster. Up until now, we have been installing and configuring the GKE cluster.

```console
# Initialize Kubernetes cluster. This command configures a service account and RBAC.
kubectl apply -k hazelcast/init

# Deploy Hazelcast Enterprise
kubectl apply -k hazelcast/overlay-base

# Deploy custom metrics API and start Prometheus/HPA.
kubectl apply -k custom-metrics/overlay-base
```

### Monitor StatefulSet

You can use the browser (GKE Console) to monitor the pods and services getting started. The URI has the following form:

```
https://console.cloud.google.com/kubernetes/list?project=$PROEJCT_ID
```
For our example,

[https://console.cloud.google.com/kubernetes/list?project=hazelcast-33](https://console.cloud.google.com/kubernetes/list?project=hazelcast-33)


From your terminal, you can also monitor the GKE components as follows:

```console
# default namespace
watch kubectl get statefulsets
watch kubectl get pods

# monitoring namespace
watch kubectl get deployments --namespace=monitoring
```

## Directory Overview

The `bin_sh` directory contains the `create_certs` script for generating the required secret file with TLS certificates. Make sure to run this script first before running Kubernetes.

The `etc` directory contains the entire Kubernetes configuration files. Each sub-directory contains `kustomization.yaml` that includes base directories and resource files for their respective configuration.

The `storage/gke` directory contains storage configuration files that are specific to GKE. These files start an NFS server and creates a persistent volume and claim used by Hazelcast pods for loading application specific configuration and library files. 

The `hazelcast/init` directory contains initialization files that must first be applied before applying `hazelcast/overlay-base` which is described below. These files create a service account and RBAC (Role-Based-Access-Control).

The `hazelcast/base` directory is the base directory that contains all the configuration files for deploying and starting Hazelcast pods.

The `hazelcast/overlay-base` directory contains configuration files that customize or patch the base files. Note that we also copied the `hazelcast-hpa-custom.yaml` file into this directory in [Quick Start](#Quick-Start). You can include additional custom metrics in this file to autoscale Hazelcast pods. The Prometheus custom metrics are defined in `custom-metrics/custom-metrics-api/custom-metrics-config-map.yaml`, which you can also extend to define additional custom metrics.

```console
kustomize-test
├── bin_sh
└── etc
    ├── custom-metrics
    │   ├── base
    │   ├── custom-metrics-api
    │   ├── metrics-server
    │   ├── overlay-base
    │   └── prometheus
    └── hazelcast
        ├── base
        ├── init
        ├── overlay-base
        ├── overlay-nfs
        └── storage
            ├── gke
                ├── init-nfs
                └── nfs
```

## Monitoring Hazelcast


### Prometheus

Prometheus runs in the `monitoring` namespace and has the port number `31190` exposed. 

```console
# Get external addresses of kustomize-test cluster nodes
gcloud compute instances list | grep kustomize-test
```

Use the external IP of a node along with the port number to view Prometheus.

[http://\<node-external-ip\>:31190](http://\<node-external-ip\>:31190 )

### HPA (Horizontal Pod Autoscaler)

You can monitor the HPA using the `watch` command as follows:

```console
# Watch HPA
watch kubectl describe hpa my-release-hazelcast 
```

### Metrics API

You can also invoke the API to monitor any metrics.

```console
# Watch the on_heap-ratio metric
watch -d 'kubectl get --raw "/apis/custom.metrics.k8s.io/v1beta1/namespaces/default/pods/*/on_heap_ratio" |jq'
```

### Hazelcast Management Center

Hazelcast Management Center can be viewed via it load balancer on port 8080.

```console
# Get the Management Center loadbalancer service external IP
kubectl get svc my-release-hazelcast-enterprise-mancenter
```

Use the load balancer service external IP to view the Management Center from your browser:

[http://\<service-external-ip\>:8080/hazelcast-mancenter/](http://\<service-external-ip\>:8080/hazelcast-mancenter/)

## Running Client Applications

To connect to the Hazelcast cluster in GKE, you need to configure the Kubernetes Discovery Service in your client application.

```console
# Get the master URI
kubectl cluster-info

# List the screts
kubectl get secrets
NAME                     TYPE                                  DATA   AGE
default-token-hd5w2      kubernetes.io/service-account-token   3      43m
enterprise-token-2qdzz   kubernetes.io/service-account-token   3      41m

# Use the token name that starts with the prefix "enterprise-token-" to get api-token
# and ca-certificate
# Get api-token
kubectl get secret enterprise-token-2qdzz  -o jsonpath={.data.token} | base64 --decode
# Get ca-certificate
kubectl get secret enterprise-token-2qdzz  -o jsonpath={.data.ca\\.crt} | base64 --decode
```

Enter the master URI, encoded token, and certificate in the `hazelcast-client.xml` file as shown below. Note that the service name is `my-service-lb` which is created when you applied the configuration files. In the next section, we will configure and run the `perf_test` client application with these settings to see the autoscaler in action. 

```xml
<!-- $PADOGRID_WORKSPACE/apps/perf_test/etc/hazelcast-client.xml -->
   <network>
      <smart-routing>true</smart-routing>
      <kubernetes enabled="true">
         <namespace>default</namespace>
         <service-name>my-service-lb</service-name>
         <use-public-ip>true</use-public-ip>
         <kubernetes-master>https://35.229.71.162</kubernetes-master>
         <api-token>
eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImVudGVycHJpc2UtdG9rZW4tMnFkenoiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZW50ZXJwcmlzZSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImNjZWQ5NmUwLTNjNTEtNDQ5Ni04ODhjLWVlZWZmMDRhNTI4YiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmVudGVycHJpc2UifQ.5a7rzmshOASr-5zfCYC3UoxhDUKvXrLMz1aA24vHmM3qhCZVutIsoIiXjFAm8pYCbaRdN7jvNKp9HwcQVcvjF9rjko3rFoUrrc3b2bPd0n_uMio9qtMSvRMLCRUFIpQy1PVvJVzx-CmvdOnd-ZZaHlsJEWAXg4nI1HIua0QaIn43mhmaJYshYUbu3_B396P945dGnqGFq2srBH_I54Oiod6Aq5WZVRt41ipUlR4r5wiED4EshUpE1tJvbrnFJk5sIG1VwS5sbDCEYxYahDWpAEGnXtI_Esxad5KdlBcakZAcfmo28fcjcUWOfMAdtx9QX8WZZ_u65ku2jgb4uVoRVw
         </api-token>
         <ca-certificate>
-----BEGIN CERTIFICATE-----
MIIC5zCCAc+gAwIBAgIBATANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwptaW5p
a3ViZUNBMB4XDTE5MDcxNjAwNDA0OVoXDTI5MDcxNDAwNDA0OVowFTETMBEGA1UE
AxMKbWluaWt1YmVDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAOg8
N1lGN4sr7MH1SwPZgKw8WWj1nCwlVkjWeoeeoGVCr9n9q8M6OFcnkupMp/VzL2SY
jMYmpEgfBCIsqoGtYkz8dS5UIDciYTDmfKFxeETNvvV4FtlgUJgvyiGuazcEtrAJ
CEatFSJGQ0r5OTqZ5nWlzFpydT30DvwJun0lSXYiBMUYaj9VyO7WLGey3fCgLtaE
TFzFKzmu4u4+Vyyjapndm4enbfsEtN8n5JRVWgnOkimKhWVFsliVmcgsgMrdWobh
xcDWPimKDvWOLnuoSaaudn2EAngYtZ1TtVxCliDZJY4H8PhHlSJM1MBhM5I+PoHY
wCKfHxCetF+mR4u2jKECAwEAAaNCMEAwDgYDVR0PAQH/BAQDAgKkMB0GA1UdJQQW
MBQGCCsGAQUFBwMCBggrBgEFBQcDATAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3
DQEBCwUAA4IBAQA4EHo3ymNRaX+rvp+1xBQUDBUvQjc+swE4wrlJnugjeLYt7uok
YOooN6PpAT2Dlxwtm5ayAUJI1OgJ0Osz/P/+imejSXAy3Hf2IBoDI5HPMfeFFEs8
6Q24K5dJdgJ+dx53gtG++bi2grI51Lomd4Jy7b7YDcFPtMLM23gkC+Wh7yqCwBem
byNVAWX2HVmROb7GMui37zOg7fy0zBUDVS528YDOzBV9B5ajbacV8OnMH5WHuAbL
clSBZt0KYuxk231wDPKJaKwjUrXr2djV5Nt624HIC+f3Rv96fRGnvNYePIEna97q
Kc7AlhwUVNEzxACkjtlOZO2NSw6DIM6xEpEw
-----END CERTIFICATE-----
         </ca-certificate>
      </kubernetes>
   </network>
```

## Testing Horizontal Pod Autoscaler (HPA)

With the custom metrics installed, you can automatically scale out or in the Hazelcast cluster running on GKE. Kubernetes HPA is responsible for auto-scaling and you can monitor it by executing the following command.

```console
# Monitor HPA.
watch kubectl describe hpa my-release-hazelcast
```

The following is a screen snapshot:

```console
Name:               my-release-hazelcast
Namespace:          default
Labels:             <none>
Annotations:        autoscaling.alpha.kubernetes.io/conditions:
                      [{"type":"AbleToScale","status":"True","lastTransitionTime":"2019-09-28T13:31:32Z","reason":"Re
adyForNewScale","message":"recommended size...
                    autoscaling.alpha.kubernetes.io/current-metrics: [{"type":"Pods","pods":{"metricName":"on_heap_ra
tio","currentAverageValue":"99m"}}]
                    autoscaling.alpha.kubernetes.io/metrics: [{"type":"Pods","pods":{"metricName":"on_heap_ratio","ta
rgetAverageValue":"850m"}}]
                    kubectl.kubernetes.io/last-applied-configuration:
                      {"apiVersion":"autoscaling/v2beta1","kind":"HorizontalPodAutoscaler","metadata":{"annotations":
{},"name":"my-release-hazelcast","namespace...
CreationTimestamp:  Sat, 28 Sep 2019 09:31:17 -0400
Reference:          StatefulSet/my-release-hazelcast-enterprise
Min replicas:       2
Max replicas:       10
StatefulSet pods:   2 current / 2 desired
```

### Auto-Scaling Out

HPA has been configure to auto-scale when the `on_heap_ratio` metric reaches 850m or 85% of the max heap. You can monitor the following lines displayed by the above command.

```console
"pods":{"metricName":"on_heap_ratio","currentAverageValue":"99m"}
"pods":{"metricName":"on_heap_ratio","targetAverageValue":"850m"}
```

When the 'current' value reaches greater than 850m, HPA will add another pod to the cluster.

To test HPA, configure the `test_perf`'s `hazelcast-client.xml` as described in the [Running Client Applications](#Running-Client-Applications) section and run the `test_ingestion` script as follows:

```console
cd $PADOGRID_WORKSPACE/apps/perf_test/bin_sh
vi ../etc/hazelcast-client.xml

# After hazelcast-client.xml has been configured, run the following command to ingest data
./test_ingestion -run
```

The `test_ingestion` script should ingest just enough data into the Hazelcast cluster so that it will increase the `on_heap_ratio` to above 850m. When HPA autoscales you will see the following output (The pod size is increased from 2 to 3.) If the limit does not go above the threshold value then try running it again.

```console
StatefulSet pods:   3 current / 3 desired
```

### Auto-Scaling In

The `test_ingestion` script puts data into two maps: `eligibility` and `tx`. Both maps have been preconfigured with TTL of 120 seconds so that the ingested data will be discarded and hence freeing memory. You can monitor the maps getting emptied from the Management Center. The default setting for scaling in is 5 minutes. After 5 minutes, you should see HPA removing a pod from the Hazelcast cluster. The TTL settings are defined in the `configmap.yaml` file as follows:

```yaml
# $HAZELCAST_KUSTOM_DIR/etc/hazelcast/overlay-base/configmap.yaml
      map:
        eligibility:
          time-to-live-seconds: 120
        profile:
          time-to-live-seconds: 120
```

:heavy_exclamation_mark: To immediately free the unused heap memory, once the maps are fully evicted (emptied), you may want to click on the *Members/member-ip/Run GC* button from the Management Center to run full GC on each member.

## Tearing Down

```console
# Uninstall custom metrics and Hazelcast
kubectl delete -k custom-metrics/overlay-base
kubectl delete -k hazelcast/overlay-base
kubectl delete -k hazelcast/storage/gke/nfs-pod
kubectl delete -k hazelcast/overlay-nfs
kubectl delete -k hazelcast/init
kubectl delete -k hazelcast/storage/gke/init-nfs

# Delete disk
gcloud compute disks delete gce-nfs-disk

# Delete Kuternetes cluster
gcloud container clusters delete kustomize-test
```

### Deleting GCR Images

```
# gcloud container images delete [HOSTNAME]/[PROJECT_ID]/[IMAGE]:[TAG] --force-delete-tags
gcloud container images delete $GCR_HOSTNAME/$PROJECT_ID/hazelcast/hazelcast-enterprise:3.12.1 --force-delete-tags
gcloud container images delete $GCR_HOSTNAME/$PROJECT_ID/hazelcast/management-center:latest --force-delete-tags
```

## References

1. Install Docker, [https://docs.docker.com/install/](https://docs.docker.com/install/).
2. gcloud Quick Start, [https://cloud.google.com/sdk/docs/quickstarts](https://cloud.google.com/sdk/docs/quickstarts)
3. Install and Set Up kubectl, [https://kubernetes.io/docs/tasks/tools/install-kubectl/](https://kubernetes.io/docs/tasks/tools/install-kubectl/).
4. Install kustomize, [https://github.com/kubernetes-sigs/kustomize/blob/master/docs/INSTALL.md](https://github.com/kubernetes-sigs/kustomize/blob/master/docs/INSTALL.md). 
5. Download jq, [https://stedolan.github.io/jq/download/](https://stedolan.github.io/jq/download/).
6. Hazelcast Enterprise Helm Charts, [https://github.com/hazelcast/charts](https://github.com/hazelcast/charts).
7. Prometheus Adapter for Kubernetes Metrics APIs, [https://github.com/DirectXMan12/k8s-prometheus-adapter](https://github.com/DirectXMan12/k8s-prometheus-adapter).
8. Querying Prometheus, [https://prometheus.io/docs/prometheus/latest/querying/basics/](https://prometheus.io/docs/prometheus/latest/querying/basics/).
9. Horizontal Pod Autoscaler, [https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/).
10. k8s-prom-hpa, *Custom Autoscaling Example*, [https://github.com/stefanprodan/k8s-prom-hpa](https://github.com/stefanprodan/k8s-prom-hpa).