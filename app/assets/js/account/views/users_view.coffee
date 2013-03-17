window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersView extends Backbone.View

  el: 'ul#userlist'

  template: _.template $('#userlist-template').html()

  events:
    "click li": "toc"

  initialize: ->
    @listenTo @collection, 'add', @render

  render: ->
    $(@el).html @template @collection.toJSON()
    @

  toc: ->
    console.log 'toc'