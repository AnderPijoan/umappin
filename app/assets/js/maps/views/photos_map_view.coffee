window.Maps or= {}
OpenLayers.ImgPath = "/assets/img/openlayers/"
_.templateSettings.variable = 'rc'

class window.Maps.PhotosMapView extends Maps.MapView
  photosLayer: null
  photosPopupTemplate: _.template $('#photos-popup-template').html()
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
    # Event for reloading photos on drag/zoom
    @map.events.register 'moveend', @, (evt) ->
      @getViewPortPhotos false
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
    @getViewPortPhotos true

  getViewPortPhotos: (expand) ->
    viewBounds = @map.getExtent().transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    v = viewBounds.toArray(false)
    bbox = "x1=#{v[0]}&x2=#{v[2]}&y1=#{v[1]}&y2=#{v[3]}"
    @getMapPhotos bbox, expand

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
  getMapPhotos: (bbox, expand) ->
    $.ajax
      url: "/photos/rect?#{bbox}"
      success: (data) =>
        json = $.parseJSON data
        json = json ? data
        #data = JSON.parse data unless $.isPlainObject(data)
        results = json.results
        if (results.length < @minPhotos) and expand
          @map.zoomOut()
          expand = @map.getZoom() > 12
          @getViewPortPhotos expand
        else
          @photosLayer.clearMarkers()
          for feature in results
            do (feature) =>
              mapPhoto = new Maps.Picture feature
              if feature? then mapPhoto.altContent = feature.alternate_contents
              @createMapPhoto mapPhoto
              
  createMapPhoto: (mapPhoto) ->  
    x = mapPhoto.get('longitude')
    y = mapPhoto.get('latitude')
    lonlat = new OpenLayers.LonLat(x, y)
    feat = new OpenLayers.Feature @photosLayer, lonlat.transform(
      Maps.MapView.OSM_PROJECTION
      @map.getProjectionObject()
    )
    feat.popupClass = OpenLayers.Popup.FramedCloud
    feat.data.popupContentHTML = @photosPopupTemplate mapPhoto
    feat.data.overflow = 'auto'
    size = new OpenLayers.Size 32, 32
    offset = new OpenLayers.Pixel(-(size.w/2), -size.h)
    if mapPhoto.altContent?
      photoUrl = "#{mapPhoto.altContent.micro.content}&#{new Date().getTime()}"
    else if mapPhoto.get('get_content_location')?
      photoUrl = "#{mapPhoto.get('get_content_location')}?#{new Date().getTime()}"
    src = if photoUrl then photoUrl else '/assets/img/140x140.gif'
    feat.data.icon = new OpenLayers.Icon(src, size, offset)
    photo = feat.createMarker()
    feat.mapPhoto = mapPhoto
    that = @
    photo.events.registerPriority "click", feat, (evt) ->
      $.ajax(
        url: "photos/#{mapPhoto.get('id')}/userlikes/#{Account.session.get('id')}"
      ).always (resp) =>
        mapPhoto.likes = if resp.user_id then { is_beautiful: resp.is_beautiful, is_useful: resp.is_useful } else null
        $.ajax(
          url: "photos/#{mapPhoto.get('id')}/likestats"
        ).always (stats) =>
          mapPhoto.stats = stats
          if photo.popup?
            photo.popup.setContentHTML(that.photosPopupTemplate mapPhoto)
          else
            feat.data.popupContentHTML = that.photosPopupTemplate mapPhoto
          that.selectPhotoHandler evt, feat, lonlat
    photo.events.register "touchstart", feat, (evt) ->
      $.ajax(
        url: "photos/#{mapPhoto.get('id')}/userlikes/#{Account.session.get('id')}"
      ).always (resp) =>
        mapPhoto.likes = if resp.user_id then { is_beautiful: resp.is_beautiful, is_useful: resp.is_useful } else null
        $.ajax(
          url: "photos/#{mapPhoto.get('id')}/likestats"
        ).always (stats) =>
          mapPhoto.stats = likes
          if photo.popup?
            photo.popup.setContentHTML(that.photosPopupTemplate mapPhoto)
          else
            feat.data.popupContentHTML = that.photosPopupTemplate mapPhoto
          that.selectPhotoHandler evt, feat, lonlat
    @photosLayer.addMarker(photo)
    @listenTo mapPhoto, 'change', () ->
      @stopListening mapPhoto
      photo.destroy()
      @createMapPhoto mapPhoto

  createPhoto: (lnglat) ->
    p = new OpenLayers.Geometry.Point lnglat.lon, lnglat.lat
    p.transform @map.getProjectionObject(), Maps.MapView.OSM_PROJECTION
    picture = new Maps.Picture      
    picture.save
      latitude: p.y
      longitude: p.x
      date_created: new Date().getTime()
      { success: (resp) => @createMapPhoto picture }

  addPictureView: (picture) ->
    @featurePictureView = new PictureView
      model: picture
      readonly: picture.get("owner_id") != Account.session.get("id")
      showInfo: false
      picWidth: '8em'
    $("div.mapPhotoHolder").last().append @featurePictureView.render().el

  selectPhotoHandler: (evt, photo, lonlat) ->
    if photo.popup
      photo.popup.toggle()
    else
      photo.popup = photo.createPopup(true)
      photo.popup.panMapIfOutOfView = false
      @map.addPopup(photo.popup)
      photo.popup.show()
      @addPictureView photo.mapPhoto
      $('.beautifulPhotoButton').bind('click', (e) => @beautifulPhoto(e, photo.mapPhoto))
      $('.usefulPhotoButton').bind('click', (e) => @usefulPhoto(e, photo.mapPhoto))
      $('.likeallPhotoButton').bind('click', (e) => @likeallPhoto(e, photo.mapPhoto))
      $('.savePhotoButton').bind('click', (e) => @savePhoto(e, photo.mapPhoto))
      $('.deletePhotoButton').bind('click', (e) => @deletePhoto(photo))
    OpenLayers.Event.stop(evt)

  savePhoto: (e, photo) ->
    photo.save
      title: $(e.target).parents('div.photos-popup').find('input[type=text]').val()
      description: $(e.target).parents('div.photos-popup').find('textarea').val()

  deletePhoto: (photo) ->
    photo.mapPhoto.destroy()
    photo.popup.destroy()
    photo.destroy()

  beautifulPhoto: (e, photo) -> @likePhoto(e, photo, '{"is_useful": 0, "is_beautiful": 1}')

  usefulPhoto: (e, photo) -> @likePhoto(e, photo, '{"is_useful": 1, "is_beautiful": 0}')

  likeallPhoto: (e, photo) -> @likePhoto(e, photo, '{"is_useful": 1, "is_beautiful": 1}')

  likePhoto: (e, photo, like) ->
    $.ajax
      url: "photos/#{photo.get('id')}/userlikes"
      type: 'POST'
      contentType: 'application/json'
      data: like
      success: (resp) ->
        html = "<label>Marked as:</label>"
        if resp.is_beautiful == 1
          newval = parseInt($(e.target).parents('.photos-popup').find('.beautifulsField').val())
          $(e.target).parents('.photos-popup').find('.beautifulsField').val(newval+1)
          html += "<label>Beautiful</label>"
        if resp.is_useful == 1
          newval = parseInt($(e.target).parents('.photos-popup').find('.usefulsField').val())
          $(e.target).parents('.photos-popup').find('.usefulsField').val(newval+1)
          html += "<label>Useful</label>"
        $(e.target).parent().parent().html html