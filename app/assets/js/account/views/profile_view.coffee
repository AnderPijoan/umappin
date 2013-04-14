window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.ProfileView extends Backbone.View
  readonly: true

  events:
    "click button":  "update"

  el: "form#profile-form"

  template: _.template $('#userprofile-template').html()

  initialize: ->
    @readonly = @options.readonly
    @listenTo @model, 'reset change', @render

  render: ->
    data = @model.attributes
    data.readonly = @readonly
    $(@el).html @template data
    $(@el).find('input[type=text], textarea').attr('readonly', @readonly)
    @

  update: ->
    # TODO
    if !@readonly
      console.log "TODO"
