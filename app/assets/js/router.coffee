class umappin.Router extends Backbone.Router
  subroutes: {}
  params: null
  routes:
    'account':            'account'
    'account/:id':        'account'
    'maps':               'maps'
    'wall':               'wall'
    'messages/*subroute': 'messages'
    'signup':             'signup'
    'login':              'login'
    'logout':             'logout'
    'linkProvider':       'linkProvider'
    'forgotPassword':     'forgotPassword'
    'changePassword':     'changePassword'
    'leafletmap':         'leafletmap'
    'test':               'test'
    'synctest':           'syncTest'

  account: (id) ->
    @params = if id? then id: id else null
    setTemplate "/assets/templates/account.html", () ->
      requirejs ['/assets/js/account/account_main.js'], () ->
      	requirejs ['/assets/js/StatisticsApp.js'], () ->
        	Account.init()

  maps: () ->
    setTemplate "/assets/templates/maps.html", () ->
      requirejs ['/assets/js/lib/openlayers.min.js'], () ->
        requirejs ['/assets/js/maps/maps.js'], () ->
          Maps.init()
          
  wall: () ->
    setTemplate "/assets/templates/wall.html"

  messages: () ->
    subroutes = @subroutes
    requirejs ['/assets/js/messagesApp/collection/discussionCollection.js'], () ->
      requirejs ['/assets/js/messagesApp/routers/router.js'], () ->
        subroutes.messagesRouter or= new messagesApp.Router "messages/"
  
  login: () ->
    setTemplate "/assets/templates/login.html"
    requirejs ['/assets/js/messagesApp/routers/router.js'], () ->
      subroutes.messagesRouter or= new messagesApp.Router "messages/"

  logout: () ->
    $.get "/logout", () ->
      setTemplate '/assets/templates/logout.html'
      updateSessionViews ""

  signup: () ->  # Need to separate js source
    setTemplate "/assets/templates/signup.html"

  login: () ->  # Need to separate js source
    setTemplate "/assets/templates/login.html"

  linkProvider: () ->  # Need to separate js source
    setTemplate "/assets/templates/linkProvider.html"

  forgotPassword: () -> # Need to separate js source
    setTemplate "/assets/templates/forgotPassword.html"

  changePassword: () ->  # Need to separate js source
    setTemplate "/assets/templates/changePassword.html"

  # Old leaflet map, remove before release
  leafletmap: () ->
    setTemplate "/assets/templates/leaflet-map.html", () ->
      requirejs ['/assets/js/lib/leaflet.min.js'], () ->
        requirejs ['/assets/js/lib/leaflet.draw.js'], () ->
          requirejs ['/assets/js/map/leaflet-map.js'], () ->
            Map.init()

  # Testing routes, remove before release
  test: () ->
    setTemplate "/assets/templates/test.html"

  syncTest: () ->
    setTemplate "/assets/templates/syncTest.html"
    requirejs ['/assets/js/test/subitem_model.js'], () ->
      requirejs ['/assets/js/test/subitem_collection.js'], () ->
        requirejs ['/assets/js/test/syncTest.js']
