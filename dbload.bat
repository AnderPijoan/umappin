mongo test --eval "db.User.drop()"
mongo test --eval "db.LinkedAccount.drop()"
mongo test --eval "db.TokenAction.drop()"
mongo test --eval "db.SecurityRole.drop()"
mongo test --eval "db.Follows.drop()"
mongo test --eval "db.Followed.drop()"
mongo test --eval "db.Photo.drop()"
mongo test --eval "db.Photo_Content.drop()"
mongo test --eval "db.Map.drop()"
mongo test --eval "db.Route.drop()"
mongo test --eval "db.RouteLike.drop()"
mongo test --eval "db.FeatureLike.drop()"
mongo test --eval "db.User2Routes.drop()"
mongo test --eval "db.Message.drop()"
mongo test --eval "db.Discussion.drop()"
mongo test --eval "db.SessionToken.drop()"
mongo test --eval "db.User2Discussion.drop()"
mongo test --eval "db.UserStatistics.drop()"
mongo test --eval "db.Award.drop()"
mongoimport --db test --collection SecurityRole --file ./preload/securityRoles.json
mongoimport --db test --collection User --file ./preload/users.json
mongoimport --db test --collection LinkedAccount --file ./preload/linkedAccounts.json
mongoimport --db test --collection Award --file ./preload/awards.json
