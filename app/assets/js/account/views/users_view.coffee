window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersView extends Backbone.View

  el: 'div#users-list'

  template: _.template $('#userlist-template').html()

  events:
    "click li button": "follow"

  initialize: ->
    @listenTo @collection, 'change reset add remove', @render

  render: ->
    $(@el).html @template @collection.toJSON()
    @

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

  getFollow: (fcollection, id) ->
    followItem = fcollection.getByUserId id
    if followItem == undefined
      followItem = new fcollection
      followItem.set userId: id
      followItem.set follow: []
      fcollection.add followItem
    followItem

  followUser: (id) ->
    ###
    profileFollows = Account.follows.getByUserId Account.profile.id
    if profileFollows == undefined
      profileFollows = new Account.Follow
      profileFollows.set userId: Account.profile.id
      profileFollows.set follow: []
      Account.follows.add profileFollows
    ###
    profileFollows = getFollow  Account.follows, Account.profile.id
    profileFollows.get("follow").push id
    profileFollows.save()

    console.log profileFollows

  unfollowUser: (id) ->
    profileFollows = Account.follows.getByUserId Account.profile.id
    profileFollows.get("follow").splice(profileFollows.get("follow").indexOf(id), 1)
    profileFollows.save()

    console.log profileFollows