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
    @getPhotosAroundLocation e.point

  # ---------------------------- Search handler ------------------------------ #
  # Overriden
  selectLocation: (location) ->
    super
    p = new OpenLayers.Geometry.Point location.lon, location.lat
    p.transform Maps.MapView.OSM_PROJECTION, @map.getProjectionObject()
    marker = new OpenLayers.Marker(
      new OpenLayers.LonLat(p.x, p.y)
      new OpenLayers.Icon location.icon
    )
    @photosLayer.addMarker marker
    @getPhotosAroundLocation p

  getPhotosAroundLocation: (p) ->
    @map.zoomToExtent p.getBounds()
    @getViewPortPhotos()

  getViewPortPhotos: () ->
    viewBounds = @map.getExtent().transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    v = viewBounds.toArray(false)
    bbox = "x1=#{v[0]}&x2=#{v[2]}&y1=#{v[1]}&y2=#{v[3]}"
    @getMapPhotos bbox

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
  getMapPhotos: (bbox) ->
    $.ajax
      url: "/photos/rect?#{bbox}"
      success: (data) =>
        json = $.parseJSON data
        json = json ? data
        #data = JSON.parse data unless $.isPlainObject(data)
        results = json.results
        if (results.length < @minPhotos) and (@map.getZoom() > 2)
          @map.zoomOut()
          @getViewPortPhotos()
        else
          for feature in results
            do (feature) =>
              x = feature.get('longitude')
              y = feature.get('latitude')
              lonlat = new OpenLayers.LonLat(x, y)
              feat = new OpenLayers.Feature @photosLayer, lonlat.transform(
                Maps.MapView.OSM_PROJECTION
                @map.getProjectionObject()
              )
              feat.popupClass = OpenLayers.Popup.FramedCloud
              feat.data.popupContentHTML = @photosPopupTemplate feature
              feat.data.overflow = 'auto'
              size = new OpenLayers.Size 32, 32
              offset = new OpenLayers.Pixel(-(size.w/2), -size.h)
              src = feature.get('get_photo_content') ? '/assets/img/140x140.gif'
              feat.data.icon = new OpenLayers.Icon(src, size, offset)
              photo = feat.createMarker()
              feat.mapPhoto = feature
              that = @
              photo.events.register "mousedown", feat, (evt) -> that.selectPhotoHandler evt, feat, lonlat
              photo.events.register "touchstart", feat, (evt) -> that.selectPhotoHandler evt, feat, lonlat
              @photosLayer.addMarker(photo)

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
    p = new OpenLayers.Geometry.Point lnglat.lon, lnglat.lat
    p.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    picture = new Maps.Picture
      latitude: p.y
      longitude: p.x
      owner_id: Account.session.get('id')
      date_created: new Date()
    picture.save()
    feat.mapPhoto = picture

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
      @addPictureView photo.mapPhoto, 'newPhotoHolder'
      $('.newPhotoButton').bind('click', (evt) => @postNewPhoto(evt, lonlat))
      $('.removePhotoButton').bind 'click', (evt) -> photo.destroy()
    OpenLayers.Event.stop(evt)

  addPictureView: (picture, containerClass) ->
    @featurePictureView = new PictureView
      model: picture
      readonly: picture.get("owner_id") != Account.session.get("id")
      showInfo: false
      picWidth: '8em'
    $("div.#{containerClass}").last().append @featurePictureView.render().el

  selectPhotoHandler: (evt, photo, lonlat) ->
    if photo.popup
      photo.popup.toggle()
    else
      photo.popup = photo.createPopup(true)
      @map.addPopup(photo.popup)
      photo.popup.show()
      @addPictureView photo.mapPhoto, 'mapPhotoHolder'
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
    url = '/photos?lat='+lonlat.lat+'&lon='+lonlat.lon+'&text='+comment
    $.post url, (data) =>
      json = $.parseJSON data
      json = json ? data
      $(evt.target).parent().parent().html @photosPopupTemplate json
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
