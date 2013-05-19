window.Maps or= {}

Maps.initFeaturesMap = () ->
  requirejs ['/assets/js/maps/models/map_model.js'], () ->
    requirejs ['/assets/js/maps/models/map_feature_model.js'], () ->
      requirejs ['/assets/js/maps/views/map_view.js'], () ->
        requirejs ['/assets/js/maps/views/features_map_view.js'], () ->
          usrMaps  = Account.session.get 'maps'
          if usrMaps.length
            Maps.featuresMap = new Maps.Map id: usrMaps[0]
            Maps.featuresMap.fetch complete: () -> Maps.featuresMapView.render()
          else
            Maps.featuresMap = new Maps.Map
            Maps.featuresMap.save
              ownerId: Account.session.get 'id'
              { success: (resp) ->
                  usrMaps.push resp.get 'id'
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
