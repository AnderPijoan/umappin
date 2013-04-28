window.MapEditor or= {}

MapEditor.init = () ->
  requirejs ['/assets/js/map/models/map_model.js'], () ->
    requirejs ['/assets/js/map/views/map_view.js'], () ->
      usrMap  = Account.session.get 'profileMap'
      MapEditor.map = if usrMap? then new Map id: usrMap else new Map
      MapEditor.mapview = new MapView model: MapEditor.map
      $('#map-container').html MapEditor.mapview.render().el
