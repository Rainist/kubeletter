# Kubeletter

Monitor k8s cluster by executing kubectl commands

## How it works

1. execute commands like `kubectl top nodes`
2. parse the printed results and process into data
3. compare the data with past data and make report out of it
4. deliver the report via Slack and email

## Prerequisites

kubectl configure to connect to your k8s cluster

## How to run


## Tips
- No need to provide kube related env inside k8s cluster to execute kubectl
