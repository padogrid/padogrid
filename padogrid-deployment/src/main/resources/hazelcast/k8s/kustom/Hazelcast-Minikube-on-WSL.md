# Hazelcast Minikube on WSL

This article provides installation steps for setting up `kubectl` in Windows 10 WSL with bash auto-completion to deploy Hazelcast. The steps described here are for Hyper-V and Docker Desktop.

1. Install Docker Dekstop for Windows
[https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)

2. Install WSL and kube tools as described in the link below.

[https://itnext.io/setting-up-the-kubernetes-tooling-on-windows-10-wsl-d852ddc6699c](https://itnext.io/setting-up-the-kubernetes-tooling-on-windows-10-wsl-d852ddc6699c)

3. PowerShell: Install Minikube

[https://kubernetes.io/docs/tasks/tools/install-minikube/](https://kubernetes.io/docs/tasks/tools/install-minikube/)

4. PowerShell: Start Minikube

```console
minikube start --extra-config=kubelet.authentication-token-webhook=true --memory 5120 --cpus 4 --vm-driver hyperv
```

5. PowerShell: Start Minikube Dashboard

**IMPORTANT:** *Make sure to start the dashboard at this time before taking the next step. You may not be able to start the dashboard otherwise.*

```console
minikube dashboard
```

6. WSL: Configure `kubectl`

Minikube installed on Windows has Windows paths for the certificate files. To get the Minikube address and port number, execute the following:

```console
kubectl config view
```

You need to execute the following to change the paths in the Unix form.

```console
kubectl config set-cluster minikube --server=https://<minikube-ip>:<port> --certificate-authority=/c/Users/<windows-user-name>/.minikube/ca.crt
kubectl config set-credentials minikube --client-certificate=/c/Users/<windows-user-name>/.minikube/client.crt --client-key=/c/Users/<windows-user-name>/.minikube/client.key
kubectl config set-context minikube --cluster=minikube --user=minikube
kubectl config view
kubectl config use-context minikube
kubectl get nodes
```

:exclamation: Note that the PadoGrid's `k8s` component includes path conversion scripts for your convenience. You do not need to execute the above commands individually.

The above commands are referenced from the following link:

[https://www.jamessturtevant.com/posts/Running-Kubernetes-Minikube-on-Windows-10-with-WSL/](https://www.jamessturtevant.com/posts/Running-Kubernetes-Minikube-on-Windows-10-with-WSL/)

7. WSL: Deploy Hazelcast container

```console
# Create TLS certificates for the Prometheus custom metrics API adapter
cd $HAZELCAST_KUSTOM_DIR/bin_sh
./create_certs

# Configure a service account and RBAC
cd $HAZELCAST_KUSTOM_DIR/etc
kubectl apply -k hazelcast/init/

# Create static persistent volume where we will store addon jar files
kubectl apply -k hazelcast/storage/minikube/
```

8. Powershell: Create Shared Directory

```console
# Login to the host and create a directory on the persistent volume
# and upload the addon jar files
minikube ssh
sudo mkdir -p /data/custom/plugins/v1
sudo chmod -R 777 /data
# Change password to docker
sudo passwd docker
exit

# Get minikube ip which will be used in the next step
minikube ip
```

9. WSL: Deploy Hazelcast

```console
# Upload addon jar files to the minikube host.
scp $PADOGRID_HOME/lib/v3/* $PADOGRID_HOME/plugins/v3/* docker@<minikube-ip>:/data/custom/plugins/v1/

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

## Kubernetes API Changes

Note that if you are using a Kubernetes version older than v1.16 then you will need to use the `apps/v1beta2` version for Prometheus. You can make the changes in the following file:

```console
vi custom-metrics/prometheus/prometheus-dep.yaml

# Change apps/v1 to apps/v1beta2 only if your Kubernetes version is older than v1.16.
apiVersion: apps/v1beta2
```

See Kubernetes API change details in the following link:

[https://kubernetes.io/blog/2019/09/18/kubernetes-1-16-release-announcement/](https://kubernetes.io/blog/2019/09/18/kubernetes-1-16-release-announcement/)

## Troubleshooting Guide

### Executing `minikube dashboard` gets stuck with the following message:

```console
* Verifying dashboard health ...
```

**Solution:** This is due to the WSL Minikube file paths set in one of the above steps. You can set the paths using `kubectl.exe` from PowerShell or reinstall Minikube and make sure to execute `minikue dashboard` before configuring `kubectl` in WSL.

To configure paths from PowerShell:

```console
kubectl.exe config set-cluster minikube --server=https://<minikube-ip>:<port> --certificate-authority=c:\Users\<windows-user-name>\.minikube\ca.crt
kubectl.exe config set-credentials minikube --client-certificate=c:\Users\<windows-user-name>\.minikube\client.crt --client-key=c:\Users\<windows-user-name>\.minikube\client.key
kubectl.exe config set-context minikube --cluster=minikube --user=minikube
kubectl.exe config view
kubectl.exe config use-context minikube
kubectl.exe get nodes
```

### I'm unable to delete Minikube by executing `minikube delete`.

**Solution:** Stop and delete Minkube from Hyper-V Manager.

```console
minikube delete
erase C:\Users\<user>\.minikube\config\config.json 
minikube start --extra-config=kubelet.authentication-token-webhook=true --memory=5120 --cpus=4 --vm-driver=virtualbox
```
