window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.RoutesMapView extends Maps.MapView
  drawLayer: null

  # ---------------------------- Controls ------------------------------ #
  initControls: ->
    OpenLayers.Feature.Vector.style['default']['strokeWidth'] = '2'
    # allow testing of specific renderers via "?renderer=Canvas", etc
    renderer = OpenLayers.Util.getParameters(window.location.href).renderer
    renderer = if renderer? then [renderer] else OpenLayers.Layer.Vector.prototype.renderers
    @drawLayer = new OpenLayers.Layer.Vector(
      "Routes"
      renderers: renderer
    )

    toolBarControls = [

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.Path
        'displayClass': 'olControlDrawFeaturePath'
        featureAdded: (feature, pixel) => @saveFeature feature
      )

      new OpenLayers.Control.ModifyFeature(
        @drawLayer
        'displayClass': 'olControlModifyFeature'
        mode: OpenLayers.Control.ModifyFeature.RESHAPE | OpenLayers.Control.ModifyFeature.DRAG
        onModificationStart: (feature) => console.log feature
        onModificationEnd: (feature) => @updateFeature feature
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
    @controls.push toolbar


    # Geolocation layer & control
    geoLocationLayer = new OpenLayers.Layer.Vector("Your location")
    @map.addLayer(geoLocationLayer);
    geolocationControl = new OpenLayers.Control.Geolocate
      bind: true
      watch: true
      geolocationOptions:
        enableHighAccuracy: true
        maximumAge: 0
        timeout: 7000
    geolocationControl.follow = true
    geolocationControl.events.register(
      "locationupdated"
      @
      (e) ->
        geoLocationLayer.removeAllFeatures()
        geoPlace = new OpenLayers.Feature.Vector(
          e.point
        {}
        {
          graphicName: 'circle'
          strokeColor: '#0000FF'
          strokeWidth: 1
          fillOpacity: 0.5
          fillColor: '#0000BB'
          pointRadius: 20
        }
        )
        geoLocationLayer.addFeatures [geoPlace]
        @map.zoomToExtent e.point.getBounds()

        geopoint = new OpenLayers.Geometry.Point e.point.x, e.point.y
        geopoint.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
        geojsonFormat = new OpenLayers.Format.GeoJSON()
        geopoint = JSON.parse geojsonFormat.write geopoint

        amount = 5 # TODO: we'll pick it up from a control
        difficulty = 1 # TODO: we'll pick it up from a control

        $.ajax
          url: "/routes/near/#{amount}/difficulty/#{difficulty}"
          contentType: 'application/json'
          data: geopoint
          success: (data) =>
            console.log data
    )
    geolocationControl.events.register(
      "locationfailed"
      @
      () -> OpenLayers.Console.log 'Location detection failed'
    )
    @controls.push geolocationControl

    # TODO: Add more controls here ....

    @map.addLayer @drawLayer
    super
    geolocationControl.activate()

  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @controls = []
    @baseLayers = []
    super


  # ---------------------------- Renderization ------------------------------ #
  render: ->
    features = @model.get 'features'
    @drawFeature f for f in features unless features == null
    super


  # ---------------------------- REST/Feature handlers ------------------------------ #
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
