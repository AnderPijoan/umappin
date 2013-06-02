window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.FeaturesMapView extends Maps.MapView
  drawLayer: null
  featurePopupTemplate: _.template $('#feature-popup-template').html()
  itemTagTemplate: _.template $('#item-tag-template').html()
  itemLikeTemplate: _.template $('#item-like-template').html()

  # ---------------------------- Controls ------------------------------ #
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

      new OpenLayers.Control.DragPan(
        'displayClass': 'olControlDragPan'
      )

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.Point
        'displayClass': 'olControlDrawFeaturePoint'
        featureAdded: (feature, pixel) => @saveFeature feature, "point"
      )

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.Path
        'displayClass': 'olControlDrawFeaturePath'
        featureAdded: (feature, pixel) => @saveFeature feature, "linestring"
      )

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.Polygon
        'displayClass': 'olControlDrawFeaturePolygon'
        featureAdded: (feature, pixel) => @saveFeature feature, "polygon"
      )

      new OpenLayers.Control.DrawFeature(
        @drawLayer
        OpenLayers.Handler.RegularPolygon
        'displayClass': 'olControlDrawFeatureRegularPolygon'
        handlerOptions: { sides: 8 }
        featureAdded: (feature, pixel) => @saveFeature feature, "polygon"
      )

      new OpenLayers.Control.ModifyFeature(
        @drawLayer
        'displayClass': 'olControlModifyFeature'
        mode: OpenLayers.Control.ModifyFeature.RESHAPE | OpenLayers.Control.ModifyFeature.DRAG
        createVertices: false #TODO handle new node creations ....
        #onModificationStart: (feature) => console.log feature
        onModificationEnd: (feature) => @updateFeature feature
      )

      new OpenLayers.Control.ModifyFeature(
        @drawLayer
        'displayClass': 'olControlModifyFeature'
        mode: OpenLayers.Control.ModifyFeature.RESIZE | OpenLayers.Control.ModifyFeature.ROTATE | OpenLayers.Control.ModifyFeature.DRAG
        #onModificationStart: (feature) => console.log feature
        onModificationEnd: (feature) => @updateFeature feature
      )

      new OpenLayers.Control.SelectFeature(
        @drawLayer
        'displayClass': 'olControlDragFeature'
        box: true
        onSelect: (feat) =>
          idf = feat.mapFeature.get 'id'
          idu = Account.session.get 'id'
          $.get "/featurelikesfeatureuser/#{idf}/#{idu}", (resp) =>
            feat.liked = parseInt(resp) > 0
            @showFeaturePopup feat
        onUnselect: (feat) => @removeFeaturePopup feat
      )

    ]
    toolbar = new OpenLayers.Control.Panel(
      displayClass: 'olControlEditingToolbar'
      defaultControl: toolBarControls[0]
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
    @fetchFeatures e.point.x, e.point.y

  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @controls = []
    @baseLayers = []
    super


  # ---------------------------- Renderization ------------------------------ #
  render: ->
    #features = @model.get 'features'
    #@drawFeature f for f in features unless features == null
    super


  # ---------------------------- Popup Handlers ------------------------------ #
  showFeaturePopup: (feat) ->
    feat.popup = new OpenLayers.Popup.FramedCloud(
      "feature-popup"
      feat.geometry.getBounds().getCenterLonLat()
      null
      @featurePopupTemplate feat
      null
      true
      (evt) =>  @removeFeaturePopup feat
    )
    @map.addPopup(feat.popup)
    @reloadTagEvents()

    that = @
    $('.saveFeatureDataButton').last().bind 'click', (e) ->
      props = {}
      $(@).parent('div').find('ul.tagsList li').each () ->
        if  $(@).find('select.tagKeySelect').val() != ''
          props[$(@).find('select.tagKeySelect').val()]= $(@).find('select.tagValueSelect').val()
      console.log props
      feat.mapFeature.set "properties", props
      feat.mapFeature.set('version', feat.mapFeature.get('version') + 1)
      feat.mapFeature.save()
      that.removeFeaturePopup feat

    $('.removeFeatureButton').last().bind 'click', (e) =>
      @removeFeaturePopup feat
      @deleteFeature feat

    $('.likeFeatureButton').last().popover
      html: true
      content: that.itemLikeTemplate feat.mapFeature
      container: 'body'

    $('.likeFeatureButton').last().change () ->
      rl = new Maps.FeatureLike
        featureId: feat.mapFeature.get 'id'
        userId: Account.session.get 'id'
        comment: $('#itemLikeComment-' + feat.mapFeature.get 'id').val()
      rl.save()
      $(@).popover('destroy')
      $(@).parent().append("<label style='float:right'>Liked!!</label>")
      $(@).remove()

  setLastTagsToSelects: (type) ->
    $('div.feature-popup').last().find("select.#{type}").each () ->
      data = $(@).attr('data')
      $(@).find('option').each () -> $(@).attr('selected', true) unless $(@).val() != data
      $(@).change()

  removeFeaturePopup: (feat) ->
    if (feat.popup)
      @map.removePopup feat.popup
      feat.popup.destroy()
      feat.popup = null

  reloadTagEvents: ->
    that = @
    $('img.addTagImage').unbind('click').click () ->
      $(@).removeClass('addTagImage').addClass('removeTagImage').attr('src', '/assets/img/error.png')
      $('ul.tagsList').append that.itemTagTemplate()
      that.reloadTagEvents()
    $('img.removeTagImage').unbind('click').click () ->
      $(@).parent('li').remove()
      that.reloadTagEvents()
    url = "http://taginfo.openstreetmap.org/api/4/keys/all?page=1&rp=100&filter=in_wiki&sortname=count_all&sortorder=desc"
    $.get url, (keys) =>
      html = "<option val=''></option>"
      html = (html + "<option val='#{entry.key}'>#{entry.key}</option>") for entry in keys.data
      $('select.tagKeySelect').html html
      that.setLastTagsToSelects('tagKeySelect')
    $('select.tagKeySelect').change () ->
      $(@).attr('data', $(@).val())
      url = "http://taginfo.openstreetmap.org/api/4/key/values?key=#{$(@).val()}&page=1&rp=100&sortname=count&sortorder=desc"
      $.get url, (resp) =>
        html = "<option val=''></option>"
        html = (html + "<option val='#{entry.value}'>#{entry.value}</option>") for entry in resp.data
        $(@).next('select').html(html).change () -> $(@).attr('data', $(@).val())
        that.setLastTagsToSelects('tagValueSelect')


  # ---------------------------- REST/Feature handlers ------------------------------ #
  ###
  drawFeature: (feat) ->
    feature = switch
      when feat.type is 'node' then new Maps.OsmNode id: feat.id
      when feat.type is 'way' then new Maps.OsmWay id: feat.id
      when feat.type is 'relation' then new Maps.OsmRelation id: feat.id
    feature.fetch complete: (resp) =>  @addFeatureToMap feature
  ###

  drawFeature: (feat, bounds) ->
    type = feat.geometry.type
    feature = switch
      when type is 'Point' then new Maps.OsmNode feat
      when (type is 'LineString' or type is 'Polygon') then new Maps.OsmWay feat
      when type is 'Relation' then new Maps.OsmRelation feat
    @addFeatureToMap feature, bounds

  saveFeature: (feature, type) ->
    console.log feature
    ft = @createOsmFeature feature, type
    ft.set('user', (@model.get 'ownerId'))
    ft.set('version', 1)
    ft.once 'change', (evt) =>
      feature.mapFeature = ft
      @reloadFeatures()
      ###
      fts = @model.get('features') or []
      osmtype = switch
        when type is 'point' then 'node'
        when (type is 'linestring' or type is 'polygon') then 'way'
        when type is 'relation' then 'relation'
      fts.push type: osmtype, id: ft.get 'id'
      @model.save features: fts
      ###
    ft.save()

  updateFeature: (feature) ->
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    osmgeom = feature.geometry.clone()
    osmgeom.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    geom = JSON.parse geojsonFormat.write osmgeom
    switch feature.mapFeature.get('geometry').type
      when "LineString", "Polygon"
        geom.node_ids = feature.mapFeature.get('geometry').node_ids
        versions = []
        versions.push(v + 1) for v in feature.mapFeature.get('geometry').node_versions
        geom.node_versions = versions
    feature.mapFeature.save
      geometry: geom
      version: feature.mapFeature.get('version') + 1
      { success: () => @reloadFeatures() }

  deleteFeature: (feature) ->
    feature.mapFeature.destroy success: ()=>
      features = @model.get 'features'
      @model.save
        features: features.splice(features.indexOf(feature.mapFeature.get 'id'), 1)
        { success: () => @drawLayer.removeFeatures [feature] }

  createOsmFeature: (feature, type) ->
    ft = switch
      when type is 'point' then new Maps.OsmNode
      when type is 'linestring', 'polygon' then new Maps.OsmWay
      when type is 'relation' then new Maps.OsmRelation
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    osmgeom = feature.geometry.clone()
    osmgeom.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    geom = JSON.parse geojsonFormat.write osmgeom
    nodes = []
    switch type
      when 'linestring'
        nodes.push 0 for c in feature.geometry.components
        geom.node_ids = nodes
      when 'polygon'
        nodes.push 0 for c in feature.geometry.components[0].components
        geom.node_ids = nodes
    ft.set('geometry', geom)

  addFeatureToMap: (feature, bounds) =>
    if feature.get('geometry')? and feature.get('geometry')!=null
      geojsonFormat = new OpenLayers.Format.GeoJSON()
      osmGeom = geojsonFormat.read(JSON.stringify(feature.get 'geometry'), 'Geometry')
      bounds.extend osmGeom.getBounds()
      geom = osmGeom.clone().transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
      olGeom = new OpenLayers.Feature.Vector geom
      olGeom.mapFeature = feature
      @drawLayer.addFeatures [olGeom]

  # ---------------------------- Search handler ------------------------------ #
  # Overriden
  selectLocation: (location) ->
    super
    p = new OpenLayers.Geometry.Point location.lon, location.lat
    p.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
    @fetchFeatures p.x, p.y
    @locationFeature = new OpenLayers.Feature.Vector(
      p
      {}
      externalGraphic: location.icon
      pointRadius: 10
    )
    @drawLayer.removeAllFeatures()
    @drawLayer.addFeatures [@locationFeature]
    @map.zoomToExtent p.getBounds()

  reloadFeatures: () ->
    p = @locationFeature.geometry
    @fetchFeatures p.x, p.y
    @drawLayer.removeAllFeatures()
    @drawLayer.addFeatures [@locationFeature]

  fetchFeatures: (lon, lat) ->
    p = new OpenLayers.Geometry.Point lon, lat
    p.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    geojsonFormat = new OpenLayers.Format.GeoJSON()
    json = "{\"geometry\": #{geojsonFormat.write p} }"
    amount = 20 # TODO: we'll pick it up from a control
    bounds = new OpenLayers.Bounds
    bounds.extend p
    $.ajax
      url: "/osmfeatures/near/#{amount}"
      type: 'POST'
      contentType: 'application/json'
      data: json
      success: (data) =>
        @drawFeature r, bounds for r in data unless data == null
        bounds.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
        @map.zoomTo Math.floor @map.getZoomForExtent bounds
