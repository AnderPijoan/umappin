window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.ProfileView extends Backbone.View

  events:
    "click button":  "update"
    "change input":  "update"

  el: "form#profile-form"

  template: _.template $('#userprofile-template').html()

  initialize: ->
    console.log @
    @listenTo @model, 'change', @render

  render: ->
    $(@el).append @template @model.attributes
    @

  update: ->
    console.log "Ahh"
