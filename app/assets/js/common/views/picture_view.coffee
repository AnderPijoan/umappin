_.templateSettings.variable = 'rc'

class window.PictureView extends Backbone.View
  readonly: true
  showInfo: false
  templatePath: "/assets/templates/picture.html"
  template: null

  events:
    #"click img":  "browsePicture"
    #"click #dropPicture":  "browsePicture"
    "dragover #dropPicture":  "dragOverPicture"
    "drop #dropPicture":  "dropPicture"

  tagName: 'div'
  className: 'pictureView'

  initialize: ->
    @readonly = @options.readonly
    @showInfo = @options.showInfo
    @listenTo @model, 'reset change', @render

  render: ->
    data = @model.attributes
    if @template
      $(@el).html @template { data: data, readonly: @readonly, showInfo: @showInfo }
    else
      $.get @templatePath, (resp) =>
        @template = _.template resp
        @render()
    @

  dragOverPicture: (e) ->
    Photo.dragOverHandler e

  dropPicture: (e) ->
    Photo.dropHandler e, 'pictureInfo'