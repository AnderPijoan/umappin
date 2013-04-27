_.templateSettings.variable = 'rc'

class window.PictureView extends Backbone.View
  readonly: true
  showInfo: false
  templatePath: "/assets/templates/picture.html"
  template: null
  picWidth: '12em'
  picHeight: '9em'

  events:
    "dragover #dropPicture":  "dragOverPicture"
    "drop #dropPicture":  "dropPicture"

  tagName: 'div'
  className: 'pictureView'

  initialize: ->
    @readonly = @options.readonly
    @showInfo = @options.showInfo
    @picWidth = @options.picWidth
    @picHeight = @options.picHeight
    @listenTo @model, 'reset change', @render

  render: ->
    if not @model.get('id') and not @readonly
      @model.save
        owner_id: Account.profile.get 'id'
        { success: () => @render() }
    else if @template
      $(@el).html @template { data: @model.attributes, readonly: @readonly, showInfo: @showInfo }
      $(@el).find('img').css('width', @picWidth).css('height', @picHeight)
      if not @readonly
        filename = null
        upclick
          element: document.getElementById 'uploader'
          action: "/photos/#{@model.get 'id'}/content"
          onstart: (fname) => filename = fname
          oncomplete: () =>
            @model.save title: filename, date_created: new Date().getTime()
    else
      $.get @templatePath, (resp) =>
        @template = _.template resp
        @render()
    @

  dragOverPicture: (e) ->
    e.stopPropagation()
    e.preventDefault()
    e.originalEvent.dataTransfer.dropEffect = 'copy'

  dropPicture: (e) =>
    e.stopPropagation()
    e.preventDefault()
    file = e.originalEvent.dataTransfer.files[0]
    if file.type.match 'image.*'
      formData = new FormData()
      formData.append file.name, file
      xhr = new XMLHttpRequest()
      xhr.open 'POST', "/photos/#{@model.get 'id'}/content", true
      xhr.onload = (e) =>
        @model.save title: file.name, date_created: new Date().getTime()
      xhr.send formData
