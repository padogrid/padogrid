# Installing Hazelcast on OCP or CRC

This article provides instructions for installing Hazelcast on OCP or CRC.

## Directory Contents

```console
.
├── README-OCP.md
├── bin_sh
│   ├── build_app
│   ├── cleanup
│   ├── login_padogrid_pod
│   ├── setenv.sh
│   ├── start_hazelcast
│   ├── start_padogrid
│   ├── stop_hazelcast
│   └── stop_padogrid
├── hazelcast
│   ├── hazelcast-enterprise-rhel.yaml
│   ├── hazelcast-enterprise.yaml
│   ├── hazelcast.yaml
│   └── service-lb.yaml
└── padogrid
    ├── padogrid-no-pvc.yaml
    ├── padogrid.yaml
    └── pv-hostPath.yaml
```

There are three (3) templates obtained from the [Hazelcast GitHub repo](https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/openshift/hazelcast-cluster) in the `hazelcast` directory. The first two (2) are Hazelcast Enterprise which requires an enterprise license key. The last one is an open source Hazelcast template.

* **Hazelcast Enterprise RHEL:** template to deploy Hazelcast IMDG Enterprise RHEL onto OpenShift Container Platform, i.e., `registry.connect.redhat.com/hazelcast/hazelcast-4-rhel8:4.0`.
* **Hazelcast Enterprise:** template to deploy Hazelcast IMDG Enterprise onto OpenShift Container Platform, i.e., `hazelcast/hazelcast-enterprise:4.0`.
* **Hazelcast:** template to deploy Hazelcast IMDG onto OpenShift, i.e., `hazelcast/hazelcast:4.0`.

## 1. Build Local Environment

Run `build_app` which initializes your local environment. This script creates a new OpenShift project using the k8s cluster name. The example shown in this article uses **crc** as the cluster name (and project name).

```bash
cd_k8s crc; cd bin_sh
./build_app
```
## 2. CRC Users (Optional): Create Mountable Persistent Volumes in Master Node

:exclamation: **This section is optional and only applies to CRC users.**

If you are logged onto CRC running on your local PC instead of OpenShift Container Platform (OCP), then to allow read/write permissions, we need to create additional persistent volumes using hostPath for PadoGrid. PadoGrid stores workspaces in the /opt/padogrid/workspaces directory, which can be optionally mounted to a persistent volume. Let’s create a couple of volumes in the master node as follows.

```bash
# Login to the master node
ssh -i ~/.crc/machines/crc/id_rsa core@$(crc ip)

# Create hostPath volumes. We only need one but let's create two (2)
# in case you want to run addional pods.
sudo mkdir -p /mnt/vol1
sudo mkdir -p /mnt/vol2
sudo chmod -R 777 /mnt/vol1
sudo chmod -R 777 /mnt/vol2
sudo chcon -R -t svirt_sandbox_file_t /mnt/vol1 /mnt/vol2
sudo restorecon -R /mnt/vol1 /mnt/vol2
exit
```

We will use the volumes created as follows:

| Container     | CDC File               | Container Path           | Volume Path |
| ------------- | ---------------------- | ------------------------ | ----------- |
| PadoGrid      | padogrid/padogrid.yaml | /opt/padogrid/workspaces | /mnt/vol?   |

We can now create the required persistent volumes using **hostPath** by executing the following.

```bash
cd_k8s crc; cd padogrid
oc create -f pv-hostPath.yaml
```

## 3. Add User to anyuid SCC (Security Context Constraints)

PadoGrid runs as a non-root user that requires read/write permissions to the persistent volume. Let's add your project's default user to the anyuid SCC.

```bash
oc edit scc anyuid
```

**anyuid SCC:**

Add your project under the`users:` section. For our example, since our project name is **crc**, we would enter the following.

```yaml
users:
- system:serviceaccount:crc:default
```

## 4. Create OpenShift secrets

_You can skip this step and go to [Step #5](#5-start-hazelcast) if you are running Hazelcast OSS._

### 4.1. Hazelcast Enterprise RHEL

To download the Hazelcast images from the RedHat Registry, i.e., `registry.connect.redhat.com`, you must create a secret using your RedHat account; otherwise, you will get an "unauthorized" error during the image download time.

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

### 4.2. Hazelcast Enterprise RHEL and Hazelcast Enterprise

Let's also create a secret that holds the Hazelcast enterprise license key.

```bash
# Create hz-enterprise-license secret expected by hazelcast.yaml  
oc create secret generic hz-enterprise-license --from-literal=key=<hazelcast-enterprise-license-key>
```

## 5. Start Hazelcast

```bash
cd_k8s crc; cd bin_sh
./start_hazelcast
```

:exclamation: The `oc` executable version (4.5.9) used for writing this article has a bug that does not properly parse numeric parameters. The following error message is seen if ${HALZELCAST_REPLICAS} is kept in the `hazelcast/hazelcast.yaml` file. 

```console
error: v1.StatefulSet.Spec: v1.StatefulSetSpec.Replicas: readUint32: unexpected character: �, error found in #10 byte of ...|eplicas":"3","select|..., bigger context ...|emplate"},"name":"hazelcast"},"spec":{"replicas":"3","selector":{"matchLabels":{"app":"hazelcast","r|...
```

To prevent the error, `${HAZELCAST_REPLICAS}` has been replaced with  the numeric value of 3 in the `hazelcast/hazelcast*.yaml` file as follows.

```yaml
...  
- apiVersion: apps/v1  
  kind: StatefulSet  
  metadata:  
    name: hazelcast  
    labels:  
      app: hazelcast  
  spec:  
    replicas: 3  
...
```

## 6. Start PadoGrid

```bash
cd_k8s crc; cd bin_sh

# If you have not created local-storage
./start_padogrid
```

## 7. View Services and Routes


```bash
oc get svc
```

Output:

```console
NAME                        TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
hazelcast-service           ClusterIP      None            <none>        5701/TCP         9m25s
hazelcast-service-lb        LoadBalancer   172.25.221.66   <pending>     5701:30000/TCP   9m25s
management-center-service   ClusterIP      None            <none>        8080/TCP         9m25s
```

```bash
oc get route
```

Output:

```console
NAME                        HOST/PORT                                        PATH   SERVICES                    PORT   TERMINATION   WILDCARD
hazelcast-service-lb        hazelcast-service-lb-crc.apps-crc.testing               hazelcast-service-lb        5701                 None
management-center-service   management-center-service-crc.apps-crc.testing          management-center-service   8080                 None
```

## 8. Monitor Hazelcast Management Center

**Note that Management Center is not available for Hazelcast OSS.**

**URL:** <http://management-center-service-crc.apps-crc.testing>

## 9. Client Applications


### 9.1. PadoGrid Container

You can use the included PadoGrid container as a client to the Hazelcast cluster. 

```bash
cd_k8s crc; cd bin_sh
./login_padogrid_pod
```

From inside the Padogrid pod, run the `perf_test` app as follows.

```bash
create_app
cd_app perft_test; cd bin_sh
./test_ingestion -run
```

### 9.2. External Client

To connect to the Hazelcast cluster from an external client, you must disable SmartRouting and directly connect to a single member. This is not ideal as it puts all of the client load on the connected member. CRC does not support external IPs which are required in order to enable SmartRouting. The same is true for Minikube.

If you have a Hazelcast client then you would configure its network with the load-balancer route address as shown below.

```xml
<hazelcast>
...
   <network>  
      <smart-routing>false</smart-routing>  
      <cluster-members>  
         <address>hazelcast-service-lb-crc.apps-crc.testing:30000</address>  
      </cluster-members>  
   </network>
...
</hazelcast>
```

You can use the `perf_test` app as an external client as follows. (Run it from outside the PadoGrid container):

```bash
create_app
cd_app perf_test

# Make the above changes in the etc/hazelcast-client.xml file.
vi etc/hazelcast-client.xml
```

Run `test_ingestion`:

```bash
cd_app perf_test; cd bin_sh
./test_ingestion -run
```

## 10. Teardown

```bash
# To view all resource objects:  
oc get all --selector app=hazelcast -o name

# To delete all resource objects:  
cd_k8s crc; cd bin_sh
./cleanup
```


## References

1. Hazelcast for OpenShift Example, [https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/openshift](https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/openshift).

