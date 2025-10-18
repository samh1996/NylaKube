I had several iterations, based on finding out that kind does not automtically load balance when I attempt to use the url.
I landed on using minikube for basic testing.
As well as condensed to app.yaml insted of all three files.

# Start minikube
minikube start
kubectl get nodes

kubectl apply -f k8s/app.yaml
kubectl -n hello get deploy,svc,pods -o wide

# Sanity check - I didnt push the image the first time
kubectl -n hello get pods

# Open new terminal
sudo minikube tunnel

# Original or new terminl
kubectl -n hello get svc nyla-spring -w
> Provides ip (mine is 127.0.0.1)

# Run it
for i in {1..10}; do curl -s http://127.0.0.1/hello; echo; done

# Update it
export USERNAME=samhendricksen
export APP=nyla-spring
export NEW_TAG=1.0.4   # <- bump this

#push new build
docker build -t $USERNAME/$APP:$NEW_TAG .
docker push $USERNAME/$APP:$NEW_TAG

# Update running build
kubectl -n hello set image deployment/$APP app=$USERNAME/$APP:$NEW_TAG
kubectl -n hello rollout status deployment/$APP

# Should see old pods terminating and new one generating
kubectl -n hello get pods -o wide

# Run the test
for i in {1..6}; do curl -s http://127.0.0.1/hello; echo; done

# Clean up
kubectl delete -f k8s/app.yaml
minikube delete

---- End main steps




docker build -t samhendricksen/nyla-spring:1.0.3 .
docker push samhendricksen/nyla-spring:1.0.3



--------- Run using prebuilt image in docker
docker pull samhendricksen/nyla-spring:1.0.1
docker run --rm -p 8080:8080 samhendricksen/nyla-spring:1.0.1

-- Test it (on another terminal)
curl http://localhost:8080/hello
---------

--------- Run kubernetes cluster locally
kind create cluster --name hello
kubectl config use-context kind-hello

kubectl cluster-info # optional
kubectl get nodes #optional

kubectl apply -f k8s/namespace.yaml
kubectl get ns hello #optional
kubectl -n hello apply -f k8s/deployment.yaml -f k8s/service.yaml

kubectl -n hello get pods -o wide # pod info, such as running
kubectl -n hello get svc nyla-spring # information

kubectl -n hello port-forward svc/nyla-spring 8080:80

curl http://localhost:8080/hello

------
 notes: this is working but it is directly going to single pod

------ minekube
minikube start

kubectl create namespace hello
kubectl -n hello create deployment nyla-spring \
  --image=samhendricksen/nyla-spring:1.0.2 --port=8080
kubectl -n hello expose deployment nyla-spring \
  --type=LoadBalancer --port=80 --target-port=8080

minikube service -n hello nyla-spring --url
curl <output>/hello
---> Still only hits one node

kubectl -n hello patch svc nyla-spring -p '{"spec":{"type":"LoadBalancer"}}'
# sudo in with a different terminal
sudo minikube tunnel
kubectl -n hello get svc nyla-spring -w
for i in {1..10}; do curl -s http://127.0.0.1/hello; echo; done

----
Deleted and rerunning from the beginning with minikube






brew install kind
kubectl delete -f k8s/
kubectl get all -A | grep nyla-spring
kind delete cluster --name hello

docker build -t samhendricksen/nyla-spring:1.0.2 .
docker push samhendricksen/nyla-spring:1.0.2

# clone repo
git clone https://github.com/<your-repo>/nyla-spring.git
cd nyla-spring

# build jar and image
docker build -t samhendricksen/nyla-spring:1.0.1 .

# test locally
docker run --rm -p 8080:8080 samhendricksen/nyla-spring:1.0.1
# visit http://localhost:8080/hello

Hello, Kubernetes! from pod <container-id>

