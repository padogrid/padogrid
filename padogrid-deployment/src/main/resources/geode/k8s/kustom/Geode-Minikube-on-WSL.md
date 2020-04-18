# Geode Minikube on WSL

This article provides installation steps for setting up `kubectl` in Windows 10 WSL with bash auto-completion to deploy Geode. The steps described here are for Hyper-V and Docker Desktop.

1. Install Docker Desktop for Windows
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

## Troubleshooting Guide

### Executing `minikube dashboard` gets stuck with the following message:

```console
* Verifying dashboard health ...
```

**Solution:** This is due to the WSL Minikube file paths set in one of the above steps. You can set the paths using `kubectl.exe` from PowerShell or reinstall Minikube and make sure to execute `minikue dashboard` before configuring `kubectl` in WSL.

To configure paths from Powershell:

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
