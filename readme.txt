Nyla Spring – Kubernetes Demo
=============================

This project deploys a simple Spring Boot "Hello World" application into a Minikube Kubernetes cluster.
It demonstrates:
- real load balancing across multiple pods
- rolling updates with zero downtime
- container image versioning
- cleanup and redeployment
- experimentation using both Kind and Minikube


------------------------------------------------------------
🚀 MAIN STEPS (Final Working Flow)
------------------------------------------------------------

1️⃣ Start Minikube
-----------------
minikube start
kubectl get nodes


2️⃣ Deploy Application
----------------------
kubectl apply -f k8s/app.yaml
kubectl -n hello get deploy,svc,pods -o wide

# Sanity check — My first attempt kept failing due to image not being pushed
kubectl -n hello get pods


3️⃣ Enable LoadBalancer Access
------------------------------
# Open a new terminal and run (keep it open):
sudo minikube tunnel


4️⃣ Get the Service IP
----------------------
kubectl -n hello get svc nyla-spring -w
# Note the EXTERNAL-IP (often 127.0.0.1)


5️⃣ Test the Application
------------------------
for i in {1..10}; do curl -s http://127.0.0.1/hello; echo; done


6️⃣ Update to a New Version (Rolling Update)
--------------------------------------------
export USERNAME=samhendricksen
export APP=nyla-spring
export NEW_TAG=1.0.4   # bump this tag for each new version

# Build and push new Docker image
docker build -t $USERNAME/$APP:$NEW_TAG .
docker push $USERNAME/$APP:$NEW_TAG

# Update the running deployment
kubectl -n hello set image deployment/$APP app=$USERNAME/$APP:$NEW_TAG
kubectl -n hello rollout status deployment/$APP


7️⃣ Verify Update
-----------------
kubectl -n hello get pods -o wide
for i in {1..6}; do curl -s http://127.0.0.1/hello; echo; done


✅ Expected output (shows multiple pods & new version):
------------------------------------------------------
Hello, Kubernetes 1.0.4! From pod: nyla-spring-85fc4fdc9d-vzqp6
Hello, Kubernetes 1.0.4! From pod: nyla-spring-85fc4fdc9d-pxv48
Hello, Kubernetes 1.0.4! From pod: nyla-spring-85fc4fdc9d-g2k7f
Hello, Kubernetes 1.0.4! From pod: nyla-spring-85fc4fdc9d-q4n8t
Hello, Kubernetes 1.0.4! From pod: nyla-spring-85fc4fdc9d-vzqp6


8️⃣ Clean Up
------------
kubectl delete -f k8s/app.yaml
minikube delete


------------------------------------------------------------
🧠 NOTES & ITERATIONS (Show Your Work)
------------------------------------------------------------

I explored several iterations before landing on this working approach:

- KIND:
  Initially used Kind ("kind create cluster --name hello").
  It worked with "kubectl port-forward" but Kind does not automatically
  load balance via Service URLs — port-forward always hits the same pod.

- MINIKUBE:
  Switched to Minikube for built-in LoadBalancer simulation via "minikube tunnel".
  Simplified configuration down to a single app.yaml (Namespace + Deployment + Service).
  Verified true load balancing using multiple pods responding differently to /hello.


------------------------------------------------------------
🧪 PREVIOUS COMMANDS & EXPERIMENTS
------------------------------------------------------------

# Run using prebuilt Docker image
docker pull samhendricksen/nyla-spring:1.0.1
docker run --rm -p 8080:8080 samhendricksen/nyla-spring:1.0.1
curl http://localhost:8080/hello


# Early Kubernetes local run (Kind)
kind create cluster --name hello
kubectl config use-context kind-hello
kubectl apply -f k8s/namespace.yaml
kubectl -n hello apply -f k8s/deployment.yaml -f k8s/service.yaml
kubectl -n hello port-forward svc/nyla-spring 8080:80
curl http://localhost:8080/hello
# → worked, but only reached one pod (no load balancing)


# Minikube experiment (before final version)
minikube start
kubectl create namespace hello
kubectl -n hello create deployment nyla-spring \
  --image=samhendricksen/nyla-spring:1.0.2 --port=8080
kubectl -n hello expose deployment nyla-spring \
  --type=LoadBalancer --port=80 --target-port=8080
minikube service -n hello nyla-spring --url
curl <url>/hello   # → still hit one pod only
kubectl -n hello patch svc nyla-spring -p '{"spec":{"type":"LoadBalancer"}}'
sudo minikube tunnel
kubectl -n hello get svc nyla-spring -w
for i in {1..10}; do curl -s http://127.0.0.1/hello; echo; done


# Deleted and reran from the beginning with minikube


brew install kind
kubectl delete -f k8s/
kubectl get all -A | grep nyla-spring
kind delete cluster --name hello

docker build -t samhendricksen/nyla-spring:1.0.2 .
docker push samhendricksen/nyla-spring:1.0.2

# Clone repo
git clone https://github.com/<your-repo>/nyla-spring.git
cd nyla-spring

# Build jar and image
docker build -t samhendricksen/nyla-spring:1.0.1 .

# Test locally
docker run --rm -p 8080:8080 samhendricksen/nyla-spring:1.0.1
# Visit http://localhost:8080/hello

Hello, Kubernetes! from pod <container-id>


------------------------------------------------------------
🧹 CLEANUP & RESET COMMANDS
------------------------------------------------------------
kubectl delete -f k8s/app.yaml
minikube delete


------------------------------------------------------------
💡 KEY LEARNINGS
------------------------------------------------------------

- "kubectl port-forward" and "minikube service --url" connect directly to one pod (no load balancing).
- "minikube tunnel" enables real LoadBalancer behavior via Service EXTERNAL-IP.
- Rolling updates with "kubectl set image" + "rollout status" demonstrate zero-downtime deployment.
- Consolidating to "app.yaml" keeps manifests simpler to manage.
- This setup mirrors a real production workflow while remaining lightweight.


------------------------------------------------------------
SUMMARY
------------------------------------------------------------
This project demonstrates:
- containerization and image versioning
- Kubernetes Deployments, Services, and health probes
- replicas and load balancing
- rolling updates
- cleanup and redeployment
All using Minikube for local testing and validation.
