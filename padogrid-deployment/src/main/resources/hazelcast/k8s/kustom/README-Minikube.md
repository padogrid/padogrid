# Hazelcast on Minikube using `kustomize`

This directory contains Kubernetes configuration files for deploying Hazelcast, Prometheus, custom metrics API, and Horizontal Pod Autoscaler (HPA) on **Minikube**. The configuration files are to be used with the `kustomize` or `kubectl apply -k` command.

## WSL Users

To run this tutorial entirely on WSL even though Minikube runs on Windows, follow the steps shown below.

- If you are running WSL, then make sure the workspace you create is on the shared folder between Windows and WSL. The Minikube settings must be converted when we switch between them. This is automatically done by a PadoGrid script as you will see in this tutorial.
- Include the `.exe` extension to all `minikube` commands, i.e., run `minikube.exe` instead of `minikube`.

## Creating Workspace

For this tutorial, let's create a new workspace named `ws-minikube`.

```console
create_workspace -name ws-minikube
```
Upon completion of creating the workspace, switch into the workspace.

```console
switch_workspace ws-minikube
```

We will be using the `$PADOGRID_WORKSPACE` environment variable set by `switch_workspace` throughout this article. You can check its value as follows:

```console
echo $PADOGRID_WORKSPACE 
/Users/dpark/Padogrid/workspaces/myrwe/ws-minikube
```

## Required Software List

Before we begin, we must first install the following software. See the [References](#References) section for URLs.

- VirtualBox (for Windows, VirtualBox or Hyper-V)
- minikube
- docker (for Windows, Docker Toolbox if VirutalBox, Docker Desktop if Hyper-V)
- kubectl
- kustomize (optional - `kubectl apply -k` equivalent to `kustomize` )
- openssl
- jq (optional)
- watch (optional)

### `kubectl` on Windows 10

Running `kubectl` on Windows can be a challenge due to the lack of examples and support for command auto-completion. To ease the pain, it is recommended that you install `kubectl` on WSL. The following article provides installation steps:

[Hazelcast Minikube on WSL](Hazelcast-Minikube-on-WSL)

## Creating Kubernetes Environment

In your workspace, create a Kubernetes environment in which we will setup Hazelcast deployment files.

```console
create_k8s -k8s minikube -cluster minikube_test

# Upon creation, source in the 'setenv.sh' file as follows.
. $PADOGRID_WORKSPACE/k8s/minikube_test/bin_sh/setenv.sh
```

We will be using the `$HAZELCAST_KUSTOM_DIR` environment variable set by `setenv.sh` throughout the subsequent sections.

## Quick Start

First, start the Minikube VM. If you are using Windows Hyper-V, then replace `--vm-driver=virtualbox` with `--vm-driver=hyperv`.

```console
# Start minikube with 5Gi and 4 CPUs.
# If you are using Hyper-V, then specify --vm-driver=hyperv.
minikube start --extra-config=kubelet.authentication-token-webhook=true --memory=5120 --cpus=4 --vm-driver=virtualbox

# Login to the host and create a directory in the host node (minikube)
# where we will upload addon jar files. We'll be mounting /data/custom as
# a persistent volume later.
minikube ssh
sudo mkdir -p /data/custom/plugins/v1
sudo chmod -R 777 /data
# Change password to docker
sudo passwd docker
exit
```

Let's add the Minikube IP address to the `/etc/hosts` file for convenience. The Kubernetes configuration files included in PadoGrid use the host name `minikube`.

**Linux/macOS:**

```console
sudo echo "$(minikube ip)	minikube" >> /etc/hosts
```

**Windows:**

If you are using WSL, you should also add the `minikube` host name in the Windows `hosts` file.

```console
minikube.exe ip
172.17.34.252

REM Edit the hosts file and add the minikube host name
notepad C:\Windows\System32\drivers\etc\hosts
172.17.34.252	minikube
```

With the `minikube` host name in place, you can now use it to login to the Minikube VM. Let's upload the required binary files to the host OS file system which we'll be mounting as a persistent volume shortly.

```console
# Switch cluster into the default cluster you created with 'create_workspace'. 
switch_cluster

# Upload the cluster's 'etc' directory that contains 'cache.xml'
scp -r etc docker@minikube:/data/custom/

# Upload addon jar files to the minikube host. 
# IMPORTANT: Upload v3/* for Hazelcast 3.x, v4/* for Hazelcast 4.x.

# Hazelcast 3.x
scp -r $PADOGRID_HOME/lib/*  \
$PADOGRID_HOME/hazelcast/lib/v3/* \
$PADOGRID_HOME/hazelcast/plugins/v3/* \
docker@minikube:/data/custom/plugins/v1/

# Hazelcast 4.x:
scp -r $PADOGRID_HOME/lib/*  \
$PADOGRID_HOME/hazelcast/lib/v4/* \
$PADOGRID_HOME/hazelcast/plugins/v4/* \
docker@minikube:/data/custom/plugins/v1/
```

If you are using WSL then you will need to convert the minikube certificate file paths from Windows to Unix notations. From WSL, edit the `set_minikube` and `set_minikube.bat` scripts to enter the minikube IP and your user name, and run it as follows:

```console
# Edit both set_minikube and set_minikube.bat and enter the user name and minikube IP address
cd $HAZELCAST_KUSTOM_DIR/bin_sh
vi set_minikube set_minikube.bat

USER_NAME=<your user name>
MINIKUBE_IP=<minikube ip>

# Save 'set_minikube' and 'set_minikube.bat' and run 'set_minikube'
./set_minikube
```

:exclamation: Whenever you switch from WSL to Windows, you must run `set_minikube.bat` to set the correct paths, and vice versa.

After running `set_minikube`, create certificates for Prometheus as follows.

```console
# Create TLS certificates for the Prometheus custom metrics API adapter
cd $HAZELCAST_KUSTOM_DIR/bin_sh
./create_certs
```

You are now ready to deploy containers.

```console
# Change directory to etc/ where the Kubernetes configuration files are located.
cd $HAZELCAST_KUSTOM_DIR/etc

# Configure a service account and RBAC
kubectl apply -k hazelcast/init/

# Create static persistent volume where we will store addon jar files
kubectl apply -k hazelcast/storage/minikube/

# Copy base files to the overlay directory. These files will be modified.
cp hazelcast/base/statefulset.yaml hazelcast/overlay-base/
cp hazelcast/base/mc-statefulset.yaml hazelcast/overlay-base/

# Copy HPA metrics file to the overly directory also. You can add other
# metrics to autoscale as needed.
cp hazelcast/base/hazelcast-hpa-custom.yaml hazelcast/overlay-base/

# Enter your Hazelcast Enterprise liense key in both statefulset.yaml and mc-statefulset.yaml.
vi hazelcast/overlay-base/statefulset.yaml
vi hazelcast/overlay-base/mc-statefulset.yaml

# Deploy Hazelcast.
kubectl apply -k hazelcast/overlay-base/

# Deploy custom metrics API and start Prometheus/HPA
kubectl apply -k custom-metrics/overlay-base/

# Monitor HPA.
watch kubectl describe hpa my-release-hazelcast
``` 

## Directory Overview

The `bin_sh` directory contains the `create_certs` script for generating the required secret file with TLS certificates. Make sure to run this script first before running Kubernetes.

The `kustom/etc` directory contains the entire Kubernetes configuration files. Each sub-directory contains `kustomization.yaml` that includes base directories and resource files for their respective configuration.

The `storage/minkube` directory contains storage configuration files that are specific to Minikube. These files create a local *hostPath* storage, persistent volume and claim used by Hazelcast pods for loading application specific configuration and library files.

The `hazelcast/init` directory contains initialization files that must first be applied before applying hazelcast/overlay-base which is describe. These files create a service account and RBAC (Role-Based-Access-Control).

The `hazelcast/base` directory is the base directory that contains all the configuration files for deploying and starting Hazelcast pods.

The `hazelcast/overlay-base` directory contains configuration files that customize or patch the base files. Note that we also copied the `hazelcast-hpa-custom.yaml` file into this directory in [Quick Start](#Quick-Start). You can include additional custom metrics in this file to autoscale Hazelcast pods. The custom metrics are defined in `custom-metrics-api/custom-metrics-config-map.yaml`, which you can also extend to define additional custom metrics.

See [Quick Start](#Quick-Start) for the execution order.

```console
kustom
├── bin_sh
└── etc
    ├── custom-metrics
    │   ├── base
    │   ├── custom-metrics-api
    │   ├── metrics-server
    │   ├── overlay
    │   └── prometheus
    └── hazelcast
        ├── base
        ├── init
        ├── overlay-base
        ├── overlay-nfs
        └── storage
            └── minikube
```

## Monitoring Kubernetes

### Dashboard

Start the Kubernetes dashboard by running the following command. It will automatically launch the browser.

#### Non-WSL

```console
minikube dashboard &
```

#### WSL

```console
# Change directory to $GEODE_KUSTOM_DIR/bin_sh
cd_k8s minikube_test; cd bin_sh

# Convert minkube settings to Windows and run the dashboard
cmd.exe /c set_minikube.bat && minikube.exe dashboard &

# Convert minikube settings back to WSL
./set_minikube
```

### Prometheus

Prometheus runs in the `monitoring` namespace and has the port number `31190` exposed. Use the following URI in the browser.

**URL:** http://minikube:31190 

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

The Hazelcast Management Center service port is `31000`.

**URL:** http://minikube:31000/hazelcast-mancenter/

## Running Client Applications

### Minikube

To connect client applications to the Hazelcast cluster running on minikube, you need to run the following commands to create static routing table entries that map the CIDRs used by Pods, Services and LoadBalancers to the minikube (host-only) IP. 

```console
# Create a routing table entry
minikube tunnel

# Create routes for the entire /8 block
# macOS
sudo route -n add 10.0.0.0/8 $(minikube ip)
netstat -nr -f inet
# Linux
sudo ip route add 10.0.0.0/8 via $(minikube ip)
netstat -nr -f inet
# Windows
route ADD 10.0.0.0 MASK 255.0.0.0 <minikube ip>
route print -4
```

Unfortunately, for version v1.2.0, `minikube tunnel` has a bug that consumes nearly all the minikube CPUs. If your minikube becomes unresponsive then stop the `minikube tunnel` command (see [Tearing Down](#Tearing-Down)) and follow the instructions below. Otherwise, you can jump to the [GCP (Google Cloud Platform)](#gcp-goggle-cloud-platform) section.

To connect to the Hazelcast cluster without enabling kubectl tunnel, you must disable SmartRouting and directly connect to a single member. This is not ideal as it puts all of the client load on the connected member, but you will be able to connect to the cluster.

To run the `perf_test` app, for example, edit the `hazelcast-client.xml` file as follows (service ports `30000` and `30001` are exposed):

```xml
<!-- $PADOGRID_WORKSPACE/apps/perf_test/etc/hazelcast-client.xml -->
   <network>
      <smart-routing>false</smart-routing>
      <cluster-members>
         <address>minikube-ip:30000</address>
         <address>minikube-ip:30001</address>
      </cluster-members>
   </network>
```

### GKE (Goggle Kubernetes Engine)

If you are running in GCP or able to run load balancers and expose public IPs then follow the steps below.

```console
# List the screts
kubectl get secrets
NAME                     TYPE                                  DATA   AGE
default-token-hd5w2      kubernetes.io/service-account-token   3      43m
enterprise-token-2qdzz   kubernetes.io/service-account-token   3      41m

# Use the token name that starts with the prefix "enterprise-token-" to get api-token and ca-certificate.
# Get api-token
kubectl get secret enterprise-token-2qdzz  -o jsonpath={.data.token} | base64 --decode
# Get ca-certificate
kubectl get secret enterprise-token-2qdzz  -o jsonpath={.data.ca\\.crt} | base64 --decode
```

Enter the encoded token and certificate in the `hazelcast-client.xml` file as shown below. Note that the service name is `my-service-lb` which is created when you applied the configuration files.

```xml
<!-- $PADOGRID_WORKSPACE/apps/perf_test/etc/hazelcast-client.xml -->
   <network>
      <smart-routing>true</smart-routing>
      <kubernetes enabled="true">
         <namespace>default</namespace>
         <service-name>my-service-lb</service-name>
         <use-public-ip>true</use-public-ip>
         <kubernetes-master>https://192.168.99.102:8443</kubernetes-master>
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

With the custom metrics installed as described in the [Quick Start](#Quick-Start) section, you can automatically scale out or in the Hazelcast cluster running on minishift. Kubernetes HPA is responsible for auto-scaling and you can monitor it by executing the following command.

```console
# Monitor HPA.
watch kubectl describe hpa my-release-hazelcast
```

### Auto-Scaling Out

HPA has been configure to auto-scale when the `on_heap_ratio` metric reaches 850m or 85% of the max heap. You can monitor the following lines displayed by the above command.

```console
Metrics:                    ( current / target )
  "on_heap_ratio" on pods:  121m / 850m
```

When the 'current' value reaches greater than 850m, HPA will add another pod to the cluster.

To test HPA, configure the `test_perf`'s `hazelcast-client.xml` as described in the [Running Client Applications](#Running-Client-Applications) section and run the `test_ingestion` script as follows:

Run `perf_test` as follows:

```console
cd_app perf_test; cd bin_sh
./test_ingestion -run
```

The `test_ingestion` script should ingest just enough data into the Hazelcast cluster so that it will increase the `on_heap_ratio` to above 850m. Once it reaches more than 850m, stop the script.

### Auto-Scaling In

The `test_ingestion` script puts data into two maps: `eligibility` and `tx`. Both maps have been preconfigured to with TTL of 120 seconds so that the ingested data will be discarded and hence freeing memory. You can monitor the maps getting emptied from the Management Center. The default setting for scaling in is 5 minutes. After 5 minutes, you should see HPA removing a pod from the Hazelcast cluster. The TTL settings are defined in the `configmap.yaml` file as follows:

```yaml
# $HAZELCAST_KUSTOM_DIR/etc/hazelcast/overlay-base/configmap.yaml
      map:
        eligibility:
          time-to-live-seconds: 120
        profile:
          time-to-live-seconds: 120
```

:heavy_exclamation_mark: To make sure the unused heap memory is freed, once the maps are emptied, you may want from the Management Center click on the *Members/member-ip/Run GC* button to run full GC on each member to reclaim unused heap memory.


## Tearing Down

Execute the following:

```console
# Stop kubectl tunnel (kill or ctrl-c)
kill -9 `ps -ef|grep "kubectl tunnel" |grep -v grep | awk '{print $2}'`

# Stop the dashboard
kill -9 `ps -ef|grep "minikube dashboard" |grep -v grep | awk '{print $2}'`

# Delete route entries
# macOS
sudo route -n delete 10.0.0.0/8
# Linux
sudo ip route delete 10.0.0.0/8
# Windows
route DELETE 10.0.0.0

# Uninstall custom metrics and Hazelcast.
cd_k8s minikube_test; cd etc
kubectl delete -k custom-metrics/overlay-base/
kubectl delete -k hazelcast/overlay-base/
kubectl delete -k hazelcast/storage/minikube/
kubectl delete -k hazelcast/init/

# Delete the minikube VM.
minikube delete
```

## Troubleshooting Guide
 
### I can't start the minikube dashboard. I'm getting the following error message:
```console
X Unable to enable dashboard: decode C:\Users\<user>\.minikube\config\config.json: EOF
```
**Solution:** Delete the minikube, config.json file, and restart minikube

```console
minikube delete
erase C:\Users\<user>\.minikube\config\config.json 
minikube start --extra-config=kubelet.authentication-token-webhook=true --memory=5120 --cpus=4 --vm-driver=virtualbox
```

## References

1. Download VirtualBox, [https://www.virtualbox.org/wiki/Downloads](https://www.virtualbox.org/wiki/Downloads).
2. Install Minikube, [https://kubernetes.io/docs/tasks/tools/install-minikube/](https://kubernetes.io/docs/tasks/tools/install-minikube/).
3. Install Docker, [https://docs.docker.com/install/](https://docs.docker.com/install/).
4. Install and Set Up kubectl, [https://kubernetes.io/docs/tasks/tools/install-kubectl/](https://kubernetes.io/docs/tasks/tools/install-kubectl/).
5. Install kustomize, [https://github.com/kubernetes-sigs/kustomize/blob/master/docs/INSTALL.md](https://github.com/kubernetes-sigs/kustomize/blob/master/docs/INSTALL.md). 
6. OpenSSL Download, [https://www.openssl.org/source/](https://www.openssl.org/source/).
7. Download jq, [https://stedolan.github.io/jq/download/](https://stedolan.github.io/jq/download/).
8. Hazelcast Enterprise Helm Charts, [https://github.com/hazelcast/charts](https://github.com/hazelcast/charts).
9. Prometheus Adapter for Kubernetes Metrics APIs, [https://github.com/DirectXMan12/k8s-prometheus-adapter](https://github.com/DirectXMan12/k8s-prometheus-adapter).
10. Querying Prometheus, [https://prometheus.io/docs/prometheus/latest/querying/basics/](https://prometheus.io/docs/prometheus/latest/querying/basics/).
11. Horizontal Pod Autoscaler, [https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/).
12. k8s-prom-hpa, *Custom Autoscaling Example*, [https://github.com/stefanprodan/k8s-prom-hpa](https://github.com/stefanprodan/k8s-prom-hpa).
13. Minikube Tunnel Design Doc, https://github.com/kubernetes/minikube/blob/master/docs/tunnel.md.