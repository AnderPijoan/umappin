window.Account or= {}

Account._loadUsersData = () ->
  Account.users = new Account.Users
  Account.follows = new Account.Follows
  Account.followed = new Account.Followed

  Account.usersview = new Account.UsersView
    collection: Account.users
    follows: Account.follows
    followed: Account.followed
    refUser: Account.profile

  Account.follows.fetch
    success: () ->
      Account.followed.fetch
        success: () ->
          Account.users.fetch()

Account._loadProfileData = () ->
  usrParams = umappin.router.params ? JSON.parse sessionStorage.getItem "user"
  Account.profile = new Account.User usrParams
  Account.profile.fetch()
  Account.profileview = new Account.ProfileView model: Account.profile

Account.loadProfileData = () ->
  requirejs [
    '/assets/js/account/models/user_model.js'
  ], () ->
    requirejs [
      '/assets/js/account/views/profile_view.js'
    ], () ->
      Account._loadProfileData()

Account.loadUsersData = () ->
  if !Account.usersview
    requirejs [
      '/assets/js/account/collections/user_collection.js'
      '/assets/js/account/models/follow_model.js'
    ], () ->
      requirejs [
        '/assets/js/account/collections/follow_collection.js'
      ], () ->
        requirejs [
          '/assets/js/account/collections/follows_collection.js'
          '/assets/js/account/collections/followed_collection.js'
        ], () ->
          requirejs [
            '/assets/js/account/views/user_follows_view.js'
            '/assets/js/account/views/user_followed_view.js'
          ], () ->
            requirejs [
              '/assets/js/account/views/user_row_view.js'
            ], () ->
              requirejs [
                '/assets/js/account/views/users_view.js'
              ], () ->
                Account._loadUsersData()

$ () ->
  Account.usersview = null
  Account.loadProfileData()
  