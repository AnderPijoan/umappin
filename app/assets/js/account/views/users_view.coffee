window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersView extends Backbone.View

  refUser = null
  userRowViews = null

  el: 'ul#userlist'

  initialize: ->
    @listenTo @collection, 'reset ', @render
    @refUser = @options.refUser

  render: ->
    @userRowViews = []
    @$el.html ''
    _this = @
    @collection.each (row) ->
      if row.get('id') != _this.refUser.get('id')
        view = new Account.UserRowView model: row, refUser: _this.refUser
        _this.userRowViews.push view
        _this.$el.append view.render().el
    @

    
    
    
    
    ###
  events:
    "click li button": "follow"
  ###  
    
    
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
