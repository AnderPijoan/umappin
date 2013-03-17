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
    if $(elm).text() == "Follow"
      $(elm).text "Unfollow"
      @followUser fusr_id
    else
      $(elm).text "Follow"
      @unfollowUser fusr_id

    followUser: (id) ->


    unfollowUser: (id) ->
