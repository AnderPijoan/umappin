_.templateSettings.variable = 'rc'

class window.PictureView extends Backbone.View
  readonly: true
  showInfo: false
  onSave: null
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
    @onSave = @options.onSave
    @listenTo @model, 'reset save update change', @render

  render: ->
    console.log @model
    data = @model.attributes
    if @template
      $(@el).html @template { data: data, readonly: @readonly, showInfo: @showInfo }
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
    files = e.originalEvent.dataTransfer.files
    output = ''

    for f in files
      do (f) =>
        if not f.type.match 'image.*' then return
        evt = e
        reader = new FileReader()
        reader2 = new FileReader()
        reader.onload = (e) => reader2.readAsDataURL f
        reader.readAsDataURL f

        reader2.onload = (e) =>
          result = e.target.result
          jsonContent =
            title: f.name
            date_created: new Date().getTime()
            owner_id: Account.profile.id
            content: result
          $.ajax(
            type: "POST"
            url: "/photos"
            dataType: "json"
            contentType: "application/json; charset=utf-8"
            data: JSON.stringify jsonContent
            success: (resp) =>
              @model.set("id", resp.id);
              @model.fetch success: () =>
                @model.trigger 'change'
                @onSave()
          )

        reader.read
