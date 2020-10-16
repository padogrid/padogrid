# Installing Hazelcast on OCP or CRC

This article provides instructions for installing Hazelcast on OCP or CRC.

## Directory Contents

```console
.
├── README.md
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
│   ├── hazelcast-enterprise.yaml0
│   ├── hazelcast.yaml
│   ├── rbac.yaml
│   ├── service-lb.yaml
│   ├── service-nodeport.yaml
│   ├── service-pods-nodeports.yaml
│   ├── service-pods.yaml
│   └── wan
│       ├── hazelcast-enterprise-rhel.yaml
│       └── hazelcast-enterprise.yaml
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

Run `build_app` which initializes your local environment. This script creates a new OpenShift project using the k8s cluster name. The example shown in this article uses the environment variable **PROJECT** for both the k8s cluster mame and project name.

```bash
# "myocp" is used as the project name throughout this article
export PROJECT="myocp"

# Build app
cd_k8s $PROJECT; cd bin_sh
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
cd_k8s $PROJECT; cd padogrid
oc create -f pv-hostPath.yaml
```

## 3. Add User to `nonroot` SCC (Security Context Constraints)

PadoGrid runs as a non-root user (padogrid/1001) that requires read/write permissions to the persistent volume. Let's add your project's default user to the `nonroot` SCC.

You can use one of the following methods to add the user to `nonroot` SSC.

### 3.1. Using Editor

```bash
oc edit scc nonroot
```

**nonroot SCC:**

Add your project under the`users:` section. For our example, since our project name is **myocp**, we would enter the following.

```yaml
users:
- system:serviceaccount:myocp:default
```

### 3.2. Using CLI

```bash
# See if user can use nonroot
oc adm policy who-can use scc nonroot

# Add user
oc adm policy add-scc-to-user nonroot system:serviceaccount:myocp:default
```

:exclamation: Note that as of **oc v4.5.9**, `oc get scc nonroot -o yaml` will not show the user you added using CLI. This is also true for the user added using the editor, which will not be shown in the output of `oc adm policy who-can use scc nonroot`.

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
cd_k8s $PROJECT; cd bin_sh
./start_hazelcast
```

The `start_hazelcast` starts a Hazelcast cluster with one (10) headless cluster IP service and three (3) pod services exposed as follows.

```bash
oc get svc
```

Output:

```console
NAME                        TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
hazelcast-service           ClusterIP      None             <none>        5701/TCP         121m
hazelcast-service-0         LoadBalancer   172.25.184.136   <pending>     5701:30001/TCP   121m
hazelcast-service-1         LoadBalancer   172.25.209.16    <pending>     5701:30002/TCP   121m
hazelcast-service-2         LoadBalancer   172.25.176.68    <pending>     5701:30003/TCP   121m
hazelcast-service-lb        LoadBalancer   172.25.7.137     <pending>     5701:30000/TCP   121m
management-center-service   ClusterIP      None             <none>        8080/TCP         121m
```

You can use the load balancer services to connect your client applications to the Hazelcast cluster.

```bash
oc get route
```

Output:

```console
NAME                        HOST/PORT                                          PATH   SERVICES                    PORT        TERMINATION   WILDCARD
hazelcast-service-0         hazelcast-service-0-myocp.apps-crc.testing                hazelcast-service-0         hazelcast                 None
hazelcast-service-1         hazelcast-service-1-myocp.apps-crc.testing                hazelcast-service-1         hazelcast                 None
hazelcast-service-2         hazelcast-service-2-myocp.apps-crc.testing                hazelcast-service-2         hazelcast                 None
hazelcast-service-lb        hazelcast-service-lb-myocp.apps-crc.testing               hazelcast-service-lb        5701                      None
management-center-service   management-center-service-myocp.apps-crc.testing          management-center-service   8080                      None
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
cd_k8s $PROJECT; cd bin_sh

# If you have not created local-storage
./start_padogrid
```

## 7. Monitor Hazelcast Management Center

**Note that Management Center is not available for Hazelcast OSS.**

**URL:** <http://management-center-service-myocp.apps-crc.testing>

## 8. Client Applications

### 8.1. PadoGrid Container

You can use the included PadoGrid container as a client to the Hazelcast cluster. 

```bash
cd_k8s $PROJECT; cd bin_sh
./login_padogrid_pod
```

From inside the Padogrid pod, run the `perf_test` app as follows.

```bash
create_app
cd_app perft_test; cd bin_sh
./test_ingestion -run
```

### 8.2. External Client

To connect to the Hazelcast cluster from an external client, you have two choices: Dummy Client and Smart Client. Dummy Client connects to a single Hazelcast member via a load balancer service whereas SmartClient connects to all Hazelcast members via the Hazelcast Kubernetes discovery service.

#### 8.2.1. Dummy Client

```xml
<hazelcast>
...
   <network>  
      <smart-routing>false</smart-routing>  
      <cluster-members>  
         <address>hazelcast-service-lb-myocp.apps-crc.testing:30000</address>  
      </cluster-members>  
   </network>
...
</hazelcast>
```

#### 8.2.2. Smart Client

To run Smart Client, each pod must be reachable via an external IP. The `start_hazelcast` script has already exposed the required node ports as described in [Section 5](#5-Start-Hazelcast). Note that even though the node ports are exposed, the OpenShift cluster may have been configured to prevent them from being externally exposed. If so, Smart Client cannot be used. Unfortunately, this is the case for CRC. Please check with your OpenShift administrator.


We also need the master node address.

```bash
oc cluster-info
```

Output:

```console
Kubernetes master is running at https://api.crc.testing:6443

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'
```

The Kubernetes discovery service uses the Kubernetes API to gather Hazelcast external IPs. To use the Kubernetes API, we need to provide the storage class token and certificate information. The `start_hazelcast` script has already created the cluster role binding on the `default` service account (see `hazelcast/rbac.yaml`).

```bash
oc get secrets | grep default-token
```

Output:

```console
default-token-w7k8h        kubernetes.io/service-account-token   4      53m
default-token-xgvkk        kubernetes.io/service-account-token   4      53m
```

Use one of the default tokens to get api-token and ca-certificate.

```bash
# Select the first token in the list
export DEFAULT_TOKEN=$(oc get secrets | grep default-token | awk 'NR==1{print $1}')

# Output base64 - echo is for adding next line
oc get secret $DEFAULT_TOKEN -o jsonpath={.data.token} | base64 --decode && echo
oc get secret $DEFAULT_TOKEN -o jsonpath={.data.ca\\.crt} | base64 --decode
```

Enter the master URI, encoded token, and certificate in the `hazelcast-client.xml` file as shown below. Note that the service name is `hazelcst-service-lb` which is created when you started Hazelcast.

```xml
<hazelcast>
...
   <network>
      <smart-routing>true</smart-routing>
      <kubernetes enabled="true">
         <namespace>myocp</namespace>
         <service-name>hazelcast-service-lb</service-name>
         <use-public-ip>true</use-public-ip>
         <kubernetes-master>https://api.crc.testing:6443</kubernetes-master>
         <api-token>
eyJhbGciOiJSUzI1NiIsImtpZCI6IlJpUmFaUTlnNnVTMkhwY3pkMnpvRndFVWtiRnVodDVTOUVVRnNwaUtEYlEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJvY3AiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiZGVmYXVsdC10b2tlbi1obmp3bSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiZGEzYzBhMjUtMmYwZS00MTBiLTg5MWQtYzA0NWI3Yjc2OWRkIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50Om9jcDpkZWZhdWx0In0.jk8piLb3nSzm2lvCDrX9q4MQ1vKqK8tuVF0v9KzWy29BpGkR6xkubgFRL12ORYY-bKngGlAUORLD-9Hb8vE3TQoj63tKHhmZqwwDcDA4Wpiptj_CKZXDbKlWVF9qyZgrSaURwtpB0kHNUl5kSWpVfYHicC5tgn7lqTPN2PusGJ0h_Njiwb0WXw1ZT6u9qCWZLjZ3V0z5iEiQ9TZ3s3XlOwEbbXPjx3D-lPfoVwkTPX0NIRTlBRi9QRmTGl83-G6tDFH9VcKEZshAEvvBb20z3ogG-GmhfTiJRtJwWkVpdjMaAxHRI2bnZAgCoxWFriVPXjaf4gM_A6_XwWgPm4U3ow
         </api-token>
         <ca-certificate>
-----BEGIN CERTIFICATE-----
MIIDMjCCAhqgAwIBAgIILdhdsXZP8TgwDQYJKoZIhvcNAQELBQAwNzESMBAGA1UE
CxMJb3BlbnNoaWZ0MSEwHwYDVQQDExhrdWJlLWFwaXNlcnZlci1sYi1zaWduZXIw
HhcNMjAwOTEzMDkwNjM3WhcNMzAwOTExMDkwNjM3WjA3MRIwEAYDVQQLEwlvcGVu
c2hpZnQxITAfBgNVBAMTGGt1YmUtYXBpc2VydmVyLWxiLXNpZ25lcjCCASIwDQYJ
KoZIhvcNAQEBBQADggEPADCCAQoCggEBAKf+/tSjVFWXsib7Cv9/2vJgWsxmeu2B
Sujd890lL4SyzvhQ7+8wad6Y2u7iJrwXZkOpPOUSqshbVtM2HC7oM2WMRjfZtEbw
rjZIT8yELHbfNP4Yc0u1tvlO7A/GmO+No2Yzgdh2+8PuYFIaJiahsCwO9I1yMI21
qQ1352w29eX0VBJezBGgDcTfbe/uRZGswL9mJV+bReCGTW1PwpbdEm3DetUP2ZWJ
2uvzpq21aF222RpaX7BU3S2BlzNk8XAfyvBmNeEVrYRwUH5s9z1Ogc7VtBc9rLKf
G/hsrRuJj+ISuLD4TI/Z8VzBoNz0hUMm1N7CUBgWcUFdNfOH0ncjk5cCAwEAAaNC
MEAwDgYDVR0PAQH/BAQDAgKkMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFJpR
P3bPEGPHn3XlXprBZl+ZZJq6MA0GCSqGSIb3DQEBCwUAA4IBAQAqmwnooXt+YL2m
j+d/ClhUGSXfv1iAixnCiqo6MnVuax8fLT3vGkYl+xwg0YojwqZZtr+IOcabLkAd
g3BudYgGWHUGfF/SiuzNeHoH6iYuSYtD6pJ/Sm8J70JRRDRuHieVoxzXhPN604N5
Ve0pRXq1QDvbBuWOKbxDY7qX7ryH5j7fGWb57U7DVcceq38/0wXIzcxazPuoqjjl
bVtkrTMbe1gmMDu/AcifTMjRHbcMjWK8nGZnh+Aj9O3Lfi8JoIETEGDJE4hChvBu
LAbPiPgIUoRkT4901lj6rG611juCCaGQvQH1k4iAkUNaiC929m5MQtwu7lm0hj2p
hdFzNXwJ
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
MIIDQDCCAiigAwIBAgIINjXPMbDj3CswDQYJKoZIhvcNAQELBQAwPjESMBAGA1UE
CxMJb3BlbnNoaWZ0MSgwJgYDVQQDEx9rdWJlLWFwaXNlcnZlci1sb2NhbGhvc3Qt
c2lnbmVyMB4XDTIwMDkxMzA5MDYzN1oXDTMwMDkxMTA5MDYzN1owPjESMBAGA1UE
CxMJb3BlbnNoaWZ0MSgwJgYDVQQDEx9rdWJlLWFwaXNlcnZlci1sb2NhbGhvc3Qt
c2lnbmVyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo5/SCu5N9aQJ
1+oPf22zQoqcvST1JDog9lqE7gAhGf47RBSyaHCoeBsYA6Si0pJwV5QOmT/JAjVD
sLhl6WPI1wmYMxET83m2qai/IQ4TFA7L0y3jhSt7CUN6CVrbIGchUYyaE+KMn+P5
V2Oz5ZP12/pctiT5OdyAS/ycdZN3+sXSx6YeUEGjWHZVg0mJbgtopBpGS8qRcUOz
xJDii5mTfHFCMHtC9QZTgfA54xPy6RfMRwAhQLkitSHg3B/12s1xmkLVau+Lu6EE
ZwQjq+3bIwrncw1XIL3bZYiYoB2DyrnYSlrt+x+riu+AWY+cp29iiE2Gmv+xW8gd
cxMnhHe/GwIDAQABo0IwQDAOBgNVHQ8BAf8EBAMCAqQwDwYDVR0TAQH/BAUwAwEB
/zAdBgNVHQ4EFgQULro9Rm7ebM8zGNDQ7S/Stkb/31EwDQYJKoZIhvcNAQELBQAD
ggEBAJAl0vxLKTEJyfqnMcqcGZwJfnsq3MufYk9YNVJeMGtwnkq3AIAz3JXltycd
5XzcrR9o1qFsmJmUZOZmvsagT89BfAACHiGw2LbhyahxOI3+I5hyKc13DE9XyLaT
6F446n8HGeVUAu1mndhGuH8op5nmHC0rnbcEBtKpmYmTFiIFjDIdBtYS7z/HdQLc
ByL0vAqtaGBlHtjP/pym2YC5yaBIIBlyQI1OuqIxcvwifzrtn5Sjz5X2HXJ6L/42
d7XrZ7aYJ17EyOYU8r4NmZ9PhLI4Ij/bcH5rx3Er+AiAYX6V8K6GAY5FHptrpme1
dzqKVBNxHS0BqGroY5dG4sv5gAg=
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
MIIDTDCCAjSgAwIBAgIIXGajSvyEHtQwDQYJKoZIhvcNAQELBQAwRDESMBAGA1UE
CxMJb3BlbnNoaWZ0MS4wLAYDVQQDEyVrdWJlLWFwaXNlcnZlci1zZXJ2aWNlLW5l
dHdvcmstc2lnbmVyMB4XDTIwMDkxMzA5MDYzN1oXDTMwMDkxMTA5MDYzN1owRDES
MBAGA1UECxMJb3BlbnNoaWZ0MS4wLAYDVQQDEyVrdWJlLWFwaXNlcnZlci1zZXJ2
aWNlLW5ldHdvcmstc2lnbmVyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
AQEAuPAngP3v9ptljdKzH2CFTnk0OWQa7AQ/UnJwP234lv8njnB/uh1FdqAfPnBI
YXFHejLn3dRAr3bxKHbw/ec+84EKdPZ71Sp42tXP/6o6Zu3hAkjQ4K6Di9eeUHq9
0CEMG0hZtFUJkbSge59xy2tzuPgknGuDpSbjF34biYsv6Jc+aEwr5Ef8iQ5yVHxN
PnKMJWErUox2OVptCjC0AqyX5PrPOwSpPgO7OHmg3IDNI5rISo9Fcn0IDh5BCJxT
jUiRK6O9p4pKhC5bVe/Jaov76Ka6WHYosO0vK47VTyubtZ0gcO4HrBf1+tPI0yfe
1r+4C4/PH8eSD4Rw6+kimI2ucwIDAQABo0IwQDAOBgNVHQ8BAf8EBAMCAqQwDwYD
VR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUt40/AGR0eeV7HhculYnPWihaUuIwDQYJ
KoZIhvcNAQELBQADggEBAKFaTMIFiyv8v5TfEnOtjViF/L18e8m8ZE8TJcAZm1k8
x+iYA7ythfSIPghr2F9s50RAnc8jZG/TTjmSaxoKY2p9fAdfzCQIUX1Zhd41yb5u
GHtOTTBTLLznJ0aGPl/sBruY2cE4PGq4/Jh4enEg8aKOxgZoLs4r5eeA4xBHLmfu
5DFw6LzptidKZO2NjQITNxTZs7UBd7CLC+TDa518Juf7PTETWsgLt5VL6JfswrSZ
uL8z5Usw1Fb6sHqVwOKT8C96gyYoPQtmBX1l+Y6LF84o7RxyyIJ2kitywKAwqcn9
oyhWGUj3My48vTYjNOqGfSWGfZLRvj6Il0USRgiIWcM=
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
MIIDlzCCAn+gAwIBAgIIN2RUrn480ZEwDQYJKoZIhvcNAQELBQAwWTFXMFUGA1UE
AwxOb3BlbnNoaWZ0LWt1YmUtYXBpc2VydmVyLW9wZXJhdG9yX2xvY2FsaG9zdC1y
ZWNvdmVyeS1zZXJ2aW5nLXNpZ25lckAxNTk5OTg5NDMyMB4XDTIwMDkxMzA5MzAz
MVoXDTMwMDkxMTA5MzAzMlowWTFXMFUGA1UEAwxOb3BlbnNoaWZ0LWt1YmUtYXBp
c2VydmVyLW9wZXJhdG9yX2xvY2FsaG9zdC1yZWNvdmVyeS1zZXJ2aW5nLXNpZ25l
ckAxNTk5OTg5NDMyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4U8/
WhjdHaUE+MRaaYIAiegjLtBtharhpHgKk3QuFwHwafvgTNdgXCNe532RUszLAyGR
w/2GYQOBIlrDDW+teN/RLdx106vXyWloBp+vq5SgxbzUH1HV/z8lAr4UK7clbg6a
WY2xCd6+F7Stw6SMuiHg7Kyj6iOloeyqZxY1uExsO5PNc2Fonz3uKih1BMkctoiH
GolkEAzH/3qaB50PCu6XuS/LQ9xbAnuiXyXPwlPEh1zF+E6ccsvuXi4Z1V5kWZxS
ZtAG4cIyp8MbEJ1OO3Zb9m6CWALql8L/pjOLsuEh9EbanlBzCUN5RPcaQYzEqObc
OPdqKAC3h/CG3THtzwIDAQABo2MwYTAOBgNVHQ8BAf8EBAMCAqQwDwYDVR0TAQH/
BAUwAwEB/zAdBgNVHQ4EFgQUgjTkWxNBJPjNMB+hK0522ckez1YwHwYDVR0jBBgw
FoAUgjTkWxNBJPjNMB+hK0522ckez1YwDQYJKoZIhvcNAQELBQADggEBAFVJ/QAK
+xxZrh2r/LybMT/UnAh+/+olcgbbZ8IlcEhgGc3AXgfTMEPBxwwjzkfEhds5z2ZI
MhTyqykBIJrSR5IVhelZLhDm7/z4eZcUU7syYFsNzApDmDFD3oeMdpubeDJ+BgcW
S+GngnsMkYdKFLLZVG8K92d5I0X5BUNcO0pfct59wx2z1tu7/X/fY6nqUyo0MJH2
b+KHKWu9Qd0dM+bX7KtCESD0R5TSFaDlPrS29AG8VUbiiEjyM+E9qX7bc9Gq8kpc
+BVGELOLxmOt0BhaAzXaYziMg2mzSL+EvGGlSrso/UeMPPLId+tAVsZk8TXKb0FY
uFIjhXqCH0pZw9I=
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
MIIDODCCAiCgAwIBAgIIRVfCKNUa1wIwDQYJKoZIhvcNAQELBQAwJjEkMCIGA1UE
AwwbaW5ncmVzcy1vcGVyYXRvckAxNTk5OTkxMDc4MB4XDTIwMDkxMzA5NTgwOFoX
DTIyMDkxMzA5NTgwOVowHTEbMBkGA1UEAwwSKi5hcHBzLWNyYy50ZXN0aW5nMIIB
IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtClYnM1FYiH+wcYiGIUnLvNi
zLWWjsqJrQ/4Snnu61HDaU5w3An9yDZojyaJdCmWUg6CKXDiCoJB+lxMFdyaolXU
dohJ9vr2wt6iuNfshmtxUwiBhI9ZBsVhztWdu3cgnUcYW8KMyUmajiEyXD8Npvba
Z4ifUsjAYE1LByZzPIkmBmKPnv0fFZu1ejgg7HuQlYmfN/pJHudWMvwZJ8b3Zhqn
sA0cDsaCDU1SJk6cQvNJFgF38+IWNhJpiQlGfFwKkal64/RYcR9yv8/pMV+7jj1z
BPI9yxbFgLuxLxlyS8Kxnz9ln/4V0ZB12qU3c6aM4Ew3uhYv8ZDSOnpTUSjrRwID
AQABo3MwcTAOBgNVHQ8BAf8EBAMCBaAwEwYDVR0lBAwwCgYIKwYBBQUHAwEwDAYD
VR0TAQH/BAIwADAdBgNVHQ4EFgQUTtNCBUyuPpf4M4YhlYBqJhZGQYYwHQYDVR0R
BBYwFIISKi5hcHBzLWNyYy50ZXN0aW5nMA0GCSqGSIb3DQEBCwUAA4IBAQBkGZiv
BqqEDKUTiifFfTxQXJzO5OBBTUDqSntrknAw0sidPgn9A8a2gGdCr7mKEH16FQ7N
1SpkjXWZghE/1pZTaS7JVcT6+Yuplfy5Reoim/Nlu8ulDDfBSSp9S9BO+Q/lFJdM
omwyIu0sSXI79ColhLirDinEmyQMDHdmTJ+kJfctpSH27L4j5osJxtNSAEABmUgX
1HQ7FYzrNmys5OcK9SlDn0vCyOTHyqBbxRnF6L9Ec2bU6KD0DvEBurBpAYGMc9ss
BD0FXdHEPg7HP0Nep79jhe10IXGrghtep3D5jUjbj1I4DGSsQ8y4df2vo6IFd96A
f0uUS/73sncN64gl
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
MIIC7TCCAdWgAwIBAgIBATANBgkqhkiG9w0BAQsFADAmMSQwIgYDVQQDDBtpbmdy
ZXNzLW9wZXJhdG9yQDE1OTk5OTEwNzgwHhcNMjAwOTEzMDk1ODAxWhcNMjIwOTEz
MDk1ODAyWjAmMSQwIgYDVQQDDBtpbmdyZXNzLW9wZXJhdG9yQDE1OTk5OTEwNzgw
ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDUSJ0nqgRixmVWTOpBvG32
lM01Ph29+C1ulLPk4Qa2HRe/MReqDmw5XrXPd+QOlswL+6h2l02p4j5hAjAnfpTS
IqJro0SKjpEzFDBrP8dkONKtmOszQRj4BalEUlLuPQKRk5Z9qegak/PN47aMr2bv
FQaSlJEg/xP67DoC7JVJjv4b9LMVhBWw5faPz+leNtulDMYC3r9uUKPOgQOjBuXH
bxcUA9VWbKxcjbBSHR+2m6kBtnYgkDGmUeb+Gm8rXzGkWvGjHh6OYHWF433QXyaJ
J3QmZD9N+PuceyS6Pc9pdT1zHLdH1hLAdUtKk7p7E1FyPdTmTtrujbYEc16LNn5n
AgMBAAGjJjAkMA4GA1UdDwEB/wQEAwICpDASBgNVHRMBAf8ECDAGAQH/AgEAMA0G
CSqGSIb3DQEBCwUAA4IBAQCeWxDHYdYpvx/X22/Kuzrx0jE7NiAhk101x6Ztnlr1
5wl5to5HMSR+jO7TyVv/RM2IE1vMOfTQQYveJYpz5WJ+62zfZfBdHBttxdOd/NDI
4S8NFKvfjuNOjet+Byx+T4KK5cXep/vvFsg1LCetOCDbTpq87GAroDeC1WxTCihC
gSSA49nvjzwHxiIa0OXLP8oWnI/x4bFUCijPZCk+4aHwdCDsFeIUwbLHlFerv3ZV
UHo4THL363aHURY9b4HoLT9lNuRh6DFoG0/EKRqX16Y/TX+gwJPdbf8dniYIpcyV
6welL9C49Zop9G4SiUU44S0Dz+XkQ8qcH7QzI2BppJkn
-----END CERTIFICATE-----
         </ca-certificate>
      </kubernetes>
   </network>
...
</hazelcast>
```

You can use the `perf_test` app as an external client as follows. (Run it from outside the PadoGrid container, i.e., on your local machine.)

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

## 9. Teardown

```bash
# To view all resource objects:  
oc get all --selector app=hazelcast -o name

# To delete all resource objects:  
cd_k8s $PROJECT; cd bin_sh
./cleanup

# Remove user from nonroot SCC
# Editor - edit scc nonroot and remove the 'system:serviceaccount:myocp:default'
# from under 'users:'
oc edit scc nonroot

# CLI
oc adm policy remove-scc-from-user nonroot system:serviceaccount:myocp:default
```

## References

1. Hazelcast for OpenShift Example, [https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/openshift](https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/openshift).
2. Hazelcast Discovery Plugin for Kubernetes, [https://github.com/hazelcast/hazelcast-kubernetes](https://github.com/hazelcast/hazelcast-kubernetes).

