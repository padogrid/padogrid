# Installing Hazelcast on OCP or CRC

This article provides instructions for installing Hazelcast on OCP or CRC.

## Directory Contents

```console
.
├── README.md
├── bin_sh
│   ├── build_app
│   ├── cleanup
│   ├── listen_port_forward
│   ├── setenv.sh
│   ├── start_jet
│   └── stop_jet
└── templates
    └── jet
        ├── hazelcast-jet-config.yaml
        ├── hazelcast-jet-enterprise-rhel.yaml
        ├── hazelcast-jet-enterprise.yaml
        ├── hazelcast-jet.yaml
        ├── mancenter-configmap.yaml
        ├── rbac.yaml
        ├── service-lb-pods.yaml
        ├── service-lb.yaml
        ├── service-nodeport-pods.yaml
        └── service-nodeport.yaml
```

There are three (3) templates in the `template/jet` directory. The first two (2) are Hazelcast Jet Enterprise templates which require an enterprise license key. The last one is an open source Hazelcast Jet template.

* **Hazelcast Jet Enterprise RHEL:** template to deploy Hazelcast Jet Enterprise RHEL onto OpenShift Container Platform, i.e., `registry.connect.redhat.com/hazelcast/hazelcast-jet-enterprise-4-rhel8:latest`.
* **Hazelcast Jet Enterprise:** template to deploy Hazelcast Jet Enterprise onto OpenShift Container Platform, i.e., `hazelcast/hazelcast-jet-enterprise:latest`.
* **Hazelcast Jet:** template to deploy Hazelcast Jet onto OpenShift, i.e., `hazelcast/hazelcast-jet:latest`.

## 1. Build Local Environment

Run `build_app` which initializes your local environment. This script creates a new OpenShift project using the k8s cluster name. The example shown in this article uses the environment variable **PROJECT_NAME** for both the k8s cluster mame and project name.

```bash
# "myocp" is used as the project name throughout this article
export PROJECT_NAME="myocp"

# Build app
cd_k8s $PROJECT_NAME; cd bin_sh
./build_app
```

The `build_app` script creates the `jet` directory containing Kubernetes `.yaml` files. By default, `build_app` configures three (3) Hazelcast Jet members. You can change the number of members, the node port numbers, etc. by editing the `setenv.sh` file.

## 2. Create Hazelcast Enterprise RHEL regitry secret

_You can skip this step and go to [Step #5](#5-start-hazelcast) if you are running Hazelcast OSS._

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

## 3. Start Hazelcast Jet

```bash
cd_k8s $PROJECT_NAME; cd bin_sh

# To start default (Enterprise or OSS depending on your workspace environment)
./start_jet

# To start OSS:
./start_jet -oss

# To start RHEL container
./start_jet -rhel
```

The `start_jet` starts a Hazelcast cluster with one (10) headless cluster IP service and three (3) pod services exposed as follows.

```bash
oc get svc
```

Output:

```console
NAME                            TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
hazelcast-jet-service           ClusterIP      None             <none>        5701/TCP         121m
hazelcast-jet-service-0         LoadBalancer   172.25.184.136   <pending>     5701:30201/TCP   121m
hazelcast-jet-service-1         LoadBalancer   172.25.209.16    <pending>     5701:30202/TCP   121m
hazelcast-jet-service-2         LoadBalancer   172.25.176.68    <pending>     5701:30203/TCP   121m
hazelcast-jet-service-lb        LoadBalancer   172.25.7.137     <pending>     5701:30200/TCP   121m
management-center-service       ClusterIP      None             <none>        8081/TCP         121m
```

You can use the load balancer services to connect your client applications to the Hazelcast Jet cluster.

```bash
oc get route
```

Output:

```console
NAME                            HOST/PORT                                          PATH   SERVICES                        PORT        TERMINATION   WILDCARD
hazelcast-jet-service-0         hazelcast-service-0-myocp.apps-crc.testing                hazelcast-jet-service-0         jet                       None
hazelcast-jet-service-1         hazelcast-service-1-myocp.apps-crc.testing                hazelcast-jet-service-1         jet                       None
hazelcast-jet-service-2         hazelcast-service-2-myocp.apps-crc.testing                hazelcast-jet-service-2         jet                       None
hazelcast-jet-service-lb        hazelcast-service-lb-myocp.apps-crc.testing               hazelcast-jet-service-lb        5701                      None
management-center-service       management-center-service-myocp.apps-crc.testing          management-center-service       8081                      None
```

:exclamation: The `oc` executable version (4.5.9) used for writing this article has a bug that does not properly parse numeric parameters. The following error message is seen if ${HALZELCAST_JET_REPLICAS} is kept in the `jet/hazelcast.yaml` file. 

```console
error: v1.StatefulSet.Spec: v1.StatefulSetSpec.Replicas: readUint32: unexpected character: �, error found in #10 byte of ...|eplicas":"3","select|..., bigger context ...|emplate"},"name":"hazelcast"},"spec":{"replicas":"3","selector":{"matchLabels":{"app":"hazelcast","r|...
```

To prevent the error, `${HAZELCAST_JET_REPLICAS}` has been replaced with  the numeric value of 3 in the `jet/hazelcast-jet*.yaml` file as follows.

```yaml
...  
- apiVersion: apps/v1  
  kind: StatefulSet  
  metadata:  
    name: hazelcast-jet
    labels:  
      app: hazelcast-jet
  spec:  
    replicas: 3  
...
```

## 5. Monitor Hazelcast Management Center

**Note that Management Center is not available for Hazelcast OSS.**

**URL:** <http://management-center-service-myocp.apps-crc.testing>

## 6. Submit Jobs

You can listen on a port locally by forwarding to the port in one of the Jet pods by running `listen_forward_port` as follows:

```bash
cd_k8s $PROJECT_NAME; cd bin_sh
# Use pod 0 and localhost port 5701
./listen_forward_port

# You can also specify the pod number and port number, e.g.,
# use pod 1 and localhost port 5702
./listen_forward_port 1 5702
```

Once you have `listen_forward_port` running, you can submit jobs as follows:

```bash
# Submit the hello-world example included in the Jet distribution
jet --targets $PROJECT_NAME@localhost:5701 submit $JET_HOME/examples/hello-world.jar
```

## 7. Teardown

```bash
# To view all resource objects:  
oc get all --selector app=hazelcast-jet -o name

# To delete all resource objects:  
cd_k8s $PROJECT_NAME; cd bin_sh
./cleanup
```
