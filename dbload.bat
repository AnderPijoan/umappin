mongo test --eval "db.User.drop()"
mongo test --eval "db.LinkedAccount.drop()"
mongo test --eval "db.TokenAction.drop()"
mongo test --eval "db.SecurityRole.drop()"
mongo test --eval "db.Follows.drop()"
mongo test --eval "db.Followed.drop()"

mongoimport --db test --collection SecurityRole --file ./preload/securityRoles.json
mongoimport --db test --collection User --file ./preload/users.json
mongoimport --db test --collection LinkedAccount --file ./preload/linkedAccounts.json

