window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.MarkersMapView extends Maps.MapView
  markersLayer: null
  notesPopupTemplate: _.template $('#notes-popup-template').html()
  notesCommentTemplate: _.template $('#notes-comment-template').html()
  newNoteTemplate: _.template $('#notes-new-template').html()
  minMarkers: 1


  # ---------------------------- Controls ------------------------------ #
  initControls: ->

    # Add marker click control
    addMarkerControl = new OpenLayers.Control
    addMarkerControl.handler = new OpenLayers.Handler.Click(
      addMarkerControl
      'click': (e) => @createMarker @map.getLonLatFromViewPortPx e.xy
    )
    @controls.push addMarkerControl


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

    # Call superclass controls initialization
    super
    # Activate controls after loading stuff
    addMarkerControl.activate()
    geolocationControl.activate()



  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @controls = []
    @baseLayers = []
    @minMarkers = @options.minMarkers ? @minMarkers
    super
    # Markers layer
    @markersLayer = new OpenLayers.Layer.Markers "Markers"
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
                  $('.closeNoteButton').bind('click', (e) -> that.closeNote(e))
                OpenLayers.Event.stop(evt)
              @markersLayer.addMarker(marker)
          bbox.transform(Maps.MapView.OSM_PROJECTION, @map.getProjectionObject())
          bbox.extend new OpenLayers.LonLat(origin.x, origin.y)
          @map.zoomToExtent bbox
          @map.zoomOut()

  createMarker: (lnglat) ->
    feat = new OpenLayers.Feature @markersLayer, lnglat
    feat.popupClass = OpenLayers.Popup.FramedCloud
    feat.data.popupContentHTML = @newNoteTemplate feat.id
    feat.data.overflow = 'auto'
    marker = feat.createMarker()
    that = @
    lonlat = new OpenLayers.LonLat(lnglat.lon, lnglat.lat).transform(
      @map.getProjectionObject()
      Maps.MapView.OSM_PROJECTION
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

  # ----------------- Simple regex XML string parser to JSON (just in case we need) ----------------- #
  xml2json: (xml) ->
    regex = /(<\w+[^<]*?)\s+([\w-]+)="([^"]+)">/
    xml = xml.replace(regex, '$1><$2>$3</$2>') while xml.match(regex)
    xml = xml.replace(/\s/g, ' ').
    replace(/< *\?[^>]*?\? *>/g, '').
    replace(/< *!--[^>]*?-- *>/g, '').
    replace(/< *(\/?) *(\w[\w-]+\b):(\w[\w-]+\b)/g, '<$1$2_$3').
    replace(/< *(\w[\w-]+\b)([^>]*?)\/ *>/g, '< $1$2>').
    replace(/(\w[\w-]+\b):(\w[\w-]+\b) *= *"([^>]*?)"/g, '$1_$2="$3"').
    replace(/< *(\w[\w-]+\b)((?: *\w[\w-]+ *= *" *[^"]*?")+ *)>( *[^< ]*?\b.*?)< *\/ *\1 *>/g, '< $1$2 value="$3">').
    replace(/< *(\w[\w-]+\b) *</g, '<$1>< ').
    replace(/> *>/g, '>').
    replace(/"/g, '\\"').
    replace(/< *(\w[\w-]+\b) *>([^<>]*?)< *\/ *\1 *>/g, '"$1":"$2",').
    replace(/< *(\w[\w-]+\b) *>([^<>]*?)< *\/ *\1 *>/g, '"$1":[{$2}],').
    replace(/< *(\w[\w-]+\b) *>(?=("\w[\w-]+\b)":\{.*?\},\2)(.*?)< *\/ *\1 *>/, '"$1":{}$3},').
    replace(/],\s*?".*?": *\[/g, ',').
    replace(/< \/(\w[\w-]+\b)\},\{\1>/g, '},{').
    replace(/< *(\w[\w-]+\b)[^>]*?>/g, '"$1":{').
    replace(/< *\/ *\w[\w-]+ *>/g, '},').
    replace(/\} *,(?= *(\}|\]))/g, '}').
    replace(/] *,(?= *(\}|\]))/g, ']').
    replace(/" *,(?= *(\}|\]))/g, '"').
    replace(/\s*, *$/g, '')
    xml = '{' + xml + '}'
    JSON.parse xml

  # Aux function for easily extending bounds
  extendBounds: (bbox, ext) ->
    xbbox = bbox.toArray(false)
    xbbox = [xbbox[0]-ext, xbbox[1]-ext, xbbox[2]+ext, xbbox[3]+ext]
    new OpenLayers.Bounds(xbbox)

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