window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.MarkersMapView extends Maps.MapView
  markersLayer: null
  notesPopupTemplate: _.template $('#notes-popup-template').html()
  notesCommentTemplate: _.template $('#notes-comment-template').html()
  newNoteTemplate: _.template $('#notes-new-template').html()
  minMarkers: 3


  # ---------------------------- Controls ------------------------------ #
  initControls: ->

    # Add marker click control
    addMarkerControl = new OpenLayers.Control
    addMarkerControl.handler = new OpenLayers.Handler.Click(
      addMarkerControl
      'click': (e) => @createMarker @map.getLonLatFromViewPortPx e.xy
    )
    @controls.push addMarkerControl

    # Call superclass controls initialization
    super
    # Activate controls after loading stuff
    addMarkerControl.activate()


  # ---------------------------- Geolocation Handler ------------------------------ #
  # Overriden
  handleGeoLocated: (e) ->
    super
    @getNotesAroundLonLat e.point.x, e.point.y

  # ---------------------------- Search handler ------------------------------ #
  # Overriden
  selectLocation: (location) ->
    super
    p = new OpenLayers.Geometry.Point location.lon, location.lat
    p.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
    @getNotesAroundLonLat p.x, p.y

    marker = new OpenLayers.Marker(
      new OpenLayers.LonLat(p.x, p.y)
      new OpenLayers.Icon location.icon
    )
    @markersLayer.addMarker marker
    @map.zoomToExtent p.getBounds()


  getNotesAroundLonLat: (lon, lat) ->
    p = new OpenLayers.Geometry.Point lon, lat
    p.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    @getOSMNotes p.getBounds(), new OpenLayers.Geometry.Point(lon, lat)

  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @controls = []
    @baseLayers = []
    @minMarkers = @options.minMarkers ? @minMarkers
    super
    # Markers layer
    @markersLayer = new OpenLayers.Layer.Markers "Notes"
    @map.addLayer @markersLayer


  # -------------------------------- OSM notes loader ---------------------------- #
  getOSMNotes: (bbox, origin) ->
    $.ajax
      url: 'http://api.openstreetmap.org/api/0.6/notes.json?bbox=' + bbox.toBBOX()
      success: (data) =>
        data = JSON.parse data unless $.isPlainObject(data)
        if data.features.length < @minMarkers
          @getOSMNotes @extendBounds(bbox, 0.05), origin
        else
          bbox = new OpenLayers.Bounds
          for feature in data.features
            do (feature) =>
              x = feature.geometry.coordinates[0]
              y = feature.geometry.coordinates[1]
              lonlat = new OpenLayers.LonLat(x, y)
              bbox.extend lonlat
              feat = new OpenLayers.Feature @markersLayer, lonlat.transform(
                Maps.MapView.OSM_PROJECTION
                @map.getProjectionObject()
              )
              feat.popupClass = OpenLayers.Popup.FramedCloud
              feat.data.popupContentHTML = @notesPopupTemplate feature
              feat.data.overflow = 'auto'
              marker = feat.createMarker()
              that = @
              marker.events.register "mousedown", feat, (evt) -> that.selectMarkerHandler evt, feat, lonlat
              marker.events.register "touchstart", feat, (evt) -> that.selectMarkerHandler evt, feat, lonlat
              @markersLayer.addMarker(marker)
          bbox.transform(Maps.MapView.OSM_PROJECTION, @map.getProjectionObject())
          bbox.extend new OpenLayers.LonLat(origin.x, origin.y)
          @map.zoomTo Math.round @map.getZoomForExtent bbox
          @map.zoomOut()

  createMarker: (lnglat) ->
    feat = new OpenLayers.Feature @markersLayer, lnglat
    feat.popupClass = OpenLayers.Popup.FramedCloud
    popupdata = id: feat.id, lon: lnglat.lon, lat: lnglat.lat
    feat.data.popupContentHTML = @newNoteTemplate popupdata
    feat.data.overflow = 'auto'
    marker = feat.createMarker()
    that = @
    lonlat = new OpenLayers.LonLat(lnglat.lon, lnglat.lat).transform(
      @map.getProjectionObject()
      Maps.MapView.OSM_PROJECTION
    )
    marker.events.register "mousedown", feat, (evt) -> that.selectNewMarkerHandler evt, feat, lonlat
    marker.events.register "touchstart", feat, (evt) -> that.selectNewMarkerHandler evt, feat, lonlat
    @markersLayer.addMarker marker

  selectNewMarkerHandler: (evt, marker, lonlat) ->
    if marker.popup
      marker.popup.toggle()
    else
      marker.popup = marker.createPopup(true)
      @map.addPopup(marker.popup)
      marker.popup.show()
      $('.newNoteButton').bind('click', (evt) => @postNewNote(evt, lonlat))
      $('.removeMarkerButton').bind 'click', (evt) -> marker.destroy()
    OpenLayers.Event.stop(evt)

  selectMarkerHandler: (evt, marker, lonlat) ->
    if marker.popup
      marker.popup.toggle()
    else
      marker.popup = marker.createPopup(true)
      @map.addPopup(marker.popup)
      marker.popup.show()
      $('.newNoteCommentButton').bind('click', (e) => @postNoteComment(e))
      $('.closeNoteButton').bind('click', (e) => @closeNote(e))
    OpenLayers.Event.stop(evt)

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
      data = @note2json $(data).find('note')
      $(evt.target).parent().parent().html @notesPopupTemplate data
      $('.newNoteCommentButton').bind('click', (e) => @postNoteComment(e))
      $('.closeNoteButton').bind('click', (e) => @closeNote(e))

  closeNote: (evt) ->
    id = $(evt.target).attr('id').split('_')[1]
    comment = 'text=' + $('#newNoteCommentText_'+id).val()
    url = $('#closeNoteUrl_'+id).val()
    $.post url + '?' + comment, (data) =>
      container = $(evt.target).parent().parent().find('div.noteComments')
      data = JSON.parse data unless $.isPlainObject(data)
      postedcomment = data.properties.comments[data.properties.comments.length-1]
      container.append @notesCommentTemplate postedcomment

  # Aux function for easily extending bounds
  extendBounds: (bbox, ext) ->
    xbbox = bbox.toArray(false)
    xbbox = [xbbox[0]-ext, xbbox[1]-ext, xbbox[2]+ext, xbbox[3]+ext]
    new OpenLayers.Bounds(xbbox)


  # -------------------------- custom xml to json parser -------------------------- #
  note2json: (data) ->
    json =
      id: $(data).find('id').text()
      lat: $(data).attr('lat')
      lon: $(data).attr('lon')
      url: $(data).find('url').text().replace(/\/(\d+)$/, '/$1.json')
      comment_url: $(data).find('comment_url').text().replace('comment', 'comment.json')
      close_url: $(data).find('close_url').text().replace('close', 'close.json')
      date_created: $(data).find('date_created').text()
      status: $(data).find('status').text()
      comments: []
    $(data).find('comments comment').each () ->
      json.comments.push
        date: $(@).find('date').text()
        text: $(@).find('text').text()
        html: $(@).find('html').text()
    json
