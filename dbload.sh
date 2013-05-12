#!/bin/bash
mongo test --eval "db.User.drop(); \
		   db.LinkedAccount.drop(); \
		   db.TokenAction.drop(); \
		   db.SecurityRole.drop(); \
		   db.Follows.drop(); \
		   db.Followed.drop(); \
		   db.Photo.drop(); \
		   db.Photo_Content.drop(); \
		   db.Map.drop();"
mongoimport --db test --collection SecurityRole --file ./preload/securityRoles.json
mongoimport --db test --collection User --file ./preload/users.json
mongoimport --db test --collection LinkedAccount --file ./preload/linkedAccounts.json

