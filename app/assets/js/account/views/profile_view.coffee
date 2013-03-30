window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.ProfileView extends Backbone.View

  events:
    "click button":  "update"

  el: "form#profile-form"

  template: _.template $('#userprofile-template').html()

  initialize: ->
    @listenTo @model, 'change', @render

  render: ->
    $(@el).html @template @model.attributes
    @

  update: ->
    # TODO
    console.log "TODO"
