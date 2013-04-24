window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.ProfileView extends Backbone.View
  readonly: true
  pictureView: null

  events:
    "click button":  "update"

  el: "form#profile-form"

  template: _.template $('#userprofile-template').html()

  initialize: ->
    @readonly = @options.readonly
    @listenTo @model, 'reset change', @render
    picture = new Photo id: @model.profilePicture
    @pictureView = new PictureView { model: picture, readonly: @readonly, showInfo: true }
    if @model.profilePicture then picture.fetch()


  render: ->
    data = @model.attributes
    #data.readonly = @readonly
    $(@el).html @template { data: data, readonly: @readonly }
    $(@el).find('input[type=text], textarea').attr('readonly', @readonly)
    $(@el).find('#profilePictureHolder').append @pictureView.render()
    @

  validate: ->
    name = @$el.find('#profile-name').val()
    email = @$el.find('#profile-email').val()
    name != '' and email.match /^\w.+@\w.+\.\w.+$/g

  update: ->
    if !@readonly
      if @validate
        @model.save
          name: @$el.find('#profile-name').val()
          email: @$el.find('#profile-email').val()
          firstName: @$el.find('#profile-firstname').val()
          lastName: @$el.find('#profile-lastname').val()
      else
        $('#actionResult').css('display','block').empty().html(
          "<div class='alert alert-error'>" + data.responseText + "</div>")

