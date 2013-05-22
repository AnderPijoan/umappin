window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.SearchMapView extends Maps.MapView
  featuresLayer: null
  searchBounds: null
  searchScope: 0.05
  featuresPopupTemplate: _.template $('#features-popup-template').html()

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
        r = @searchScope
        p = new OpenLayers.Geometry.Point e.point.x, e.point.y
        p.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
        @searchBounds = [p.y - r, p.x - r, p.y + r, p.x + r]
        @map.zoomToExtent e.point.getBounds()
    )
    geolocationControl.events.register(
      "locationfailed"
      @
      () -> OpenLayers.Console.log 'Location detection failed'
    )
    @controls.push geolocationControl

    that = @
    # Search control
    OpenLayers.Control.prototype.keepEvents = (div) ->
      @keepEventsDiv = new OpenLayers.Events(@, div, null, true)

      #IN THIS CASE triggerButton IS NOT NECESSARY
      triggerButton = (evt) =>
        element = OpenLayers.Event.element(evt)
        buttonclick = @map.events and @map.events.extensions and @map.events.extensions.buttonclick
        if element && buttonclick
          element = buttonclick.getPressedButton(element)
          if element && OpenLayers.Element.hasClass(element, "olButton") then buttonclick.buttonClick(evt)

      triggerSearch = (evt) =>
        element = OpenLayers.Event.element(evt)
        if  evt.keyCode == 13 then that.getFeatures $(element).val()

      listeners =
        "mousedown": (evt) ->
          @mousedown = true
          triggerButton(evt)
          OpenLayers.Event.stop(evt, true)

        "mousemove": (evt) ->
          OpenLayers.Event.stop(evt, true) unless !@mousedown

        "mouseup": (evt) ->
          if @mousedown
            @mousedown = false
            triggerButton(evt)
            OpenLayers.Event.stop(evt, true)

        "click": (evt) ->
          triggerButton(evt)
          OpenLayers.Event.stop(evt, true)

        "mouseout": (evt) -> @mousedown = false

        "dblclick": (evt) ->
          triggerButton(evt)
          OpenLayers.Event.stop(evt, true)

        "touchstart": (evt) ->
          OpenLayers.Event.stop(evt, true)

        "keydown": (evt) ->  triggerSearch(evt)

        scope: @

      @keepEventsDiv.on(listeners);

    customControl = new OpenLayers.Control
    OpenLayers.Util.extend customControl,
      displayClass: 'customControl'

      initialize : () ->
        OpenLayers.Control.prototype.initialize.apply(@, arguments)

      draw: () ->
        div = OpenLayers.Control.prototype.draw.apply(@, arguments)
        html = "<div><input type='text' id='textToolbar' value='highway'/></div>"
        div.innerHTML = html
        @keepEvents(div);
        div

      allowSelection: true

    @controls.push customControl

    # Call superclass controls initialization
    super
    # Activate controls after loading stuff
    geolocationControl.activate()
    customControl.activate()


  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @controls = []
    @baseLayers = []
    super
    # Markers layer
    @featuresLayer = new OpenLayers.Layer.Markers "Features" # change this to features layer
    @map.addLayer @featuresLayer


  # -------------------------------- OSM notes loader ---------------------------- #
  getFeatures: (text) ->
    bbox = "#{@searchBounds[0]}, #{@searchBounds[1]}, #{@searchBounds[2]}, #{@searchBounds[3]}"
    $.get "http://overpass.osm.rambler.ru/cgi/interpreter?data=[out:json];node['#{text}'](#{bbox});out;", (data) =>
      data = JSON.parse data unless $.isPlainObject(data)
      console.log data
      if data? and data.elements? and data.elements.length > 0
        bounds = new OpenLayers.Bounds
        @addItem item, bounds for item in data.elements
        @map.zoomToExtent bounds

  addItem: (item, bounds) ->
    lonlat = new OpenLayers.LonLat(item.lon, item.lat)
    lonlat.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
    bounds.extend lonlat
    feat = new OpenLayers.Feature @markersLayer, lonlat
    feat.popupClass = OpenLayers.Popup.FramedCloud
    feat.data.popupContentHTML = @featuresPopupTemplate item
    feat.data.overflow = 'auto'
    marker = feat.createMarker()
    that = @
    marker.events.register "mousedown", feat, (evt) ->
      if @popup
        @popup.toggle()
      else
        @popup = @createPopup(true)
        that.map.addPopup(@popup)
        @popup.show()
        $('.doSomethingButton').bind('click', (e) -> alert('TODO!!'))
      OpenLayers.Event.stop(evt)
    @featuresLayer.addMarker(marker)
