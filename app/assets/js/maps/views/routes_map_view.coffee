window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.RoutesMapView extends Maps.MapView
  drawLayer: null
  routesPopupTemplate: _.template $('#routes-popup-template').html()
  searchBarTemplate: _.template $('#search-bar-template').html()
  routeTagTemplate: _.template $('#route-tag-template').html()

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

      new OpenLayers.Control.DragPan(
        'displayClass': 'olControlDragPan'
      )

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.Path
        'displayClass': 'olControlDrawFeaturePath'
        featureAdded: (route, pixel) => @saveRoute route
      )

      new OpenLayers.Control.ModifyFeature(
        @drawLayer
        'displayClass': 'olControlModifyFeature'
        mode: OpenLayers.Control.ModifyFeature.RESHAPE | OpenLayers.Control.ModifyFeature.DRAG
        onModificationStart: (route) => console.log route
        onModificationEnd: (route) => @updateRoute route
      )

      new OpenLayers.Control.SelectFeature(
        @drawLayer
        'displayClass': 'olControlDragFeature'
        box: true
        onSelect: (feat) => @showFeaturePopup feat
        onUnselect: (feat) => @removeFeaturePopup feat
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
        @fetchRoutes e.point.x, e.point.y
    )
    geolocationControl.events.register(
      "locationfailed"
      @
      () -> OpenLayers.Console.log 'Location detection failed'
    )
    @controls.push geolocationControl


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
        div
      allowSelection: true
    @controls.push searchControl

    # TODO: Add more controls here ....

    @map.addLayer @drawLayer
    super
    geolocationControl.activate()
    searchControl.activate()

  # ---------------------------- Popup Handlers  ------------------------------ #
  showFeaturePopup: (feat) ->
    feat.popup = new OpenLayers.Popup.FramedCloud(
      "route-popup"
      feat.geometry.getBounds().getCenterLonLat()
      null
      @routesPopupTemplate feat
      null
      true
      (evt) =>  @removeFeaturePopup feat
    )
    @map.addPopup(feat.popup)
    @reloadTagEvents()

    $('div.routes-popup').last().find('select').each () ->
      data = $(@).attr('data')
      $(@).find('option').each () -> $(@).attr('selected', true) unless $(@).val() != data
      $(@).change()

    that = @
    $('.saveRouteDataButton').last().bind 'click', (e) ->
      props = {}
      $(@).parent('div').find('ul.tagsList li').each () ->
        if  $(@).find('select.tagKeySelect').val() != ''
          props[$(@).find('select.tagKeySelect').val()]= $(@).find('select.tagValueSelect').val()
      console.log props
      feat.mapFeature.set "properties", props
      feat.mapFeature.set "name", $(@).parent('div').find('input.routeNameInput').val()
      feat.mapFeature.set "difficulty", parseInt($(@).parent('div').find('select.routeDifficultySelect').val())
      feat.mapFeature.save()
      that.removeFeaturePopup feat

    $('.removeRouteButton').last().bind 'click', (e) =>
      @removeFeaturePopup feat
      @deleteRoute feat

  removeFeaturePopup: (feat) ->
    @map.removePopup feat.popup
    feat.popup.destroy()
    feat.popup = null

  reloadTagEvents: ->
    that = @
    $('img.addTagImage').unbind('click').click () ->
      $(@).removeClass('addTagImage').addClass('removeTagImage').attr('src', '/assets/img/error.png')
      $('ul.tagsList').append that.routeTagTemplate()
      that.reloadTagEvents()
    $('img.removeTagImage').unbind('click').click () ->
      $(@).parent('li').remove()
      that.reloadTagEvents()
    # TODO: Get tagKeys from server
    $('select.tagKeySelect').change () ->
      # TODO: Get tagValues from server
      values = if $(@).val() is '' then [''] else [$(@).val()+'_1', $(@).val()+'_2', $(@).val()+'_3', $(@).val()+'_4']
      html = ""
      html = (html + "<option val='" + value + "'>" + value + "</option>") for value in values
      $(@).next('select').html html



  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @controls = []
    @baseLayers = []
    super


  # ---------------------------- Renderization ------------------------------ #
  render: -> super

  drawRoute: (data, bounds) ->
    route = new Maps.Route data
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    geom = geojsonFormat.read(JSON.stringify(route.get 'geometry'), 'Geometry')
    bounds.extend geom.getBounds()
    geom.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
    olGeom = new OpenLayers.Feature.Vector geom
    olGeom.mapFeature = route
    @drawLayer.addFeatures [olGeom]

  # ---------------------------- REST/Route Feature handlers ------------------------------ #
  saveRoute: (route) ->
    geom = route.geometry.clone().transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    ft = new Maps.Route
      name: 'dummyRoute'
      difficulty: 1 # TODO: pick this properties from elsewhere
      geometry: JSON.parse geojsonFormat.write geom
    ft.once 'change', (evt) =>
      route.mapFeature = ft
      @showFeaturePopup route
    ft.save()

  updateRoute: (route) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    geom = route.geometry.clone().transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    route.mapFeature.save geometry: JSON.parse geojsonFormat.write geom

  deleteRoute: (route) ->
    route.mapFeature.destroy success: () => @drawLayer.removeFeatures [route]

  fetchRoutes: (lon, lat) ->
    p = new OpenLayers.Geometry.Point lon, lat
    p.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    json = "{\"geometry\": #{geojsonFormat.write p} }"
    amount = 6 # TODO: we'll pick it up from a control
    difficulty = -1 # TODO: we'll pick it up from a control
    bounds = new OpenLayers.Bounds
    bounds.extend p
    $.ajax
      url: "/routes/near/#{amount}/difficulty/#{difficulty}"
      type: 'POST'
      contentType: 'application/json'
      data: json
      success: (data) =>
        @drawRoute r, bounds for r in data unless data == null
        bounds.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
        @map.zoomTo Math.floor @map.getZoomForExtent bounds

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

  selectLocation: (location) ->
    console.log location
    p = new OpenLayers.Geometry.Point location.lon, location.lat
    p.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
    @fetchRoutes p.x, p.y
    feature = new OpenLayers.Feature.Vector(
      p
      {}
      externalGraphic: location.icon
      pointRadius: 10
    )
    @drawLayer.removeAllFeatures()
    @drawLayer.addFeatures [feature]
    @map.zoomToExtent p.getBounds()
