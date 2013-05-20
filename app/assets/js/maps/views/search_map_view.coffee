window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.SearchMapView extends Maps.MapView
  featuresLayer: null
  #featuresPopupTemplate: _.template $('#features__-popup-template').html()

  # ---------------------------- Controls ------------------------------ #
  initControls: ->
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
        origin = new OpenLayers.Geometry.Point e.point.x, e.point.y
        bbox = origin.getBounds()
        bbox.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
        @getOSMNotes bbox, e.point
    )
    geolocationControl.events.register(
      "locationfailed"
      @
      () -> OpenLayers.Console.log 'Location detection failed'
    )
    @controls.push geolocationControl

    # Search control
    CustomControl = OpenLayers.Class OpenLayers.Control
      displayClass: 'customControl'
      initialize : () ->
        OpenLayers.Control.prototype.initialize.apply(this, arguments)
      draw: () ->
        div = OpenLayers.Control.prototype.draw.apply(this, arguments)
        html = "<div><input type='text' id='textToolbar' value='This'/></div>"
        div.innerHTML = html
        div
      allowSelection: true
    customControl = new CustomControl
    @controls.push new customControl

    # Call superclass controls initialization
    super
    # Activate controls after loading stuff
    #geolocationControl.activate()
    customControl.activate()


  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @controls = []
    @baseLayers = []
    super
    # Markers layer
    @featuresLayer = new OpenLayers.Layer.Vector "Features"
    @map.addLayer @featuresLayer


  # -------------------------------- OSM notes loader ---------------------------- #
  getFeatures: () ->
    text = $('#textToolbar').val();
    $.get "http://overpass.osm.rambler.ru/cgi/interpreter?data=[out:json];#{text}", (data) ->
      data = JSON.parse data unless $.isPlainObject(data)
      console.log data
