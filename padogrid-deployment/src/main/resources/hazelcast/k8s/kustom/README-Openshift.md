# Hazelcast on OpenShift using `kustomize`

This directory contains Kubernetes configuration files for deploying Hazelcast, Prometheus, custom metrics API, and Horizontal Pod Autoscaler (HPA) on **OpenShift**. The configuration files are to be used with the `oc apply -k` or `kubectl apply -k` command.

## Initializing Workspace

To follow instructions in this article, you must first install `PADOGRID` and create a workspace. For example, the following creates the `ws-gke` workspace in the `~/padogrid/workspaces/myrwe` directory. Make sure to source in the `initenv.sh` file, which sets the required environment variables that are specific to the workspace you created.

```console
mkdir -p ~/padogrid/workspaces/myrwe
tar -C ~/padogrid/products/ -xzf padogrid_0.9.2-SNAPSHOT
~/padogrid/products/padogrid_0.9.2-SNAPSHOT/bin_sh/create_workspace -workspace ~/padogrid/workspaces/myrwe/ws-gke
. ~/padogrid/workspaces/myrwe/ws-gke/initenv.sh
```

We will be using the `$PADOGRID_WORKSPACE` environment variable set by `initenv.sh` throughout this article.

```console
echo $PADOGRID_WORKSPACE 
/Users/dpark/padogrid/workspaces/myrwe/ws-gke
```

:exclamation: If you have built PadoGrid from Windows and the commands fail due to the Windows line break issue, then you must convert the next line characters using the `dos2linux` command. Make sure to convert all files including the hidden files as follows:

```
dos2unix ~/Hazelcast/padogrid_0.9.2-SNAPSHOT/hazelcast/bin_sh/*
dos2unix ~/Hazelcast/padogrid_0.9.2-SNAPSHOT/hazelcast/bin_sh/.*sh
dos2unix ~/Hazelcast/padogrid_0.9.2-SNAPSHOT/apps/k8s/kustom/bin_sh/*
dos2unix ~/Hazelcast/padogrid_0.9.2-SNAPSHOT/apps/k8s/kustom/bin_sh/.*sh
```

## Required Software List

Before you begin, you must first install the following software. See the [References](#References) section for URIs.

- Docker
- openssl (most operating systems have this already installed)
- oc or kubectl
- jq (optional)
- watch (optional)

## Creating Kubernetes Environment

In your workspace, create a Kubernetes environment in which we will setup Hazelcast deployment files as follows:

```console
create_k8s -k8s openshift -cluster kustomize-test
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
    │   ├── overlay-prometheus
    │   └── prometheus
    └── hazelcast
        ├── base
        ├── init
        ├── overlay-base
        └── storage
```

## Configuring OpenShift Environment

It is assumed that you have access to an OpenShift cluster.

Source in the `setenv.sh` file as follows.

```console
. $PADOGRID_WORKSPACE/k8s/kustomize-test/bin_sh/setenv.sh
```

We'll use the `$HAZELCAST_KUSTOM_DIR` environment variable set in the `setenv.sh` file in the subsequent sections.

### OpenShift Container Storage

We need a shared storage such as NFS or OCS with `accessModes` of `ReadWriteMany` so that we can mount a shared volume in which we can place application jar and configuration files accessible by all the Hazelcast pods. The `etc/hazelcast/overlay-cephfs/cephfs-pvc.yaml` file has been configured to `cephfs` of OCS. You can change it another `ReadWriteMany` storage such as NFS as needed. 

Set `storageClassName` to the your storage class name.

```bash
cd_k8s kustomize-test; cd etc
vi hazelcast/overlay-cephfs/cephfs-pvc.yaml
```

Change the value of `storageClassName` as needed.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: hazelcast-pv-claim
  namespace: default
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 2Gi
  storageClassName: ocs-storagecluster-cephfs
  volumeMode: Filesystem
```

### Hazelcast Images and License Keys

The Hazelcast cluster images are by default set as follows. You can change the image versions and set license keys in the respective StatefulSet files in the `overlay-base` directory.

#### Hazelcast Enterprise

File: `hazelcast/overlay-base/statefulset.yaml`

| Parameter    | Value                |
| ------------ | -------------------- |
| IMAGE        | hazelcast-enterprise |
| TAG          | 3.12.7               |

#### Management Center

File: `hazelcast/overlay-base/mc-statefulset.yaml`

| Parameter    | Value                |
| ------------ | -------------------- |
| IMAGE        | management-center    |
| TAG          | 3.12.9               |

### Copy Application Library Files to the Storage

We can now upload the application library files to the NFS disk which can be accessed by all Hazelcast containers via the persistence volume claim. To do this, we create a pod that uses the persistence volume claim to access the shared storage. 

```console
# First, create a pod to which you will mount the disk
kubectl apply -k hazelcast/storage/openshift/cephfs-pod

# Login to the cephfs-pod and create the /var/cephfs/plugins/v1 directory
kubectl exec -it cephfs-pod bash
mkdir -p /var/cephfs/plugins/v1
exit

# Copy the PADOGRID jar files 
# (Note: copy to /var/cephfs/plugins/v1, NOT /data/custom/plugins/v1)
kubectl cp $PADOGRID_HOME/hazelcast/lib/hazelcast-addon-common-0.9.2-SNAPSHOT.jar cephfs-pod:/var/cephfs/plugins/v1/
kubectl cp $PADOGRID_HOME/hazelcast/lib/v3/hazelcast-addon-core-3-0.9.2-SNAPSHOT.jar cephfs-pod:/var/cephfs/plugins/v1/
kubectl cp $PADOGRID_HOME/hazelcast/plugins/v3/hazelcast-addon-core-3-0.9.2-SNAPSHOT-tests.jar cephfs-pod:/var/cephfs/plugins/v1/

# Delete the pod
kubectl delete -k hazelcast/storage/openshift/cephfs-pod
```

### Create Certificates

To use custom metrics, we need to setup TLS certificates. This is done by running the `bin_sh/create_certs` script which creates and inserts them into the `custom-metrics/overlay-base/cm-adapter-serving-certs.yaml` file. Please see this script for details.

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
|hazelcast/storage/openshift/cephfs | hazelcast/overlay-cephfs | cephfs-pvc.yaml |
|Generated      | custom-metrics/overlay-base | cm-adapter-serving-certs.yaml |
|custom-metrics/prometheus | custom-metrics/overlay-prometheus | prometheus-pvc.yaml |

Some of these overlay files will be modified in the subsequent sections. Their `kustomization.yaml` files are shown below for your reference.

#### overlay-base/kustomization.yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
- ../base

patchesStrategicMerge:
- configmap.yaml
- mc-pvc.yaml
- statefulset.yaml
- mc-statefulset.yaml
- hazelcast-hpa-custom.yaml
```

#### overlay-cephfs/kustomization.yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
- ../storage/openshift/cephfs

patchesStrategicMerge:
- cephfs-pvc.yaml
```

### Hazelcast Configuration File

The Hazelcast configuration file, `configmap.yaml` is found in the `hazelcast/base`. This distribution includes a *kustomized* version of that file in the the `hazelcast/overlay-base` directory. It has been preconfigured with `PADOGRID` domain classes and eviction policies to demonstrate the autoscaler. We have already uploaded the jar files that contain the domain classes in the [Copy Application Library Files to NFS Disk](#Copy-Application-Library-Files-to-NFS-Disk) section. You can modify this file as needed to incorporate your own applications.

### Deploy Hazelcast and Custom Metrics

We are now ready to deploy Hazelcast and custom metrics to the GKE cluster. Up until now, we have been installing and configuring the GKE cluster.

```console
# Initialize Kubernetes cluster. This command configures a service account and RBAC.
kubectl apply -k hazelcast/init

# Create persistent volume claim for cephfs.
# (Make sure storageClassName is set to the OCS cephfs before running this command.)
kubectl apply -k hazelcast/overlay-cephfs

# Deploy Hazelcast Enterprise
kubectl apply -k hazelcast/overlay-base

# Deploy custom metrics API and start Prometheus/HPA.
kubectl apply -k custom-metrics/overlay-base
kubectl apply -k custom-metrics/overlay-prometheus
```

### Monitor StatefulSet

You can use the browser (Openshift Web Console) to monitor the pods and services getting started. The URI has the following form:

```
https://console-openshift-console.apps.ocp-hazel.jojo81.online
```

From your terminal, you can also monitor the OpenShift objects as follows:

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

The `storage/openshift` directory contains storage configuration files that are specific to OpenShift. These files create a persistent volume claim used by Hazelcast pods for loading application specific configuration and library files. 

The `hazelcast/init` directory contains initialization files that must first be applied before applying `hazelcast/overlay-base` which is described below. These files create a service account and RBAC (Role-Based-Access-Control).

The `hazelcast/base` directory is the base directory that contains all the configuration files for deploying and starting Hazelcast pods.

The `hazelcast/overlay-base` directory contains configuration files that customize or patch the base files. Note that we also copied the `hazelcast-hpa-custom.yaml` file into this directory in [Quick Start](#Quick-Start). You can include additional custom metrics in this file to autoscale Hazelcast pods. The Prometheus custom metrics are defined in `custom-metrics/custom-metrics-api/custom-metrics-config-map.yaml`, which you can also extend to define additional custom metrics.

```console
kustomize-test
├── README-Openshift.md
├── bin_sh
│   ├── create_certs
│   └── setenv.sh
└── etc
    ├── custom-metrics
    │   ├── base
    │   ├── custom-metrics-api
    │   ├── metrics-server
    │   ├── overlay
    │   └── prometheus
    ├── hazelcast
    │   ├── base
    │   ├── init
    │   ├── overlay-base
    │   ├── overlay-cephfs
    │   └── storage
    └── prometheus
        ├── grafana_datasource.yaml
        ├── kustomization.yaml
        ├── prometheus.yaml
        ├── service_account.yaml
        └── service_monitor.yaml
```

## Monitoring Hazelcast

### Prometheus

Prometheus runs in the `monitoring` namespace and the `prometheus-lb` load balancer service exposes the external IP.

```bash
kubectl get svc prometheus-lb -n monitoring
```

**Output:**

```console
NAME            TYPE           CLUSTER-IP       EXTERNAL-IP                                                               PORT(S)          AGE
prometheus-lb   LoadBalancer   172.30.159.249   a011498d0b3954d0785b613b998a7bdc-1452224069.us-east-2.elb.amazonaws.com   9090:31021/TCP   13m
```

Use the external IP along with the port number to view Prometheus.

http://a011498d0b3954d0785b613b998a7bdc-1452224069.us-east-2.elb.amazonaws.com:9090

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

Hazelcast Management Center can be viewed via its load balancer on port 8080.

```console
# Get the Management Center loadbalancer service external IP
kubectl get svc my-release-hazelcast-enterprise-mancenter -default
```

**Output:**

```console
NAME                                        TYPE           CLUSTER-IP       EXTERNAL-IP                                                               PORT(S)          AGE
my-release-hazelcast-enterprise-mancenter   LoadBalancer   172.30.230.150   aa4da0cb28ca94f849ceff8be9a1c07f-1310371129.us-east-2.elb.amazonaws.com   8080:31000/TCP   20m
```

Use the load balancer service external IP to view the Management Center from your browser:

http://aa4da0cb28ca94f849ceff8be9a1c07f-1310371129.us-east-2.elb.amazonaws.com:8080/hazelcast-mancenter


## Running Client Applications

To connect to the Hazelcast cluster in GKE, you need to configure the Kubernetes Discovery Service in your client application.

Get the master URI.

```console
kubectl cluster-info
```

**Output:**

```console
Kubernetes master is running at https://api.ocp-hazel.jojo81.online:6443
```

Describe one of the pods to get the secret token needed for connecting external clients to Hazelast.

```bash
kubectl describe pod my-release-hazelcast-enterprise-0 | grep token
```

**Output:**

```console
      /var/run/secrets/kubernetes.io/serviceaccount from enterprise-token-dwjwz (ro)
  enterprise-token-dwjwz:
    SecretName:  enterprise-token-dwjwz
```

Use the token name that starts with the prefix "enterprise-token-" to get api-token
and ca-certificate.

```bash
# Get api-token
kubectl get secret enterprise-token-dwjwz  -o jsonpath={.data.token} | base64 --decode
# Get ca-certificate
kubectl get secret enterprise-token-dwjwz  -o jsonpath={.data.ca\\.crt} | base64 --decode
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
         <kubernetes-master>https://api.ocp-hazel.jojo81.online:6443</kubernetes-master>
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
kubectl delete -k custom-metrics/overlay-prometheus
kubectl delete -k custom-metrics/overlay-base
kubectl delete -k hazelcast/overlay-base
kubectl delete -k hazelcast/storage/openshift/cephfs-pod
kubectl delete -k hazelcast/overlay-cephfs
kubectl delete -k hazelcast/init
```

## References

1. Install Docker, [https://docs.docker.com/install/](https://docs.docker.com/install/).
2. Install OC, [https://www.okd.io/download.html](https://www.okd.io/download.html)
3. Install and Set Up kubectl, [https://kubernetes.io/docs/tasks/tools/install-kubectl/](https://kubernetes.io/docs/tasks/tools/install-kubectl/).
4. Install kustomize, [https://github.com/kubernetes-sigs/kustomize/blob/master/docs/INSTALL.md](https://github.com/kubernetes-sigs/kustomize/blob/master/docs/INSTALL.md). 
5. Download jq, [https://stedolan.github.io/jq/download/](https://stedolan.github.io/jq/download/).
6. Hazelcast Enterprise Helm Charts, [https://github.com/hazelcast/charts](https://github.com/hazelcast/charts).
7. Prometheus Adapter for Kubernetes Metrics APIs, [https://github.com/DirectXMan12/k8s-prometheus-adapter](https://github.com/DirectXMan12/k8s-prometheus-adapter).
8. Querying Prometheus, [https://prometheus.io/docs/prometheus/latest/querying/basics/](https://prometheus.io/docs/prometheus/latest/querying/basics/).
9. Horizontal Pod Autoscaler, [https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/).
10. k8s-prom-hpa, *Custom Autoscaling Example*, [https://github.com/stefanprodan/k8s-prom-hpa](https://github.com/stefanprodan/k8s-prom-hpa).
