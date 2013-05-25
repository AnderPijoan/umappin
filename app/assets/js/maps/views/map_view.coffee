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
  geoLocationControl: null
  searchBarTemplate: _.template $('#search-bar-template').html()

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

    # Custom Keyboard control & handler
    keyboardControl = new OpenLayers.Control
    keyboardControl.handler = new OpenLayers.Handler.Keyboard(
      keyboardControl
      'keyup': (e) =>
        console.log "key #{e.keyCode}"
        # TODO: handle different key events
    )
    @controls.push keyboardControl


    # Geolocation layer & control
    geoLocationLayer = new OpenLayers.Layer.Vector("Your location")
    @map.addLayer(geoLocationLayer);
    @geoLocationControl = new OpenLayers.Control.Geolocate
      bind: true
      watch: true
      geolocationOptions:
        enableHighAccuracy: true
        maximumAge: 0
        timeout: 7000
    @geoLocationControl.follow = true
    @geoLocationControl.events.register(
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
        @handleGeoLocated e
    )
    @geoLocationControl.events.register(
      "locationfailed"
      @
      () -> OpenLayers.Console.log 'Location detection failed'
    )
    @controls.push @geoLocationControl


    # Search control
    that = @
    OpenLayers.Control.prototype.keepEvents = (div) ->
      @keepEventsDiv = new OpenLayers.Events(@, div, null, true)

      triggerSearch = (evt) =>
        element = OpenLayers.Event.element(evt)
        if  evt.keyCode == 13 then that.performSearch $(element).val()

      @keepEventsDiv.on
        "mousedown": (evt) ->
          @mousedown = true
          OpenLayers.Event.stop(evt, true)
        "mousemove": (evt) ->
          OpenLayers.Event.stop(evt, true) unless !@mousedown
        "mouseup": (evt) ->
          if @mousedown
            @mousedown = false
            OpenLayers.Event.stop(evt, true)
        "click": (evt) -> OpenLayers.Event.stop(evt, true)
        "mouseout": (evt) -> @mousedown = false
        "dblclick": (evt) -> OpenLayers.Event.stop(evt, true)
        "touchstart": (evt) -> OpenLayers.Event.stop(evt, true)
        "keydown": (evt) -> triggerSearch(evt)
        scope: @

    searchControl = new OpenLayers.Control
    OpenLayers.Util.extend searchControl,
      displayClass: 'searchControl'
      initialize : () ->
        OpenLayers.Control.prototype.initialize.apply(@, arguments)
      draw: () ->
        div = OpenLayers.Control.prototype.draw.apply(@, arguments)
        div.innerHTML = that.searchBarTemplate {}
        @keepEvents(div);
        $(div).find('img.clickableImage').click () =>
          that.performSearch $('#searchInput').val()
        $(div).find('button.geoLocateButton').click () =>
          that.geoLocationControl.deactivate()
          that.geoLocationControl.activate()
        div
      allowSelection: true
    @controls.push searchControl


    # Simple map position display control
    mapPosition = new OpenLayers.Control.MousePosition()
    mapPosition.displayProjection = new OpenLayers.Projection("EPSG:4326")
    @controls.push mapPosition

    # Layer switcher control
    @controls.push new OpenLayers.Control.LayerSwitcher()

    # TODO: Add more controls here ....

    @map.addControls @controls

    # Controls activation
    keyboardControl.activate()
    @geoLocationControl.activate()
    searchControl.activate()

  # ---------------------------- Geolocation Handler ------------------------------ #
  handleGeoLocated: (e) -> console.log e

  # ---------------------------- Search handler ------------------------------ #
  performSearch: (text) ->
    $.get "http://nominatim.openstreetmap.org/search?q=#{text}&format=json&limit=10", (data) =>
      if data? and data.length > 0
        $('div.searchControl ul.searchList').css('display', 'block').html('')
        for item in data
          do (item) =>
            $('div.searchControl ul.searchList').append "<li>#{item.display_name.substring(0, 20)}</li>"
            $('div.searchControl ul.searchList').find('li').last().click () =>
              $('div.searchControl ul.searchList').css('display', 'none').html('')
              @selectLocation item

  selectLocation: (location) -> console.log location

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

