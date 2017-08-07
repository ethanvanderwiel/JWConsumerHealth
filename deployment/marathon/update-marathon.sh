#!/bin/bash

set -e

source APPS

ENV=$1
MARATHON_BASE_URL=$2

# If environment variable for ldap user/pass is not set, read from stdin
marathon_username=$MARATHON_LDAP_USERNAME
marathon_password=$MARATHON_LDAP_PASSWORD
if [ -z "$marathon_username" ]; then
    echo "Enter your LDAP username"
    marathon_username=`read user && echo -n $user`
fi

if [ -z "$marathon_password" ]; then
    echo "Enter your LDAP password"
    marathon_password=`read pass && echo -n $pass`
fi

for app in "${APPS[@]}"; do
    curl -X PUT -H "Content-Type: application/json" --user "$marathon_username":"$marathon_password" -d @$app-$ENV.json $MARATHON_BASE_URL/v2/apps/$app
done
