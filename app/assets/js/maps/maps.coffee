window.Maps or= {}

Maps.initFeaturesMap = () ->
  requirejs ['/assets/js/maps/models/map_model.js'], () ->
    requirejs ['/assets/js/maps/models/map_feature_model.js'], () ->
      requirejs ['/assets/js/maps/views/map_view.js'], () ->
        requirejs ['/assets/js/maps/views/features_map_view.js'], () ->
          usrMaps  = Account.session.get 'maps'
          if usrMaps.features? and usrMaps.features.length
            Maps.featuresMap = new Maps.Map id: usrMaps.features[0]
            Maps.featuresMap.fetch complete: () -> Maps.featuresMapView.render()
          else
            Maps.featuresMap = new Maps.Map
            Maps.featuresMap.save
              ownerId: Account.session.get 'id'
              { success: (resp) ->
                  usrMaps.features = [resp.get 'id']
                  Account.session.save maps: usrMaps
              }
          Maps.featuresMapView = new Maps.FeaturesMapView
            el: $('#map-container')
            model: Maps.featuresMap

Maps.initMarkersMap = () ->
  requirejs ['/assets/js/maps/models/map_model.js'], () ->
    requirejs ['/assets/js/maps/views/map_view.js'], () ->
      requirejs ['/assets/js/maps/views/markers_map_view.js'], () ->
        Maps.markersMapview = new Maps.MarkersMapView
          el: $('#map-container')
          model: new Maps.Map

Maps.initSearchMap = () ->
  requirejs ['/assets/js/maps/models/map_model.js'], () ->
    requirejs ['/assets/js/maps/views/map_view.js'], () ->
      requirejs ['/assets/js/maps/views/search_map_view.js'], () ->
        Maps.searchMapview = new Maps.SearchMapView
          el: $('#map-container')
          model: new Maps.Map

Maps.initRoutesMap = () ->
  requirejs ['/assets/js/maps/models/map_model.js'], () ->
    requirejs ['/assets/js/maps/models/map_route_model.js'], () ->
      requirejs ['/assets/js/maps/models/map_route_like_model.js'], () ->
        requirejs ['/assets/js/maps/views/map_view.js'], () ->
          requirejs ['/assets/js/maps/views/routes_map_view.js'], () ->
            usrMaps  = Account.session.get 'maps'
            # TODO: handle the different user map types (Routes, etc..), for now it defaults to maps[0]
            if usrMaps.routes? and usrMaps.routes.length
              Maps.routesMap = new Maps.Map id: usrMaps.routes[0]
              Maps.routesMap.fetch complete: () -> Maps.routesMapView.render()
            else
              Maps.routesMap = new Maps.Map
              Maps.routesMap.save
                ownerId: Account.session.get 'id'
                { success: (resp) ->
                  usrMaps.routes = [resp.get 'id']
                  Account.session.save maps: usrMaps
                }
            Maps.routesMapView = new Maps.RoutesMapView
              el: $('#map-container')
              model: Maps.routesMap