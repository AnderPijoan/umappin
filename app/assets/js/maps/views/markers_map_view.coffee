window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.MarkersMapView extends Maps.MapView
  markersLayer: null
  notesPopupTemplate: _.template $('#notes-popup-template').html()
  notesCommentTemplate: _.template $('#notes-comment-template').html()
  newNoteTemplate: _.template $('#notes-new-template').html()

  # ---------------------------- Controls Layer ------------------------------ #
  initControls: ->
    addMarkerControl = new OpenLayers.Control
    addMarkerControl.handler = new OpenLayers.Handler.Click(
      addMarkerControl
      'click': (e) => @createMarker @map.getLonLatFromViewPortPx e.xy
    )

    @controls.push addMarkerControl
    super
    addMarkerControl.activate()

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
                $('.closeNoteButton').bind('click', (e) -> that.closeNote(e))
              OpenLayers.Event.stop(evt)
            @markersLayer.addMarker(marker)
        @map.zoomToExtent bounds


  initialize: ->
    super
    # E. Add markers layer
    @markersLayer = new OpenLayers.Layer.Markers "Markers"
    @map.addLayer @markersLayer
    # ---- Testing Notes ---- #
    @getOSMNotes('-0.65094,51.312159,0.374908,51.669148')


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
        $('.closeNoteButton').bind('click', (evt) -> that.closeNote(evt, lonlat))
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

  closeNote: (evt) ->
    id = $(evt.target).attr('id').split('_')[1]
    comment = 'text=' + $('#newNoteCommentText_'+id).val()
    url = $('#closeNoteUrl_'+id).val()
    $.post url + '?' + comment, (data) =>
      container = $(evt.target).parent().parent().find('div.noteComments')
      data = JSON.parse data unless $.isPlainObject(data)
      postedcomment = data.properties.comments[data.properties.comments.length-1]
      container.append @notesCommentTemplate postedcomment