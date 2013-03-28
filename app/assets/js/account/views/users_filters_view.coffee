window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersFiltersView extends Backbone.View
  follows = null
  followed = null
  refUser = null
  usersListView = null
  filters = []
  textFilter = ''
  el: 'div#users-filters'
  events: 
    'change #orderFilter':           'orderBy'
    'click #followsFilter':         'filterByFollows'
    'click #followedFilter':        'filterByFollowed'
    'keyup #textFilter':           'filterByText'

  initialize: ->
    @listenTo @collection, 'reset sort', @render
    @follows = @options.follows
    @followed = @options.followed
    @refUser = @options.refUser

  filterByText: (event) ->
    @textFilter = event.target.value
    @render()

  filterByFollows: (event) ->
    @setFollowFilter event, @applyFollowsFilter

  filterByFollowed: (event) ->
    @setFollowFilter event, @applyFollowedFilter

  setFollowFilter: (event, filterAction) ->
    filterPos = filters.indexOf filterAction
    if not $(event.target).hasClass 'active'
      filters.push filterAction unless filterPos != -1
    else
      filters.splice filterPos, 1 unless filterPos == -1
    @render()

  applyFollowsFilter: (coll) ->
    @filterByFollow coll, @follows

  applyFollowedFilter: (coll) ->
    @filterByFollow coll, @followed

  filterByFollow: (coll, followColl) ->
    followList =  Account.FollowCollection.getFollow(followColl, @refUser.get 'id').get "follow"
    newCollection = new Account.Users
    coll.each (user) ->
      newCollection.add user unless followList.indexOf(user.get 'id') == -1
    newCollection

  applyFilterByText: (coll) ->
    textFilter = @textFilter
    newCollection = new Account.Users
    coll.each (user) ->
      if user.get('name').indexOf(textFilter) == 0
        newCollection.add user
    newCollection

  orderBy: (event) ->
    @collection.orderBy event.target.value
    @collection.sort()

  render: () ->
    that = @
    filteredCollection = @collection
    _.each filters, (filter) ->
      filteredCollection = filter.call that, filteredCollection
    filteredCollection = @applyFilterByText filteredCollection unless not @textFilter
    @usersListView = new Account.UsersListView
      collection: filteredCollection
      follows: @follows
      followed: @followed
      refUser: @refUser
    @usersListView.render()
