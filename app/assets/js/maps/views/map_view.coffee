window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"

class window.Maps.MapView extends Backbone.View
  width: 'auto'
  height: '50em'
  readonly: false
  map: null
  drawLayer: null
  controls: []
  baseLayers: [

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

  initialize: ->
    @width = @options.width ? @width
    @height = @options.height ? @height
    @readonly = @options.readonly? @readonly

    $(@el).css('width', @width).css('height', @height)

    @listenTo @model, 'reset change', @render

    @map = new OpenLayers.Map(
      @el.id
      theme: null
      projection: "EPSG:900913"
      fractionalZoom: true
    )
    @map.addLayers @baseLayers
    @initControls()
    @map.zoomToMaxExtent()

  initControls: ->
    OpenLayers.Feature.Vector.style['default']['strokeWidth'] = '2'
    # allow testing of specific renderers via "?renderer=Canvas", etc
    renderer = OpenLayers.Util.getParameters(window.location.href).renderer
    renderer = if renderer? then [renderer] else OpenLayers.Layer.Vector.prototype.renderers
    @drawLayer = new OpenLayers.Layer.Vector(
      "Draws"
      renderers: renderer
    )

    toolBarControls = [

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.Point
        'displayClass': 'olControlDrawFeaturePoint'
        featureAdded: (feature, pixel) => @saveFeature feature
      )

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.Path
        'displayClass': 'olControlDrawFeaturePath'
        featureAdded: (feature, pixel) => @saveFeature feature
      )

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.Polygon
        'displayClass': 'olControlDrawFeaturePolygon'
        featureAdded: (feature, pixel) => @saveFeature feature
      )

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.RegularPolygon
        'displayClass': 'olControlDrawFeatureRegularPolygon'
        handlerOptions: { sides: 8 }
        featureAdded: (feature, pixel) => @saveFeature feature
      )

      new OpenLayers.Control.ModifyFeature(
        @drawLayer
        'displayClass': 'olControlModifyFeature'
        createVertices: true
        mode: OpenLayers.Control.ModifyFeature.RESHAPE
        onModificationEnd: (feature) => @updateFeature feature
      )

      new OpenLayers.Control.DragFeature(
        @drawLayer
        'displayClass': 'olControlDragFeature'
        onComplete: (feature, pixel) => @updateFeature feature
      )

      new OpenLayers.Control.Button(
        'displayClass': "olControlSaveAllFeatures"
        trigger: () -> console.log "TODO: Trigger save all!"
      )

    ]
    toolbar = new OpenLayers.Control.Panel(
      displayClass: 'olControlEditingToolbar'
      defaultControl: toolBarControls[0]
    )
    toolbar.addControls toolBarControls
    @controls.push toolbar
    @controls.push new OpenLayers.Control.LayerSwitcher()

    # TODO: Add more controls here ....

    @map.addLayer @drawLayer
    @map.addControls @controls

  render: ->
    features = @model.get 'features'
    @drawFeature f for f in features unless features == null
    @

  drawFeature: (featureId) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    feature = new Maps.Feature id: featureId
    feature.fetch complete: (resp) =>
      geom = geojsonFormat.read(JSON.stringify(feature.get 'geometry'), 'Geometry')
      olGeom = new OpenLayers.Feature.Vector geom
      olGeom.data = feature
      @drawLayer.addFeatures [olGeom]

  saveFeature: (feature) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    ft = new Maps.Feature
      ownerId: @model.get 'ownerId'
      geometry: JSON.parse geojsonFormat.write feature.geometry
    ft.once 'change', (evt) =>
      feature.data = ft
      fts = @model.get('features') or []
      fts.push ft.get 'id'
      @model.save features: fts
    ft.save()

  updateFeature: (feature) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    feature.data.save geometry: JSON.parse geojsonFormat.write feature.geometry
