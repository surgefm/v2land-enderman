#!/bin/bash

docker --version  # document the version travis is using

pip install --user awscli # install aws cli w/o sudo

export PATH=$PATH:$HOME/.local/bin # put aws in the path

echo 'Log in...'
eval $(aws ecr get-login --no-include-email --region us-east-1) #needs AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY envvars

docker tag enderman:latest 098553434883.dkr.ecr.us-east-1.amazonaws.com/enderman:latest

echo 'Pushing...'
docker push 098553434883.dkr.ecr.us-east-1.amazonaws.com/enderman:latest

