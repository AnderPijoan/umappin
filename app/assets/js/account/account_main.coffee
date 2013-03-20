window.Account or= {}

Account.loadUsersData = () ->
  Account.users = new Account.Users
  Account.usersview = new Account.UsersView Account.users, Account.profile
  Account.users.fetch()

  Account.follows = new Account.Follows
  Account.follows.fetch()

Account.loadProfileData = () ->
  Account.profile = new Account.User JSON.parse sessionStorage.getItem "user"
  Account.profileview = new Account.ProfileView model: Account.profile
  Account.profileview.render()

$ () ->
  Account.loadProfileData()





