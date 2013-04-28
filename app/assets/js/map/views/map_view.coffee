class window.MapView extends Backbone.View
  tagName: 'div'
  id: 'map'
  width: 'auto'
  height: '50em'
  readonly: false
  map: null

  initialize: ->
    @width = @options.width ? @width
    @height = @options.height ? @height
    @readonly = @options.readonly? @readonly
    @listenTo @model, 'reset change', @render
    if not @model.get('id') and not @readonly
      @model.save ownerId: Account.session.get 'id'
    setTimeout (() => $(@el).css('width', @width).css('height', @height)), 5000
    @map = new OpenLayers.Map div: @el, theme: '/assets/css/openlayers/style.min.css'#TODO handle here...
    OpenLayers.ImgPath = "/assets/img/openlayers/"
    ol_wms = new OpenLayers.Layer.WMS(
      "OpenLayers WMS"
      "http://vmap0.tiles.osgeo.org/wms/vmap0"
      { layers: "basic" }
    )
    @map.addLayers [ol_wms]
    @map.zoomToMaxExtent()
    @render()
  render: ->
    @drawFeature f for f in @model.get 'features'
    @

  drawFeature: (feature) ->
    console.log feature