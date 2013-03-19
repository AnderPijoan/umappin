window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersView extends Backbone.View

  el: 'div#users-list'

  template: _.template $('#userlist-template').html()

  events:
    "click li button": "follow"

  initialize: ->
    @listenTo @collection, 'add', @render

  render: ->
    $(@el).html @template @collection.toJSON()
    @

  follow: (event) ->
    elm = event.target
    tmp = $(elm).attr('id').split('_')
    fusr_id = tmp[tmp.length - 1]
    if $(elm).text() != "Unfollow"
      $(elm).text "Unfollow"
      @followUser fusr_id
    else
      $(elm).text "Follow"
      @unfollowUser fusr_id

  followUser: (id) ->
    profileFollows = Account.follows.getByUserId Account.profile.id
    console.log profileFollows
    if profileFollows == undefined
      profileFollows = new Account.Follow
      profileFollows.set userId: Account.profile.id
      profileFollows.set follow: []
      Account.follows.add profileFollows
    profileFollows.get("follow").push id
    #console.log profileFollows
    profileFollows.save()
    #console.log profileFollows

  unfollowUser: (id) ->
    profileFollows = Account.follows.get userId: Account.profile.id
    profileFollows.follow.splice(profileFollows.follow.indexOf(id), 1)
    profileFollows.save()

