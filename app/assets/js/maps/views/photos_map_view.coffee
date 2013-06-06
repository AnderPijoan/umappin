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
              mapPhoto = new Maps.Picture feature
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
    src = mapPhoto.get('get_content_location') ? '/assets/img/140x140.gif'
    feat.data.icon = new OpenLayers.Icon(src, size, offset)
    photo = feat.createMarker()
    feat.mapPhoto = mapPhoto
    that = @
    photo.events.register "mousedown", feat, (evt) -> that.selectPhotoHandler evt, feat, lonlat
    photo.events.register "touchstart", feat, (evt) -> that.selectPhotoHandler evt, feat, lonlat
    @photosLayer.addMarker(photo)

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
      @map.addPopup(photo.popup)
      photo.popup.show()
      @addPictureView photo.mapPhoto
      $('.savePhotoButton').bind('click', (e) => @savePhoto(e, photo.mapPhoto))
      $('.deletePhotoButton').bind('click', (e) => @deletePhoto(photo.mapPhoto))
    OpenLayers.Event.stop(evt)

  savePhoto: (e, photo) ->
    photo.save()
      #{ success: (resp) => console.log resp } # TODO set view data first

  deletePhoto: (photo) ->
    photo.destroy()
      #{ success: (resp) => console.log resp } #TODO delete feature & popup
