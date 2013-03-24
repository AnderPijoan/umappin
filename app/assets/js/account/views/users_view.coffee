window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersView extends Backbone.View

  follows = null
  followed = null
  refUser = null
  el: 'ul#userlist'

  initialize: ->
    @listenTo @collection, 'reset ', @render
    @follows = @options.follows
    @followed = @options.followed
    @refUser = @options.refUser

  render: ->
    follows = Account.FollowCollection.getFollow @follows, @refUser.get 'id'
    followed = Account.FollowCollection.getFollow @followed, @refUser.get 'id'
    @$el.html ''
    _this = @
    @collection.each (row) ->
      if row.get('id') != _this.refUser.get "id"
        usrFollowed = Account.FollowCollection.getFollow _this.followed, row.get 'id'
        view = new Account.UserRowView model: row, follows: follows, followed: followed, usrFollowed: usrFollowed
        _this.$el.append view.render().el
    @

