window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UserRowView extends Backbone.View

  refUser: null

  followsView: null

  tagName: 'li'

  className: 'user-entry'

  template: _.template $('#user-row-template').html()

  constructor: (@model, @refUser) ->

  initialize: ->
    @listenTo @model, 'change', @render
    follows = Account.follows.getByUserId @refUser.get 'id'
    @followsView = new Account.UserFollowsView  follows, @model.get 'id'

  render: ->
    @$el.html @template @model.attributes
    @$el.find('div.row').html @followsView.render().el
    @

