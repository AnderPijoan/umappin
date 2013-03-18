#!/bin/bash
mongoimport --db test --collection SecurityRole --file ./public/securityRoles.json
mongoimport --db test --collection User --file ./public/users.json
mongoimport --db test --collection LinkedAccount --file ./public/linkedAccounts.json

