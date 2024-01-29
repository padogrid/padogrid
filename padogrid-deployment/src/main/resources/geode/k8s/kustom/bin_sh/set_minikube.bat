:: ========================================================================
:: Copyright (c) 2020-2024 Netcrest Technologies, LLC. All rights reserved.
::
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::     http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
:: ========================================================================

REM Set the following environment variables to reflect your environment.
REM Note that the uer name must be your Windows user name.
set USERNAME=dpark

REM Minikube IP can be obtained by running 'minikube ip' or 'minikube.exe ip'.
set MINIKUBE_IP=172.17.34.252

REM Change the port number only if you changed the default Minikube port number.
set MINIKUBE_PORT=8443

kubectl config set-cluster minikube --server=https://%MINIKUBE_IP%:%MINIKUBE_PORT% --certificate-authority=c:\Users\%USERNAME%\.minikube\ca.crt
kubectl config set-credentials minikube --client-certificate=c:\Users\%USERNAME%\.minikube\client.crt --client-key=c:\Users\%USERNAME%\.minikube\client.key
kubectl config set-context minikube --cluster=minikube --user=minikube
kubectl config view
kubectl config use-context minikube
kubectl get nodes
