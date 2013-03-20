window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersView extends Backbone.View

  refUser = null

  userRowViews = []

  el: 'ul#userlist'
  ###
  events:
    "click li button": "follow"
  ###
  constructor: (@collection, @userRef) ->

  initialize: ->
    @listenTo @collection, 'change reset add remove', @render
    @collection.each (row) ->
      @userRowViews.push new Account.UserRowView row, @refUser


  render: ->
    @userRowViews.each (view) ->
       @$el.append view.render().el
    @

    ###
  follow: (event) ->
    elm = event.target
    tmp = $(elm).attr('id').split('_')
    fusr_id = tmp[tmp.length - 1]
    if not $(elm).text().contains "Unfollow"
      $(elm).text "Unfollow"
      @followUser fusr_id
    else
      $(elm).text "Follow"
      @unfollowUser fusr_id

  checkFollow: (followItem, id) ->
    if followItem == undefined
      followItem = new Account.Follow
      followItem.set userId: id
      followItem.set follow: []
    followItem

  getFollows: (id) ->
    followItem = Account.follows.getByUserId id
    followItem = @checkFollow followItem, id
    Account.follows.add followItem
    followItem

  getFollowed: (id) ->
    followItem = Account.followed.getByUserId id
    followItem = @checkFollow followItem, id
    Account.followed.add followItem
    followItem

  followUser: (id) ->
    profileFollows = @getFollows Account.profile.id
    profileFollows.get("follow").push id
    # Here  to decide whether to use local/session storage as cache
    profileFollows.save()

    console.log profileFollows

  unfollowUser: (id) ->
    profileFollows = @getFollows Account.profile.id
    profileFollows.get("follow").splice(profileFollows.get("follow").indexOf(id), 1)
    # Here  to decide whether to use local/session storage as cache
    profileFollows.save()

    console.log profileFollows
  ###
