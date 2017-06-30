#!/bin/bash

set -e

VERSION=$1

ENVS=( staging uat production )
source APPS

for env in "${ENVS[@]}"; do
    for app in "${APPS[@]}"; do
        cat $app-$env.json | jq ".container.docker.image = \"registry.banno-internal.com/$app:$VERSION\"" > $app-$env.json.updated
        mv $app-$env.json.updated $app-$env.json
    done
done
