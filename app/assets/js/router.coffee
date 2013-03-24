class window.MainRouter extends Backbone.Router
  params: null
  routes:
    'account':      'account'
    'account/:id':  'account'
    'signup':       'signup'
    'editmap':      'editmap'
    'messages':     'messages'
    'account':      'account'
    'login':        'login'
    'linkProvider': 'linkProvider'
    'profile':      'profile'
  account: (id) ->
    @params = if id? then id: id else null
    setTemplate "/account"
  signup: () ->
    setTemplate "/signup"
  editmap: () ->
    setTemplate "/editmap"
  messages: () ->
    setTemplate "/messages"
  login: () ->
    setTemplate "/login"
  linkProvider: () ->
    setTemplate "/linkProvider"
  profile: () ->
    setTemplate "/profile"