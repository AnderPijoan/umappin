window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.SearchMapView extends Maps.MapView
  featuresLayer: null
  searchBounds: null
  searchScope: 0.05
  markers: []
  featuresPopupTemplate: _.template $('#features-popup-template').html()

  # ---------------------------- Controls ------------------------------ #
  initControls: ->
    # Call superclass controls initialization
    super


  # ---------------------------- Geolocation Handler ------------------------------ #
  handleGeoLocated: (e) ->
    super
    r = @searchScope
    p = new OpenLayers.Geometry.Point e.point.x, e.point.y
    p.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    @searchBounds = [p.y - r, p.x - r, p.y + r, p.x + r]
    @map.zoomToExtent e.point.getBounds()


  # ---------------------------- Search handler ------------------------------ #
  # Overriden
  performSearch: (text) -> @getFeatures text


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
        @featuresLayer.removeMarker(marker) for marker in @markers
        @markers = []
        bounds = new OpenLayers.Bounds
        @addItem item, bounds for item in data.elements
        @map.zoomTo Math.round @map.getZoomForExtent bounds


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
    @markers.push marker
    @featuresLayer.addMarker(marker)

