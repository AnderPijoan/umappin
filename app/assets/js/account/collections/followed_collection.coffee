window.Account or= {}

class window.Account.Followed extends Backbone.Collection

  model: Account.Follow

  url: '/followed'

  getByUserId: (id) ->
    @find (follow) ->
      follow.get('userId') == id

