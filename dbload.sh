#!/bin/bash
mongo test --eval "db.User.drop(); \
		   db.LinkedAccount.drop(); \
		   db.TokenAction.drop(); \
		   db.SecurityRole.drop(); \
		   db.Follows.drop(); \
		   db.Followed.drop(); \
		   db.Photo.drop(); \
		   db.Photo_Content.drop(); \
		   db.Map.drop(); \
		   db.Route.drop(); \
		   db.RouteLike.drop(); \
		   db.FeatureLike.drop(); \
		   db.User2Routes.drop(); \
		   db.Message.drop(); \
		   db.Discussion.drop(); \
		   db.SessionToken.drop(); \
		   db.User2Discussion.drop(); \
		   db.UserStatistics.drop(); \
		   db.Award.drop(); "
mongoimport --db test --collection SecurityRole --file ./preload/securityRoles.json
mongoimport --db test --collection User --file ./preload/users.json
mongoimport --db test --collection LinkedAccount --file ./preload/linkedAccounts.json
mongoimport --db test --collection Award --file ./preload/awards.json
psql -d umappin -U gisuser -c 'delete from osmnodes; delete from osmways; delete from routes; delete from photos;'

