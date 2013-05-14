window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.MapView extends Backbone.View
  width: '60em'
  height: '30em'
  readonly: false
  map: null
  drawLayer: null
  markersLayer: null
  controls: []
  notesPopupTemplate: _.template $('#notes-popup-template').html()
  notesCommentTemplate: _.template $('#notes-comment-template').html()
  newNoteTemplate: _.template $('#notes-new-template').html()
  selectedFeatures: []

  # ----------------------------- Base Layers ------------------------------- #
  baseLayers: [

    new OpenLayers.Layer.OSM(
      "OSM"
      [ "http://a.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png"
        "http://b.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png"
        "http://c.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png" ]
      layers: "basic"
      isBaseLayer: true
      resolutions: [156543.03390625, 78271.516953125, 39135.7584765625,
                    19567.87923828125, 9783.939619140625, 4891.9698095703125,
                    2445.9849047851562, 1222.9924523925781, 611.4962261962891,
                    305.74811309814453, 152.87405654907226, 76.43702827453613,
                    38.218514137268066, 19.109257068634033, 9.554628534317017,
                    4.777314267158508, 2.388657133579254, 1.194328566789627,
                    0.5971642833948135, 0.25, 0.1, 0.05]
      serverResolutions: [156543.03390625, 78271.516953125, 39135.7584765625,
                          19567.87923828125, 9783.939619140625,
                          4891.9698095703125, 2445.9849047851562,
                          1222.9924523925781, 611.4962261962891,
                          305.74811309814453, 152.87405654907226,
                          76.43702827453613, 38.218514137268066,
                          19.109257068634033, 9.554628534317017,
                          4.777314267158508, 2.388657133579254,
                          1.194328566789627, 0.5971642833948135]
      transitionEffect: 'resize'
    )

    new OpenLayers.Layer.Bing(
      name: "Bing-Road"
      key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
      type: "Road"
      metadataParams: { mapVersion: "v1" }
      isBaseLayer: true
    )

    new OpenLayers.Layer.Bing(
      name: "Bing-Aerial"
      key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
      type: "Aerial"
      isBaseLayer: true
    )

    new OpenLayers.Layer.Bing(
      name: "Bing-AerialWithLabels"
      key: "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf"
      type: "AerialWithLabels"
      isBaseLayer: true
    )

    new OpenLayers.Layer.WMS(
      "WMS"
      "http://vmap0.tiles.osgeo.org/wms/vmap0"
      layers: "basic"
      isBaseLayer: true
    )

  ]


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
        onModificationStart: (feature) => @selectedFeatures.push feature
        onModificationEnd: (feature) =>
          @updateFeature feature
          @selectedFeatures.splice(@selectedFeatures.indexOf(feature), 1)
      )

      new OpenLayers.Control.ModifyFeature(
        @drawLayer
        'displayClass': 'olControlRotateScaleFeature'
        mode: OpenLayers.Control.ModifyFeature.RESIZE | OpenLayers.Control.ModifyFeature.ROTATE | OpenLayers.Control.ModifyFeature.DRAG
        onModificationStart: (feature) => @selectedFeatures.push feature
        onModificationEnd: (feature) =>
          @updateFeature feature
          @selectedFeatures.splice(@selectedFeatures.indexOf(feature), 1)
      )

      new OpenLayers.Control.DragFeature(
        @drawLayer
        'displayClass': 'olControlDragFeature'
        onComplete: (feature, pixel) => @updateFeature feature
      )

      new OpenLayers.Control.Button(
        'displayClass': "olControlSaveAllFeatures"
        trigger: () -> console.log "TODO: Trigger save all!"
      )

      new OpenLayers.Control.Button(
        'displayClass': "olControlAddMarker"
        trigger: () ->
          if addMarkerControl.active
            elm = $('div.olControlAddMarkerItemActive')
            $(elm).removeClass 'olControlAddMarkerItemActive'
            $(elm).addClass 'olControlAddMarkerItemInactive'
            addMarkerControl.deactivate()
          else
            elm = $('div.olControlAddMarkerItemInactive')
            $(elm).removeClass 'olControlAddMarkerItemInactive'
            $(elm).addClass 'olControlAddMarkerItemActive'
            addMarkerControl.activate()

      )

      new OpenLayers.Control.Button(
        'displayClass': "olControlSelect"
        trigger: () -> console.log "TODO: Popup events to Features!"
      )

    ]
    toolbar = new OpenLayers.Control.Panel(
      displayClass: 'olControlEditingToolbar'
      defaultControl: toolBarControls[6]
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
    #keyboardControl.activate()


    @controls.push toolbar
    @controls.push keyboardControl
    @controls.push addMarkerControl
    @controls.push new OpenLayers.Control.LayerSwitcher()
    @controls.push new OpenLayers.Control.MousePosition()

    # TODO: Add more controls here ....

    @map.addLayer @drawLayer
    @map.addControls @controls


  # -------------------------------- OSM notes Layer ---------------------------- #
  getOSMNotes: (bbox) ->
    $.ajax
      url: 'http://api.openstreetmap.org/api/0.6/notes.json?bbox=' + bbox
      success: (data) =>
        bounds = new OpenLayers.Bounds()
        data = JSON.parse data unless $.isPlainObject(data)
        for feature in data.features
          do (feature) =>
            x = feature.geometry.coordinates[0]
            y = feature.geometry.coordinates[1]
            lonlat = new OpenLayers.LonLat(x, y).transform(
              new OpenLayers.Projection("EPSG:4326")
              @map.getProjectionObject()
            )
            bounds.extend lonlat
            feat = new OpenLayers.Feature @markersLayer, lonlat
            feat.popupClass = OpenLayers.Popup.FramedCloud
            feat.data.popupContentHTML = @notesPopupTemplate feature.properties
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
                $('.newNoteCommentButton').bind('click', (e) -> that.postNoteComment(e))
              OpenLayers.Event.stop(evt)
            @markersLayer.addMarker(marker)
        @map.zoomToExtent bounds


  initialize: ->
    @width = @options.width ? @width
    @height = @options.height ? @height
    @readonly = @options.readonly? @readonly

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
    @map.addLayers @baseLayers
    @map.setLayerIndex(baseLayer, 40) for baseLayer in @baseLayers
    # 2. Add markers layer
    @markersLayer = new OpenLayers.Layer.Markers "Markers"
    @map.addLayer @markersLayer
    @markersLayer.setZIndex 20
    @map.setLayerIndex(@markersLayer, 20)
    # 3. Add controls layer
    @initControls()
    @drawLayer.setZIndex 30
    @map.setLayerIndex(@drawLayer, 30)
    # 4. Initial map zoom
    @map.zoomToMaxExtent()
    # ---- Testing Notes ---- #
    @getOSMNotes('-0.65094,51.312159,0.374908,51.669148')

  render: ->
    features = @model.get 'features'
    @drawFeature f for f in features unless features == null
    @

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


  createMarker: (lnglat) ->
    feat = new OpenLayers.Feature @markersLayer, lnglat
    feat.popupClass = OpenLayers.Popup.FramedCloud
    feat.data.popupContentHTML = @newNoteTemplate feat.id
    feat.data.overflow = 'auto'
    marker = feat.createMarker()
    that = @
    lonlat = new OpenLayers.LonLat(lnglat.lon, lnglat.lat).transform(
      @map.getProjectionObject()
      new OpenLayers.Projection("EPSG:4326")
    )
    marker.events.register "mousedown", feat, (evt) ->
      if @popup
        @popup.toggle()
      else
        @popup = @createPopup(true)
        that.map.addPopup(@popup)
        @popup.show()
        $('.newNoteButton').bind('click', (evt) -> that.postNewNote(evt, lonlat))
      OpenLayers.Event.stop(evt)
    @markersLayer.addMarker marker


  postNoteComment: (evt) ->
    id = $(evt.target).attr('id').split('_')[1]
    comment = 'text=' + $('#newNoteCommentText_'+id).val()
    url = $('#newNoteCommentUrl_'+id).val()
    $.post url + '?' + comment, (data) =>
      container = $(evt.target).parent().parent().find('div.noteComments')
      data = JSON.parse data unless $.isPlainObject(data)
      postedcomment = data.properties.comments[data.properties.comments.length-1]
      container.append @notesCommentTemplate postedcomment

  postNewNote: (evt, lonlat) ->
    id = $(evt.target).attr('id').split('-')[1]
    comment = $(evt.target).prev().val()
    url = 'http://api.openstreetmap.org/api/0.6/notes?lat='+lonlat.lat+'&lon='+lonlat.lon+'&text='+comment
    $.post url, (data) =>
      console.log data
      # TODO Reload the single marker