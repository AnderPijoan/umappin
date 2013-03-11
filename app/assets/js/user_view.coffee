class UserView extends Backbone.View
  el: 'ul#user'
  template: _.template $('#userlist-template').html()
  collection: null
  initialize: ->
    @listenTo Users, 'add', @add
  render: ->
    @el.html @template(@model.attributes)
    @
  add: ->
    @render