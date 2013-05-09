window.Maps or= {}

Maps.init = () ->
  requirejs ['/assets/js/maps/models/map_model.js'], () ->
    requirejs ['/assets/js/maps/models/map_feature_model.js'], () ->
      requirejs ['/assets/js/maps/views/map_view.js'], () ->
        usrMaps  = Account.session.get 'maps'
        Maps.map = null
        if usrMaps.length
          Maps.map = new Maps.Map id: usrMaps[0]
          Maps.map.fetch complete: () -> Maps.mapview.render()
        else
          Maps.map = new Maps.Map
          Maps.map.save
            ownerId: Account.session.get 'id'
            { success: (resp) ->
                usrMaps.push resp.get 'id'
                Account.session.save maps: usrMaps
            }
        Maps.mapview = new Maps.MapView
          el: $('#map-container')
          model: Maps.map
