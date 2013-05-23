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
    ###
    @drawLayer.events.register "featureselected", @, (feat) ->
      if feat.popup
        feat.popup.toggle()
      else
        feat.popup = feat.createPopup(true)
        @map.addPopup(feat.popup)
        feat.popup.show()
        $('.doSomethingButton').bind('click', (e) -> alert('TODO!!'))
    ###
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

      new OpenLayers.Control.SelectFeature(
        @drawLayer
        'displayClass': 'olControlDragFeature'
        box: true
        onSelect: (feat) =>
          feat.popup = new OpenLayers.Popup.FramedCloud(
            "chicken"
            feat.geometry.getBounds().getCenterLonLat()
            null
            "<div style='font-size:.8em'>Route: #{feat.id} <br>Area: #{feat.geometry.getArea()}</div>"
            null
            true
            (evt) =>
              @map.removePopup feat.popup
              #feat.popup.destroy()
              #feat.popup = null
          )
          @map.addPopup(feat.popup)
          $('.doSomethingButton').bind('click', (e) -> alert('TODO!!'))
        onUnselect: (feat) =>
          @map.removePopup feat.popup
          feat.popup.destroy()
          feat.popup = null
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
        geopoint = "{\"geometry\": #{geojsonFormat.write geopoint} }"

        amount = 5 # TODO: we'll pick it up from a control
        difficulty = -1 # TODO: we'll pick it up from a control

        $.ajax
          url: "/routes/near/#{amount}/difficulty/#{difficulty}"
          type: 'POST'
          contentType: 'application/json'
          data: geopoint
          success: (data) => @drawRoute r for r in data unless data == null
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
    #@drawFeature f for f in features unless features == null
    super


  drawRoute: (data) ->
    route = new Maps.Route data
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    geom = geojsonFormat.read(JSON.stringify(route.get 'geometry'), 'Geometry')
    geom.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
    olGeom = new OpenLayers.Feature.Vector geom
    olGeom.mapFeature = route
    ###
    olGeom.popupClass = OpenLayers.Popup.FramedCloud
    olGeom.data.popupContentHTML = "<p>wtf??</p>"
    olGeom.data.overflow = 'auto'
    ###

    @drawLayer.addFeatures [olGeom]

  # ---------------------------- REST/Route Feature handlers ------------------------------ #
  # This one gets a feature given its id via typical REST -- maybe we can get rid of it
  drawFeature: (featureId) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    feature = new Maps.Route id: featureId
    feature.fetch complete: (resp) =>
      geom = geojsonFormat.read(JSON.stringify(feature.get 'geometry'), 'Geometry')
      geom.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
      olGeom = new OpenLayers.Feature.Vector geom
      olGeom.mapFeature = feature
      @drawLayer.addFeatures [olGeom]

  saveFeature: (feature) ->
    console.log feature
    geom = feature.geometry.clone().transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    ft = new Maps.Route
      name: 'dummyRoute'
      difficulty: 1 # TODO: pick this properties from elsewhere
      geometry: JSON.parse geojsonFormat.write geom
    ft.once 'change', (evt) =>
      feature.mapFeature = ft
      fts = @model.get('features') or []
      fts.push ft.get 'id'
      @model.save features: fts
    ft.save()

  updateFeature: (feature) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    geom = feature.geometry.clone().transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    feature.mapFeature.save geometry: JSON.parse geojsonFormat.write geom

  deleteFeature: (feature) ->
    feature.mapFeature.destroy success: ()=>
      features = @model.get 'features'
      @model.save
        features: features.splice(features.indexOf(feature.mapFeature.get 'id'), 1)
        { success: () => @drawLayer.removeFeatures [feature] }
