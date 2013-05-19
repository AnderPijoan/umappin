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
        "OSM"
        [ "http://a.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png"
          "http://b.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png"
          "http://c.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png" ]
        layers: "basic"
        isBaseLayer: true
        resolutions: [156543.03390625, 78271.516953125, 39135.7584765625,
                      19567.87923828125, 9783.939619140625, 4891.9698095703125,
                      2445.9849047851562, 1222.9924523925781, 611.4962261962891,
                      305.74811309814453, 152.87405654907226, 76.43702827453613,
                      38.218514137268066, 19.109257068634033, 9.554628534317017,
                      4.777314267158508, 2.388657133579254, 1.194328566789627,
                      0.5971642833948135, 0.25, 0.1, 0.05]
        serverResolutions: [156543.03390625, 78271.516953125, 39135.7584765625,
                            19567.87923828125, 9783.939619140625,
                            4891.9698095703125, 2445.9849047851562,
                            1222.9924523925781, 611.4962261962891,
                            305.74811309814453, 152.87405654907226,
                            76.43702827453613, 38.218514137268066,
                            19.109257068634033, 9.554628534317017,
                            4.777314267158508, 2.388657133579254,
                            1.194328566789627, 0.5971642833948135]
        transitionEffect: 'resize'
      )

      new OpenLayers.Layer.Bing(
        name: "Bing-Road"
        key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
        type: "Road"
        metadataParams: { mapVersion: "v1" }
        isBaseLayer: true
      )

      new OpenLayers.Layer.Bing(
        name: "Bing-Aerial"
        key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
        type: "Aerial"
        isBaseLayer: true
      )

      new OpenLayers.Layer.Bing(
        name: "Bing-AerialWithLabels"
        key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
        type: "AerialWithLabels"
        isBaseLayer: true
      )

      new OpenLayers.Layer.WMS(
        "WMS"
        "http://vmap0.tiles.osgeo.org/wms/vmap0"
        layers: "basic"
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
    @controls.push new OpenLayers.Control.MousePosition()

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
