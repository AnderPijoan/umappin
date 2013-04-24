class window.Photo.Content extends Backbone.Model

  urlRoot: null
  defaults:
    id: null
    xSize: null
    ySize: null
    mimeType: null
    fileBytes: null

  initialize: ->
    @urlRoot = "/photos/#{@id}/content"