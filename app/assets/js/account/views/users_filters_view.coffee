window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersFiltersView extends Backbone.View
  follows = null
  followed = null
  refUser = null
  usersListView = null
  filteredCollection = null
  el: 'div#users-filters'
  events: 
    'change #orderFilter':           'orderBy'

  initialize: ->
    @listenTo @collection, 'reset ', @render
    @follows = @options.follows
    @followed = @options.followed
    @refUser = @options.refUser
    @filteredCollection = @collection

  orderBy: (event) ->
    @collection.orderBy event.target.value
    @collection.sort()

  render: () ->
    @usersListView = new Account.UsersListView
      collection: @collection
      follows: @follows
      followed: @followed
      refUser: @refUser
    @usersListView.render()
