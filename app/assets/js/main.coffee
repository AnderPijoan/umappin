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

  sessionRequest = $.get "/sessionuser?#{new Date().getTime()}"
  sessionRequest.done (data) ->
    unreadDiscussions = $.get "/discussions/unread"
    unreadDiscussions.done (unread) ->
      if unread != 'No discussion found'
        $("#messages-badge").show();
        $("#messages-badge").text(unread.length)
        $("#message-unread").attr("href", "./#messages/message/"+unread.pop().id)
        sessionStorage.setItem('unread-discusion',JSON.stringify(unread))
    setInterval ->
      unreadDiscussions = $.get "/discussions/unread"
      unreadDiscussions.done (unread) ->
        if unread != 'No discussion found'
          $("#messages-badge").show();
          $("#messages-badge").text(unread.length)
          $("#message-unread").attr("href", "./#messages/message/"+unread.pop().id)
          sessionStorage.setItem('unread-discusion',JSON.stringify(unread))
    , 10000
    if data.profilePicture
      profileImg = './photos/'+data.profilePicture+'/content'
    else
      profileImg = './assets/img/140x140.gif'
    sessionStorage.setItem("user", JSON.stringify(data));
    setSessionUser data
    setTemplate "/assets/templates/main_logged.html", () =>
      $('#profile-picture').html('<img id="my-avatar" src="'+profileImg+'" onload="resize(this)">')
      # Initialize the main router
      requirejs ['/assets/js/router.js'], () ->
        umappin.router or= new umappin.Router
        Backbone.history.start()
        @navigate '#wall/news'
    #   location.href = "/#wall/news"
  sessionRequest.error ->
    updateSessionViews ""
    setTemplate "/assets/templates/main.html"