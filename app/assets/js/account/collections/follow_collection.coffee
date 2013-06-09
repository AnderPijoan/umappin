window.Account or= {}

class window.Account.FollowCollection extends Backbone.Collection

  getByUserId: (id) ->
    @find (follow) ->
      follow.get('userId') == id

  @getFollow: (coll, id) ->
    follow = coll.getByUserId id
    if follow == undefined
      follow = new Account.Follow
      follow.set userId: id
      follow.set follow: []
      coll.add follow
      follow.save()# success: (resp) -> follow.set id: resp.id
    follow
