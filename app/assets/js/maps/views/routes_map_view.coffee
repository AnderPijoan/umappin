window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.RoutesMapView extends Maps.MapView
  drawLayer: null
  routesPopupTemplate: _.template $('#routes-popup-template').html()
  routeTagTemplate: _.template $('#route-tag-template').html()
  routeLikeTemplate: _.template $('#route-like-template').html()
  # ---------------------------- Controls ------------------------------ #
  #Overriden
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
        onSelect: (feat) =>
          idr = feat.mapFeature.get 'id'
          idu = Account.session.get 'id'
          $.get "/routelikesrouteuser/#{idr}/#{idu}", (resp) =>
            feat.liked = parseInt(resp) > 0
            @showFeaturePopup feat
        onUnselect: (feat) => @removeFeaturePopup feat
      )

    ]

    toolbar = new OpenLayers.Control.Panel(
      displayClass: 'olControlEditingToolbar'
      defaultControl: toolBarControls[5]
    )
    toolbar.addControls toolBarControls
    @controls.push toolbar

    # TODO: Add more controls here ....

    @map.addLayer @drawLayer
    super

  # ---------------------------- Geolocation Handler ------------------------------ #
  #Overriden
  handleGeoLocated: (e) ->
    super
    @map.zoomToExtent e.point.getBounds()
    @fetchRoutes e.point.x, e.point.y

  # ---------------------------- Popup Handlers ------------------------------ #
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

    $('.likeRouteButton').last().popover
      html: true
      content: that.routeLikeTemplate feat.mapFeature
      container: 'body'

    $('.likeRouteButton').last().change () ->
      console.log $(@).parent()
      rl = new Maps.RouteLike
        routeId: feat.mapFeature.get 'id'
        userId: Account.session.get 'id'
        comment: $('#routeLikeComment-' + feat.mapFeature.get 'id').val()
      rl.save()
      $(@).popover('destroy')
      $(@).parent().append("<label style='float:right'>Liked!!</label>")
      $(@).remove()

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
    url = "http://taginfo.openstreetmap.org/api/4/keys/all?page=1&rp=100&filter=in_wiki&sortname=count_all&sortorder=desc"
    $.get url, (keys) =>
      html = "<option val=''>&nbsp;</option>"
      html = (html + "<option val='#{entry.key}'>#{entry.key}</option>") for entry in keys.data
      $('select.tagKeySelect').html html
    $('select.tagKeySelect').change () ->
      url = "http://taginfo.openstreetmap.org/api/4/key/values?key=#{$(@).val()}&page=1&rp=100&sortname=count&sortorder=desc"
      $.get url, (resp) =>
        html = "<option val=''>&nbsp;</option>"
        html = (html + "<option val='#{entry.value}'>#{entry.value}</option>") for entry in resp.data
        $(@).next('select').html html


  # ---------------------------- Popup Handlers ------------------------------ #
  saveRouteLike: (routeId) ->
    alert "Save #{routeId} !!"


  # ---------------------------- Initialization ------------------------------ #
  #Overriden
  initialize: ->
    @controls = []
    @baseLayers = []
    super


  # ---------------------------- Renderization ------------------------------ #
  #Overriden
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
  # Overriden
  selectLocation: (location) ->
    super
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
