window.Account or= {}

Account.loadUsersData = () ->
  Account.follows = new Account.Follows
  Account.followed = new Account.Followed

  Account.users = new Account.Users
  Account.usersview = new Account.UsersView collection: Account.users, refUser: Account.profile
  
  Account.follows.fetch
    success: () ->
      Account.followed.fetch
        success: () ->
          Account.users.fetch()


Account.loadProfileData = () ->
  Account.profile = new Account.User JSON.parse sessionStorage.getItem "user"
  Account.profileview = new Account.ProfileView model: Account.profile
  Account.profileview.render()

$ () ->
  Account.loadProfileData()





