# Kubeletter

Monitor k8s cluster by executing kubectl commands

## How it works

1. execute commands like `kubectl top nodes`
2. parse the printed results and process into data
3. compare the data with past data and make report out of it
4. deliver the report via Slack and email

## Prerequisites

configure `kubectl` to connect to your k8s cluster
> this is already ready in k8s containers in most cases

## How to run

### Use your local kubectl to develop

#### proxy

```$ kubectl proxy --address="0.0.0.0" --accept-hosts '.*' --reject-methods=NONE```

#### Example ENVs
> if you use docker for mac

```
KUBERNETES_SERVICE_PORT=8001
KUBERNETES_SERVICE_HOST=docker.for.mac.localhost
INDIVISUAL_COUNT_LIMIT=3 # default is 5 and -1 means no limit
```

### Running as a k8s container

Simply pass just `RUNNING_INSIDE_K8S=true` to run `kubectl` properly inside a k8s cluster
> No need to provide kube related env inside k8s cluster to execute kubectl

## Troubleshooting

### It seems like it's not working inside k8s cluster

you can debug this by entering the pod and run necessaray commands like below
- `kubectl get pod`
- `kubectl get node`
- `kubectl get hpa`
- `kubectl top node`
- `kubectl top pod`
- `kubectl cluster-info`

if any of commands above doesn't work, that's probably because your service account tied to your deployment has not enough permissions; in that case you might want to look at https://kubernetes.io/docs/reference/access-authn-authz/rbac/
