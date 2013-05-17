window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.FeaturesMapView extends Maps.MapView
  drawLayer: null

  # ---------------------------- Controls Layer ------------------------------ #
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
        mode: OpenLayers.Control.ModifyFeature.RESHAPE | OpenLayers.Control.ModifyFeature.DRAG
        onModificationStart: (feature) => console.log feature
        onModificationEnd: (feature) =>
          @updateFeature feature
      )

      new OpenLayers.Control.ModifyFeature(
        @drawLayer
        'displayClass': 'olControlDragFeature'
        mode: OpenLayers.Control.ModifyFeature.RESIZE | OpenLayers.Control.ModifyFeature.ROTATE | OpenLayers.Control.ModifyFeature.DRAG
        onModificationStart: (feature) => console.log feature
        onModificationEnd: (feature) => @updateFeature feature
      )
    ]
    toolbar = new OpenLayers.Control.Panel(
      displayClass: 'olControlEditingToolbar'
      defaultControl: toolBarControls[5]
    )
    toolbar.addControls toolBarControls

    addMarkerControl = new OpenLayers.Control
    addMarkerControl.handler = new OpenLayers.Handler.Click(
      addMarkerControl
      'click': (e) => @createMarker @map.getLonLatFromViewPortPx e.xy
    )

    # Custom Keyboard handler
    keyboardControl = new OpenLayers.Control
    keyboardControl.handler = new OpenLayers.Handler.Keyboard(
      keyboardControl
      'keyup': (e) =>
        console.log "key #{e.keyCode}"
        if e.keyCode == 46 then (@deleteFeature feature) for feature in @selectedFeatures
    )

    # Custom GetFeature handler
    getFeatureControl = new OpenLayers.Control.GetFeature
      protocol: new OpenLayers.Protocol.Script()
      multipleKey: 'shiftKey'
      toggleKey: 'altKey'
      multiple: true
      box: true
    getFeatureControl.handler = new OpenLayers.Handler.Click(
      getFeatureControl
      'click': (e) => console.log e
    )
    getFeatureControl.events.register 'featureselected', @, (e) -> console.log e

    @controls.push toolbar
    @controls.push getFeatureControl

    # TODO: Add more controls here ....

    @map.addLayer @drawLayer
    super
    #keyboardControl.activate()
    #getFeatureControl.activate()

  render: ->
    features = @model.get 'features'
    @drawFeature f for f in features unless features == null
    super

  drawFeature: (featureId) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    feature = new Maps.Feature id: featureId
    feature.fetch complete: (resp) =>
      geom = geojsonFormat.read(JSON.stringify(feature.get 'geometry'), 'Geometry')
      olGeom = new OpenLayers.Feature.Vector geom
      olGeom.mapFeature = feature
      @drawLayer.addFeatures [olGeom]

  saveFeature: (feature) ->
    console.log feature
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    ft = new Maps.Feature
      ownerId: @model.get 'ownerId'
      geometry: JSON.parse geojsonFormat.write feature.geometry
    ft.once 'change', (evt) =>
      feature.mapFeature = ft
      fts = @model.get('features') or []
      fts.push ft.get 'id'
      @model.save features: fts
    ft.save()

  updateFeature: (feature) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    feature.mapFeature.save geometry: JSON.parse geojsonFormat.write feature.geometry

  deleteFeature: (feature) ->
    feature.mapFeature.destroy success: ()=>
      features = @model.get 'features'
      @model.save
        features: features.splice(features.indexOf(feature.mapFeature.get 'id'), 1)
        { success: () => @drawLayer.removeFeatures [feature] }
