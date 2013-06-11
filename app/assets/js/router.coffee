class umappin.Router extends Backbone.Router
  subroutes: {}
  params: null
  routes:
    'account':            'account'
    'account/:id':        'account'
    'featuresMap':        'featuresMap'
    'markersMap':         'markersMap'
    'searchMap':          'searchMap'
    'routesMap':          'routesMap'
    'photosMap':          'photosMap'
    'messages/*subroute': 'messages'
    'wall/*subroute': 	  'wall'
    'signup':             'signup'
    'login':              'login'
    'logout':             'logout'
    'linkProvider':       'linkProvider'
    'forgotPassword':     'forgotPassword'
    'userlist':           'userlist'
    'profile':            'profile'
    'awards':			  'awards'

  account: (id) ->
    @params = if id? then id: id else null
    setTemplate "/assets/templates/account.html", () ->
      requirejs ['/assets/js/account/account_main.js'], () ->
        	Account.init()

  userlist: () ->
    setTemplate "/assets/templates/userlist.html", () ->
      requirejs ['/assets/js/account/account_main.js'], () ->
        Account.loadUserList()
        #Account.init()
        #Account.loadUsersData();

  profile: () ->
    setTemplate "/assets/templates/profile.html", () ->
      requirejs ['/assets/js/account/account_main.js'], () ->
        Account.init()

  featuresMap: () ->
    setTemplate "/assets/templates/maps.html", () ->
      requirejs ['/assets/js/lib/openlayers.extended.js'], () ->
        requirejs ['/assets/js/maps/maps.js'], () ->
          Maps.initFeaturesMap()

  markersMap: () ->
    setTemplate "/assets/templates/maps.html", () ->
      requirejs ['/assets/js/lib/openlayers.extended.js'], () ->
        requirejs ['/assets/js/maps/maps.js'], () ->
          Maps.initMarkersMap()

  searchMap: () ->
    setTemplate "/assets/templates/maps.html", () ->
      requirejs ['/assets/js/lib/openlayers.extended.js'], () ->
        requirejs ['/assets/js/maps/maps.js'], () ->
          Maps.initSearchMap()

  routesMap: () ->
    setTemplate "/assets/templates/maps.html", () ->
      requirejs ['/assets/js/lib/openlayers.extended.js'], () ->
        requirejs ['/assets/js/maps/maps.js'], () ->
          Maps.initRoutesMap()

  photosMap: () ->
    setTemplate "/assets/templates/maps.html", () ->
      requirejs ['/assets/js/lib/openlayers.extended.js'], () ->
        requirejs ['/assets/js/maps/maps.js'], () ->
          Maps.initPhotosMap()

  messages: () ->
    subroutes = @subroutes
    requirejs ['/assets/js/messagesApp/collection/discussionCollection.js'], () ->
     requirejs ['/assets/js/messagesApp/model/user.js'], () ->
        requirejs [
          '/assets/js/messagesApp/collection/followedCollection.js',
          '/assets/js/messagesApp/view/user_view.js'
        ], () ->
          requirejs ['/assets/js/messagesApp/routers/router.js'], () ->
            subroutes.messagesRouter or= new messagesApp.Router "messages/"

  wall: () ->
    subroutes = @subroutes
    requirejs ['/assets/js/timelineApp/routers/router.js'], () ->
      subroutes.timelineRouter or= new timelineApp.Router "wall/"

  awards: () ->
    setTemplate "/assets/templates/awards.html", () ->
      requirejs ['/assets/js/StatisticsApp.js']

  login: () ->
    setTemplate "/assets/templates/login.html"
    requirejs ['/assets/js/messagesApp/routers/router.js'], () ->
      subroutes.messagesRouter or= new messagesApp.Router "messages/"

  logout: () ->
    $.get "/logout", () ->
      sessionStorage.removeItem "user"
      updateSessionViews ""
      location.href='./'

  signup: () ->  # Need to separate js source
    setTemplate "/assets/templates/signup.html"

  login: () ->  # Need to separate js source
    setTemplate "/assets/templates/login.html"

  linkProvider: () ->  # Need to separate js source
    setTemplate "/assets/templates/linkProvider.html"

  forgotPassword: () -> # Need to separate js source
    setTemplate "/assets/templates/forgotPassword.html"
