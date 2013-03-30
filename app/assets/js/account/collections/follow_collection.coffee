window.Account or= {}

class window.Account.FollowCollection extends Backbone.Collection

  model: Account.Follow

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
      follow.save() # Here  to decide whether to use local/session storage as cache
    follow
