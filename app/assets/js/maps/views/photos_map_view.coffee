window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.PhotosMapView extends Maps.MapView
  photosLayer: null
  photosPopupTemplate: _.template $('#photos-popup-template').html()
  photosCommentTemplate: _.template $('#photos-comment-template').html()
  newPhotoTemplate: _.template $('#photos-new-template').html()
  minPhotos: 3


  # ---------------------------- Controls ------------------------------ #
  # Overriden
  initControls: ->

    # Add photo click control
    addPhotoControl = new OpenLayers.Control
    addPhotoControl.handler = new OpenLayers.Handler.Click(
      addPhotoControl
      'click': (e) => @createPhoto @map.getLonLatFromViewPortPx e.xy
    )
    @controls.push addPhotoControl

    # Call superclass controls initialization
    super
    # Activate controls after loading stuff
    addPhotoControl.activate()


  # ---------------------------- Geolocation Handler ------------------------------ #
  # Overriden
  handleGeoLocated: (e) ->
    super
    @getPhotosAroundLonLat e.point.x, e.point.y

  # ---------------------------- Search handler ------------------------------ #
  # Overriden
  selectLocation: (location) ->
    super
    p = new OpenLayers.Geometry.Point location.lon, location.lat
    p.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
    @getPhotosAroundLonLat p.x, p.y

    marker = new OpenLayers.Marker(
      new OpenLayers.LonLat(p.x, p.y)
      new OpenLayers.Icon location.icon
    )
    @photosLayer.addMarker marker
    @map.zoomToExtent p.getBounds()


  getPhotosAroundLonLat: (lon, lat) ->
    p = new OpenLayers.Geometry.Point lon, lat
    p.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    @getMapPhotos p.getBounds(), new OpenLayers.Geometry.Point(lon, lat)

  # ---------------------------- Initialization ------------------------------ #
  initialize: ->
    @controls = []
    @baseLayers = []
    @minPhotos = @options.minPhotos ? @minPhotos
    super
    # Photos layer
    @photosLayer = new OpenLayers.Layer.Markers "Photos"
    @map.addLayer @photosLayer


  # -------------------------------- OSM photos loader ---------------------------- #
  getMapPhotos: (bbox, origin) ->
    $.ajax
      url: 'http://api.openstreetmap.org/api/0.6/notes.json?bbox=' + bbox.toBBOX()
      success: (data) =>
        json = $.parseJSON data
        json = json ? data
        #data = JSON.parse data unless $.isPlainObject(data)
        if json.features.length < @minPhotos
          @getMapPhotos @extendBounds(bbox, 0.05), origin
        else
          bbox = new OpenLayers.Bounds
          for feature in json.features
            do (feature) =>
              x = feature.geometry.coordinates[0]
              y = feature.geometry.coordinates[1]
              lonlat = new OpenLayers.LonLat(x, y)
              bbox.extend lonlat
              feat = new OpenLayers.Feature @photosLayer, lonlat.transform(
                Maps.MapView.OSM_PROJECTION
                @map.getProjectionObject()
              )
              feat.popupClass = OpenLayers.Popup.FramedCloud
              feat.data.popupContentHTML = @photosPopupTemplate feature
              feat.data.overflow = 'auto'
              size = new OpenLayers.Size 32, 32
              offset = new OpenLayers.Pixel(-(size.w/2), -size.h)
              feat.data.icon = new OpenLayers.Icon('/assets/img/140x140.gif', size, offset)
              photo = feat.createMarker()
              that = @
              photo.events.register "mousedown", feat, (evt) -> that.selectPhotoHandler evt, feat, lonlat
              photo.events.register "touchstart", feat, (evt) -> that.selectPhotoHandler evt, feat, lonlat
              @photosLayer.addMarker(photo)
          bbox.transform(Maps.MapView.OSM_PROJECTION, @map.getProjectionObject())
          bbox.extend new OpenLayers.LonLat(origin.x, origin.y)
          @map.zoomTo Math.round @map.getZoomForExtent bbox
          @map.zoomOut()

  createPhoto: (lnglat) ->
    feat = new OpenLayers.Feature @photosLayer, lnglat
    feat.popupClass = OpenLayers.Popup.FramedCloud
    popupdata = id: feat.id, lon: lnglat.lon, lat: lnglat.lat
    feat.data.popupContentHTML = @newPhotoTemplate popupdata
    feat.data.overflow = 'auto'
    size = new OpenLayers.Size 32, 32
    offset = new OpenLayers.Pixel(-(size.w/2), -size.h)
    feat.data.icon = new OpenLayers.Icon('/assets/img/140x140.gif', size, offset)
    photo = feat.createMarker()
    that = @
    lonlat = new OpenLayers.LonLat(lnglat.lon, lnglat.lat).transform(
      @map.getProjectionObject()
      Maps.MapView.OSM_PROJECTION
    )
    photo.events.register "mousedown", feat, (evt) -> that.selectNewPhotoHandler evt, feat, lonlat
    photo.events.register "touchstart", feat, (evt) -> that.selectNewPhotoHandler evt, feat, lonlat
    @photosLayer.addMarker photo

  selectNewPhotoHandler: (evt, photo, lonlat) ->
    if photo.popup
      photo.popup.toggle()
    else
      photo.popup = photo.createPopup(true)
      @map.addPopup(photo.popup)
      photo.popup.show()
      $('.newPhotoButton').bind('click', (evt) => @postNewPhoto(evt, lonlat))
      $('.removePhotoButton').bind 'click', (evt) -> photo.destroy()
    OpenLayers.Event.stop(evt)

  selectPhotoHandler: (evt, photo, lonlat) ->
    if photo.popup
      photo.popup.toggle()
    else
      photo.popup = photo.createPopup(true)
      @map.addPopup(photo.popup)
      photo.popup.show()
      $('.newPhotoCommentButton').bind('click', (e) => @postPhotoComment(e))
      $('.closePhotoButton').bind('click', (e) => @closePhoto(e))
    OpenLayers.Event.stop(evt)

  postPhotoComment: (evt) ->
    id = $(evt.target).attr('id').split('_')[1]
    comment = 'text=' + $('#newPhotoCommentText_'+id).val()
    url = $('#newPhotoCommentUrl_'+id).val()
    $.post url + '?' + comment, (data) =>
      json = $.parseJSON data
      json = json ? data
      container = $(evt.target).parent().parent().find('div.photoComments')
      #data = JSON.parse data unless $.isPlainObject(data)
      postedcomment = json.properties.comments[json.properties.comments.length-1]
      container.append @photosCommentTemplate postedcomment

  postNewPhoto: (evt, lonlat) ->
    id = $(evt.target).attr('id').split('-')[1]
    comment = $(evt.target).prev().val()
    url = 'http://api.openstreetmap.org/api/0.6/photos?lat='+lonlat.lat+'&lon='+lonlat.lon+'&text='+comment
    $.post url, (data) =>
      data = @photo2json $(data).find('photo') # TODO check here photo creation
      $(evt.target).parent().parent().html @photosPopupTemplate data
      $('.newPhotoCommentButton').bind('click', (e) => @postPhotoComment(e))
      $('.closePhotoButton').bind('click', (e) => @closePhoto(e))

  closePhoto: (evt) ->
    id = $(evt.target).attr('id').split('_')[1]
    comment = 'text=' + $('#newPhotoCommentText_'+id).val()
    url = $('#closePhotoUrl_'+id).val()
    $.post url + '?' + comment, (data) =>
      json = $.parseJSON data
      json = json ? data
      container = $(evt.target).parent().parent().find('div.photoComments')
      #data = JSON.parse data unless $.isPlainObject(data)
      postedcomment = json.properties.comments[json.properties.comments.length-1]
      container.append @photosCommentTemplate postedcomment

  # Aux function for easily extending bounds
  extendBounds: (bbox, ext) ->
    xbbox = bbox.toArray(false)
    xbbox = [xbbox[0]-ext, xbbox[1]-ext, xbbox[2]+ext, xbbox[3]+ext]
    new OpenLayers.Bounds(xbbox)


  # -------------------------- custom xml to json parser -------------------------- #
  photo2json: (data) ->
    json = data
    ###
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
    ###
