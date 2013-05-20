window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.MapView extends Backbone.View
  @OSM_PROJECTION: new OpenLayers.Projection("EPSG:4326")
  @OL_PROJECTION: new OpenLayers.Projection("EPSG:900913")
  width: '60em'
  height: '30em'
  readonly: false
  map: null
  controls: []
  baseLayers: []

  # ----------------------------- Base Layers ------------------------------- #
  initBaseLayers: () ->
    baseLayers = [

      new OpenLayers.Layer.OSM(
        "OpenStreetMap"
        [ "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png"
          "http://b.tile.openstreetmap.org/${z}/${x}/${y}.png"
          "http://c.tile.openstreetmap.org/${z}/${x}/${y}.png" ]
        isBaseLayer: true
        transitionEffect: 'resize'
      )

      new OpenLayers.Layer.Bing(
        name: "Bing Road"
        key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
        type: "Road"
        metadataParams: { mapVersion: "v1" }
        isBaseLayer: true
      )

      new OpenLayers.Layer.Bing(
        name: "Bing Aerial"
        key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
        type: "Aerial"
        isBaseLayer: true
      )

      new OpenLayers.Layer.Bing(
        name: "Bing Aerial With Labels"
        key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
        type: "AerialWithLabels"
        isBaseLayer: true
      )

    ]
    @baseLayers.push lyr for lyr in baseLayers
    @map.addLayers @baseLayers

  # ---------------------------- Controls ------------------------------ #
  initControls: ->
    # Custom Keyboard handler
    keyboardControl = new OpenLayers.Control
    keyboardControl.handler = new OpenLayers.Handler.Keyboard(
      keyboardControl
      'keyup': (e) =>
        console.log "key #{e.keyCode}"
        # TODO: handle different key events
    )
    keyboardControl.activate()

    @controls.push keyboardControl
    @controls.push new OpenLayers.Control.LayerSwitcher()
    mapPosition = new OpenLayers.Control.MousePosition()
    mapPosition.displayProjection = new OpenLayers.Projection("EPSG:4326")
    @controls.push mapPosition

    # TODO: Add more controls here ....

    @map.addControls @controls


  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @width = @options.width ? @width
    @height = @options.height ? @height
    @readonly = @options.readonly ? @readonly

    $(@el).css('width', @width).css('height', @height)

    @listenTo @model, 'reset ', @render
    # 0. Create the Map
    @map = new OpenLayers.Map(
      @el.id
      theme: null
      projection: "EPSG:900913"
      fractionalZoom: true
    )
    # 1. Add base layers
    @initBaseLayers()
    # 2. Add controls
    @initControls()
    # Initial map zoom
    @map.zoomToMaxExtent()

  # ---------------------------- Renderization ------------------------------ #
  render: -> @
