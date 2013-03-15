class window.UserView extends Backbone.View
  el: 'ul#userlist'
  template: _.template $('#userlist-template').html()
  constructor: (@collection) ->
  initialize: ->
    console.log @collection
    @listenTo @collection, 'change', @add()
  render: ->
    console.log $el
    @el.html @template @collection.toJSON()
    @
  add: ->
    console.log @collection.toJSON()
    @render