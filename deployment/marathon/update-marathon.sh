#!/bin/bash

set -e

source APPS

ENV=$1
MARATHON_BASE_URL=$2

for app in "${APPS[@]}"; do
    curl -X PUT -H "Content-Type: application/json" -d @$app-$ENV.json $MARATHON_BASE_URL/v2/apps/$app
done
