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

  render: ->
    $(@el).html @template { data: @model.attributes, readonly: @readonly }
    $(@el).find('input[type=text], textarea').attr('readonly', @readonly)
    picture = new Picture id: @model.get "profilePicture"
    if @model.get "profilePicture"
      @pictureView = new PictureView
        model: picture
        readonly: @readonly
        showInfo: true
        picWidth: '16em'
      picture.fetch()
      $(@el).find('#profilePictureHolder').append @pictureView.render().el
    else
      picture.save
        owner_id: @model.get 'id'
        { success: () => @model.save profilePicture: picture.get 'id' }
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
          phone: @$el.find('#profile-phone').val()
          address: @$el.find('#profile-address').val()
          { success: () => location.href="./" }
      else
        $('#profileError').css('display','block').empty().html(
          "<div class='alert alert-error'>" + data.responseText + "</div>")

