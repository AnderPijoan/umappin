class window.UserView extends Backbone.View
  el: 'ul#userlist'
  template: _.template $('#userlist-template').html()
  constructor: (@collection) ->
  initialize: ->
    @listenTo @collection, 'add', @add()
  render: ->
    $(@el).html @template @collection.toJSON()
    @
  add: ->
    @render