# Sets a template for the main content
window.setTemplate = (url, callback) ->
  $('div#actionResult').empty()
  $('div#actionResult').css('display','none')
  $.get url, (template) ->
    $('div#content').empty().html template
    callback?.call @

# Updates views based on session authentication
window.updateSessionViews = (username) ->
  $('.loggedout').css 'display', if username !="" then 'none' else 'block'
  $('.loggedin').css('display', if username !="" then 'block' else 'none').find('#username').text username

# Sets the session User from server
window.setSessionUser = (user) ->
  requirejs ['/assets/js/account/models/user_model.js'], () ->
    Account.session = new Account.User user
    updateSessionViews Account.session.get 'name'

# Main script
$ () ->
  # Initialize main routing namespace
  window.umappin or= {}

  # Initialize the main router
  requirejs ['/assets/js/router.js'], () ->
    umappin.router or= new umappin.Router
    Backbone.history.start()
  sessionRequest = $.get "/sessionuser"
  sessionRequest.done (data) -> setSessionUser data
  updateSessionViews ""
