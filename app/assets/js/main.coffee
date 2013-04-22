window.setTemplate = (url, callback) ->
  $('div#actionResult').empty()
  $.get url, (template) ->
    $('div#content').empty().html template
    callback?.call(@)

window.updateSessionViews = (username) ->
  $('.loggedout').css 'display', if username !="" then 'none' else 'block'
  $('.loggedin').css('display', if username !="" then 'block' else 'none').find('#username').text username


$ () ->
  # Initialize main routing namespace
  window.umappin = window.umappin || {};

  # Initialize the main router
  requirejs ['/assets/js/router.js'], () ->
    umappin.router or= new umappin.Router
    Backbone.history.start()

  # Check the user session
  json = sessionStorage.getItem "user"
  if json? and json != ""
    usr = JSON.parse json
    updateSessionViews usr.name
  else
    sessionRequest = $.get "/sessionuser"
    sessionRequest.done (data) ->
      updateSessionViews data.name
      sessionStorage.setItem "user", JSON.stringify data
    updateSessionViews ""
