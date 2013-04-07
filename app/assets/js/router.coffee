class umappin.Router extends Backbone.Router
  subroutes: {}
  params: null
  routes:
    'account':          'account'
    'account/:id':     'account'
    'signup':            'signup'
    'editmap':         'editmap'
    'messages/*subroute':     'messages'
    'login':              'login'
    'logout':            'logout'
    'linkProvider':   'linkProvider'
    'forgotPassword':   'forgotPassword'
    'changePassword':   'changePassword'
    'test':   'test'
    'synctest':   'syncTest'

  account: (id) ->
    @params = if id? then id: id else null
    setTemplate "/assets/templates/account.html"
    requirejs ['/assets/js/account/account_main.js'], () ->
      Account.init()

  signup: () ->
    setTemplate "/assets/templates/signup.html"

  editmap: () ->
    setTemplate "/assets/templates/editmap.html"
    requirejs ['/assets/js/lib/OpenLayers.js'], () ->
      requirejs ['/assets/js/maps/editmap.js'], () ->
        setTimeout (() -> Map.init()), 100

  messages: () ->
    subroutes = @subroutes
    requirejs ['/assets/js/messagesAPP/routers/router.js'], () ->
      subroutes.messagesRouter or= new messagesApp.Router "messages/"

  login: () ->
    setTemplate "/assets/templates/login.html"

  logout: () ->
    $.get "/logout", () ->
      setTemplate '/assets/templates/logout.html'
      sessionStorage.removeItem "user"
      updateSessionViews ""

  linkProvider: () ->
    setTemplate "/assets/templates/linkProvider.html"

  forgotPassword: () ->
    setTemplate "/assets/templates/forgotPassword.html"

  changePassword: () ->
    setTemplate "/assets/templates/changePassword.html"

  test: () ->
    setTemplate "/assets/templates/test.html"

  syncTest: () ->
    setTemplate "/assets/templates/syncTest.html"